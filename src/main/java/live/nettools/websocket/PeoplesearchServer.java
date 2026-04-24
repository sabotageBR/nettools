package live.nettools.websocket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import live.nettools.service.peoplesearch.PeoplesearchService;
import live.nettools.to.PeopleSearchResultTO;
import live.nettools.to.PeoplesearchTO;

/**
 * Endpoint WebSocket do People Search.
 *
 * Estado e fila são estáticos para serem compartilhados entre todas as
 * instâncias do endpoint (o container cria uma instância por conexão).
 * Uma única worker-thread daemon processa a fila, acordada via
 * {@link Condition} — sem timer polling de 2s e sem o cooldown de 5s do
 * desenho antigo.
 */
@ServerEndpoint("/PeoplesearchServer")
public class PeoplesearchServer {

    private static final Logger LOG = Logger.getLogger(PeoplesearchServer.class.getName());
    private static final long START_DELAY_MS = 1000L;

    private static final LinkedHashMap<String, QueuedSearch> QUEUE = new LinkedHashMap<String, QueuedSearch>();
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition NEW_WORK = LOCK.newCondition();
    private static final AtomicBoolean WORKER_STARTED = new AtomicBoolean(false);

    @Inject
    private PeoplesearchService service;

    @OnOpen
    public void onOpen(Session session) {
        ensureWorkerStarted();
        int size;
        LOCK.lock();
        try {
            size = QUEUE.size();
        } finally {
            LOCK.unlock();
        }
        sendStatus(session, "Users waiting: " + size);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            PeoplesearchTO input = new Gson().fromJson(message, PeoplesearchTO.class);
            if (input == null) {
                return;
            }
            input.setDateTime(LocalDateTime.now());
            enqueue(session, input);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em PeoplesearchServer", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        removeFromQueue(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.log(Level.FINE, "Erro em sessão " + session.getId(), t);
        removeFromQueue(session);
    }

    private void enqueue(Session session, PeoplesearchTO input) {
        if (service == null) {
            sendStatus(session, "Service not available");
            return;
        }
        int position;
        boolean hadQueued;
        LOCK.lock();
        try {
            hadQueued = QUEUE.containsKey(session.getId());
            // SEMPRE usa o input mais recente. Se já havia entrada na fila
            // (worker ainda não pegou OU está processando a anterior), ela
            // é substituída pela nova — assim o usuário pode alterar o
            // formulário e reenviar Go sem a busca voltar com valor antigo.
            QUEUE.put(session.getId(), new QueuedSearch(session, input, service));
            if (!hadQueued) {
                NEW_WORK.signalAll();
            }
            position = indexOf(session.getId());
        } finally {
            LOCK.unlock();
        }
        if (position == 0) {
            sendStatus(session, hadQueued ? "Queued (running with updated values next)" : "Starting soon...");
        } else {
            sendStatus(session, "Position in queue: " + position);
        }
        broadcastPositions();
    }

    private void removeFromQueue(Session session) {
        boolean removed;
        LOCK.lock();
        try {
            removed = QUEUE.remove(session.getId()) != null;
        } finally {
            LOCK.unlock();
        }
        if (removed) {
            broadcastPositions();
        }
    }

    private static int indexOf(String id) {
        int i = 0;
        for (String k : QUEUE.keySet()) {
            if (k.equals(id)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static void ensureWorkerStarted() {
        if (WORKER_STARTED.compareAndSet(false, true)) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    workerLoop();
                }
            }, "PeoplesearchWorker");
            t.setDaemon(true);
            t.start();
        }
    }

    private static void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            QueuedSearch next;
            LOCK.lock();
            try {
                while (QUEUE.isEmpty()) {
                    try {
                        NEW_WORK.await();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                next = QUEUE.values().iterator().next();
            } finally {
                LOCK.unlock();
            }

            broadcastPositions();

            try {
                Thread.sleep(START_DELAY_MS);
                if (next.session.isOpen()) {
                    sendStatus(next.session, "Starting search...");
                    next.service.executar(next.input, next.session);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Erro executando People Search", t);
            } finally {
                if (next.session.isOpen()) {
                    sendRaw(next.session, "", "FIM");
                }
            }

            LOCK.lock();
            boolean leftNewer = false;
            try {
                // Só remove se a entrada na fila ainda for a mesma que o
                // worker processou. Se o usuário enviou nova busca durante
                // o processamento, o enqueue substituiu a entrada — nesse
                // caso mantemos, para o worker pegar na próxima iteração.
                QueuedSearch current = QUEUE.get(next.session.getId());
                if (current == next) {
                    QUEUE.remove(next.session.getId());
                } else {
                    leftNewer = current != null;
                    NEW_WORK.signalAll();
                }
            } finally {
                LOCK.unlock();
            }
            broadcastPositions();
            if (leftNewer && next.session.isOpen()) {
                sendStatus(next.session, "Re-running with updated values...");
            }
        }
    }

    private static void broadcastPositions() {
        List<QueuedSearch> snapshot;
        LOCK.lock();
        try {
            snapshot = new ArrayList<QueuedSearch>(QUEUE.values());
        } finally {
            LOCK.unlock();
        }
        int idx = 0;
        for (QueuedSearch q : snapshot) {
            idx++;
            if (idx == 1) {
                continue;
            }
            if (q.session.isOpen()) {
                sendStatus(q.session, "Position in queue: " + (idx - 1));
            }
        }
    }

    private static void sendStatus(Session session, String msg) {
        sendRaw(session, "STATUS", msg);
    }

    private static void sendRaw(Session session, String titulo, String texto) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(new Gson().toJson(
                        new PeoplesearchTO("", new PeopleSearchResultTO(titulo, texto))));
            } catch (Exception e) {
                LOG.log(Level.FINE, "Falha ao enviar mensagem WS", e);
            }
        }
    }

    private static final class QueuedSearch {
        final Session session;
        final PeoplesearchTO input;
        final PeoplesearchService service;

        QueuedSearch(Session session, PeoplesearchTO input, PeoplesearchService service) {
            this.session = session;
            this.input = input;
            this.service = service;
        }
    }
}
