package live.nettools.websocket;

import java.time.LocalDateTime;
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

import live.nettools.service.reversedns.ReverseDnsService;
import live.nettools.to.ReverseDnsTO;

@ServerEndpoint("/ReverseDnsServer")
public class ReverseDnsServer {

    private static final Logger LOG = Logger.getLogger(ReverseDnsServer.class.getName());

    @Inject
    private ReverseDnsService reverseDnsService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            ReverseDnsTO to = new Gson().fromJson(message, ReverseDnsTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            reverseDnsService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em ReverseDnsServer", e);
        }
    }

    @OnOpen
    public void open(Session session) {}

    @OnError
    public void error(Session session, Throwable t) {
        LOG.log(Level.FINE, "Erro em ReverseDnsServer", t);
    }

    @OnClose
    public void closedConnection(Session session) {}
}
