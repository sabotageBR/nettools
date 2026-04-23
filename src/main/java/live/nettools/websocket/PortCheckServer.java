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

import live.nettools.service.portcheck.PortCheckService;
import live.nettools.to.PortCheckTO;

@ServerEndpoint("/PortCheckServer")
public class PortCheckServer {

    private static final Logger LOG = Logger.getLogger(PortCheckServer.class.getName());

    @Inject
    private PortCheckService portCheckService;

    @OnMessage
    public void recebeMensagem(String message, Session session) {
        try {
            PortCheckTO to = new Gson().fromJson(message, PortCheckTO.class);
            if (to == null) return;
            to.setDateTime(LocalDateTime.now());
            portCheckService.executar(to, session);
        } catch (Exception e) {
            LOG.log(Level.INFO, "Mensagem inválida em PortCheckServer", e);
        }
    }

    @OnOpen
    public void open(Session session) {}

    @OnError
    public void error(Session session, Throwable t) {
        LOG.log(Level.FINE, "Erro em PortCheckServer", t);
    }

    @OnClose
    public void closedConnection(Session session) {}
}
