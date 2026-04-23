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

import live.nettools.service.isitdown.IsItDownService;
import live.nettools.to.IsItDownTO;

@ServerEndpoint("/IsItDownServer")
public class IsItDownServer {

    private static final Logger LOG = Logger.getLogger(IsItDownServer.class.getName());

    @Inject
    private IsItDownService isItDownService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            IsItDownTO to = new Gson().fromJson(message, IsItDownTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            isItDownService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em IsItDownServer", e);
        }
    }

    @OnOpen public void open(Session session) {}
    @OnError public void error(Session session, Throwable t) { LOG.log(Level.FINE, "IsItDownServer err", t); }
    @OnClose public void closedConnection(Session session) {}
}
