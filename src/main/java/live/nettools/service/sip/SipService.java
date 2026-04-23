package live.nettools.service.sip;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.SipTO;
import live.nettools.util.HostValidator;

@Named
public class SipService implements Serializable {

    private static final long serialVersionUID = -6986454410309827919L;
    private static final Logger LOG = Logger.getLogger(SipService.class.getName());

    public String executar(SipTO sip, Session session) {
        StringBuilder sb = new StringBuilder();
        Gson gson = new Gson();
        try {
            HostValidator.requireHost(sip.getHost());
            int porta = HostValidator.requirePort(sip.getPorta());
            String user = HostValidator.optionalFreeText(sip.getUser(), "user");

            String result = CheckSipUdp.checkSipUdp(sip.getHost().trim(), porta, user);
            if (session != null) {
                session.getBasicRemote().sendText(gson.toJson(new SipTO(sip.getHost(), result)));
                session.getBasicRemote().sendText(gson.toJson(new SipTO(sip.getHost(), "FIM")));
            }
            if (result != null) {
                sb.append(result);
            }
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em SipService: {0}", ex.getMessage());
            try {
                if (session != null) {
                    session.getBasicRemote().sendText(
                            gson.toJson(new SipTO(sip.getHost(), "Invalid input: " + ex.getMessage())));
                    session.getBasicRemote().sendText(
                            gson.toJson(new SipTO(sip.getHost(), "FIM")));
                }
            } catch (Exception ignored) {
                // silenciar falha de WebSocket
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em SipService", e);
        }
        return sb.toString();
    }
}
