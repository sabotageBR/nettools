package live.nettools.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/NettoolServer")
public class NettoolServer {

    private static final Logger LOG = Logger.getLogger(NettoolServer.class.getName());
    private static final Map<Session, String> mapaQueue = new ConcurrentHashMap<Session, String>();
    private static final Map<Integer, Map<String, String>> mapaGeral = new HashMap<Integer, Map<String, String>>();

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        mapaQueue.put(session, message);
    }

    @OnOpen
    public void open(Session session) {
        mapaQueue.put(session, "");
        LOG.log(Level.FINE, "Nova sessão: {0}", session.getId());
    }

    @OnError
    public void error(Session session, Throwable t) {
        mapaQueue.remove(session);
        LOG.log(Level.FINE, "Erro em sessão " + session.getId(), t);
    }

    @OnClose
    public void closedConnection(Session session) {
        mapaQueue.remove(session);
        LOG.log(Level.FINE, "Sessão encerrada: {0}", session.getId());
    }

    public Map<Integer, Map<String, String>> recuperarMapaGeral() {
        return mapaGeral;
    }
}