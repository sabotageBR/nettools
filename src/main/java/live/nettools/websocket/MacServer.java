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

import live.nettools.service.mac.MacService;
import live.nettools.to.MacTO;

@ServerEndpoint("/MacServer")
public class MacServer {

    private static final Logger LOG = Logger.getLogger(MacServer.class.getName());

    @Inject
    private MacService macService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            MacTO to = new Gson().fromJson(message, MacTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            macService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em MacServer", e);
        }
    }

    @OnOpen public void open(Session session) {}
    @OnError public void error(Session session, Throwable t) { LOG.log(Level.FINE, "MacServer err", t); }
    @OnClose public void closedConnection(Session session) {}
}
