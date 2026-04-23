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

import live.nettools.service.httpstatus.HttpStatusService;
import live.nettools.to.HttpStatusTO;

@ServerEndpoint("/HttpStatusServer")
public class HttpStatusServer {

    private static final Logger LOG = Logger.getLogger(HttpStatusServer.class.getName());

    @Inject
    private HttpStatusService httpStatusService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            HttpStatusTO to = new Gson().fromJson(message, HttpStatusTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            httpStatusService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em HttpStatusServer", e);
        }
    }

    @OnOpen public void open(Session session) {}
    @OnError public void error(Session session, Throwable t) { LOG.log(Level.FINE, "HttpStatusServer err", t); }
    @OnClose public void closedConnection(Session session) {}
}
