package live.nettools.service.portcheck;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.PortCheckTO;
import live.nettools.util.HostValidator;

/**
 * Port Check: testa se uma única porta TCP aceita conexão a partir da nossa
 * rede. Pure Java — sem shell, sem dependências externas.
 *
 * Status possíveis:
 * <ul>
 *   <li>OPEN     — conexão completou o 3-way handshake.</li>
 *   <li>CLOSED   — servidor respondeu mas recusou (RST).</li>
 *   <li>FILTERED — timeout sem resposta; firewall está descartando.</li>
 * </ul>
 */
@Named
public class PortCheckService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PortCheckService.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 3000;

    public String executar(PortCheckTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        try {
            String host = HostValidator.requireHost(input.getHost());
            int port = HostValidator.requirePort(input.getPorta());

            sendLine(session, gson, input, "Connecting to " + host + ":" + port + " ...");

            long start = System.nanoTime();
            Socket socket = new Socket();
            String status;
            String message;
            try {
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
                long elapsed = (System.nanoTime() - start) / 1_000_000L;
                status = "OPEN";
                message = "Port " + port + " on " + host + " is OPEN — responded in " + elapsed + " ms";
                input.setStatus(status);
                input.setElapsedMs(elapsed);
            } catch (SocketTimeoutException to) {
                status = "FILTERED";
                message = "Port " + port + " on " + host + " is FILTERED — no response within "
                        + CONNECT_TIMEOUT_MS + " ms (likely firewalled)";
                input.setStatus(status);
            } catch (ConnectException ce) {
                status = "CLOSED";
                message = "Port " + port + " on " + host + " is CLOSED — host reachable but nothing listening";
                input.setStatus(status);
            } catch (NoRouteToHostException nr) {
                status = "UNREACHABLE";
                message = "Host " + host + " has no route — " + nr.getMessage();
                input.setStatus(status);
            } catch (UnknownHostException uh) {
                status = "UNKNOWN_HOST";
                message = "Host " + host + " could not be resolved";
                input.setStatus(status);
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
            sendLine(session, gson, input, message);
            sb.append(message);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em PortCheckService: {0}", ex.getMessage());
            sendLine(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em PortCheckService", e);
            sendLine(session, gson, input, "Error: " + e.getMessage());
        } finally {
            sendLine(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private void sendLine(Session session, Gson gson, PortCheckTO input, String line) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.getBasicRemote().sendText(gson.toJson(new PortCheckTO(input.getHost(), line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }
}
