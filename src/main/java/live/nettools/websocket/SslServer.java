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

import live.nettools.service.ssl.SslService;
import live.nettools.to.SslTO;

@ServerEndpoint("/SslServer")
public class SslServer {

    private static final Logger LOG = Logger.getLogger(SslServer.class.getName());

    @Inject
    private SslService sslService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            SslTO to = new Gson().fromJson(message, SslTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            sslService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em SslServer", e);
        }
    }

    @OnOpen public void open(Session session) {}
    @OnError public void error(Session session, Throwable t) { LOG.log(Level.FINE, "SslServer err", t); }
    @OnClose public void closedConnection(Session session) {}
}
