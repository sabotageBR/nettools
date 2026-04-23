package live.nettools.service.isitdown;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.IsItDownTO;
import live.nettools.util.HostValidator;
import live.nettools.util.InsecureSsl;

/**
 * Is It Down: consolidated up/down diagnosis for a public host.
 * Roda DNS resolve → TCP 443 → TCP 80 → HTTP GET em sequência e produz
 * um veredicto amigável.
 */
@Named
public class IsItDownService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(IsItDownService.class.getName());
    private static final int TCP_TIMEOUT_MS = 3000;
    private static final int HTTP_CONNECT_MS = 4000;
    private static final int HTTP_READ_MS = 5000;
    private static final String UA = "Mozilla/5.0 (compatible; nettools/1.0; +https://nettools.live)";

    public String executar(IsItDownTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        String host = null;
        try {
            String raw = input.getHost();
            if (raw == null) throw new IllegalArgumentException("host é obrigatório");
            raw = raw.trim();
            if (raw.startsWith("http://") || raw.startsWith("https://")) {
                try {
                    raw = new URL(raw).getHost();
                } catch (Exception e) {
                    throw new IllegalArgumentException("URL mal formada");
                }
            }
            host = HostValidator.requireHost(raw);

            sendLine(session, gson, input, "Checking " + host + " ...");

            // 1) DNS
            String resolvedIp = null;
            try {
                InetAddress addr = InetAddress.getByName(host);
                resolvedIp = addr.getHostAddress();
                sendLine(session, gson, input, "[ok]  DNS resolved → " + resolvedIp);
            } catch (Exception e) {
                sendLine(session, gson, input, "[FAIL] DNS resolution failed: " + e.getMessage());
                sendLine(session, gson, input, "VERDICT: DOWN — the domain does not resolve. Either the domain is misconfigured, expired, or the DNS entry is missing.");
                input.setVerdict("DOWN");
                sb.append("DOWN");
                return sb.toString();
            }

            // 2) TCP 443
            boolean tcp443 = tcpConnect(host, 443);
            sendLine(session, gson, input, (tcp443 ? "[ok]  " : "[FAIL] ") + "TCP 443 (HTTPS) " + (tcp443 ? "reachable" : "unreachable"));

            // 3) TCP 80
            boolean tcp80 = tcpConnect(host, 80);
            sendLine(session, gson, input, (tcp80 ? "[ok]  " : "[FAIL] ") + "TCP 80 (HTTP) " + (tcp80 ? "reachable" : "unreachable"));

            if (!tcp443 && !tcp80) {
                sendLine(session, gson, input, "VERDICT: DOWN — neither HTTPS nor HTTP is accepting connections. DNS works but the server isn't responding.");
                input.setVerdict("DOWN");
                sb.append("DOWN");
                return sb.toString();
            }

            // 4) HTTP GET
            String tryUrl = tcp443 ? "https://" + host + "/" : "http://" + host + "/";
            int status = -1;
            long ms = -1;
            try {
                URL u = new URL(tryUrl);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                InsecureSsl.apply(conn);
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(HTTP_CONNECT_MS);
                conn.setReadTimeout(HTTP_READ_MS);
                conn.setRequestProperty("User-Agent", UA);
                conn.setRequestMethod("GET");
                long start = System.nanoTime();
                status = conn.getResponseCode();
                ms = (System.nanoTime() - start) / 1_000_000L;
                conn.disconnect();
                sendLine(session, gson, input, "[ok]  HTTP GET " + tryUrl + " → " + status + " (" + ms + " ms)");
            } catch (Exception e) {
                sendLine(session, gson, input, "[FAIL] HTTP GET " + tryUrl + " → " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            String verdict;
            String human;
            if (status >= 200 && status < 400) {
                verdict = "UP";
                human = "UP — server reachable and responding with a healthy status code (" + status + ").";
            } else if (status >= 500) {
                verdict = "UP_WITH_ERRORS";
                human = "UP but serving errors — TCP and DNS are fine, but the application is returning HTTP " + status + ".";
            } else if (status >= 400) {
                verdict = "UP_WITH_ERRORS";
                human = "UP but restrictive — HTTP " + status + ". Content may require auth or the path doesn't exist at the root.";
            } else {
                verdict = "DOWN";
                human = "DOWN — the server accepts TCP connections but the HTTP layer is not responding.";
            }
            sendLine(session, gson, input, "VERDICT: " + human);
            input.setVerdict(verdict);
            sb.append(verdict);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em IsItDownService: {0}", ex.getMessage());
            sendLine(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em IsItDownService", e);
            sendLine(session, gson, input, "Error: " + e.getMessage());
        } finally {
            sendLine(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private boolean tcpConnect(String host, int port) {
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(host, port), TCP_TIMEOUT_MS);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    private void sendLine(Session session, Gson gson, IsItDownTO input, String line) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(gson.toJson(new IsItDownTO(input.getHost(), line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }
}
