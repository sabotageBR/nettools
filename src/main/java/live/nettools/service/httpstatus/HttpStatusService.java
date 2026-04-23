package live.nettools.service.httpstatus;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.HttpStatusTO;
import live.nettools.util.HostValidator;
import live.nettools.util.InsecureSsl;

/**
 * HTTP Status & Redirect Chain Checker.
 * Faz GET sem seguir redirects automaticamente, emite cada hop (status + URL)
 * e para quando encontrar 2xx/4xx/5xx ou atingir o limite de redirects.
 */
@Named
public class HttpStatusService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(HttpStatusService.class.getName());
    private static final int MAX_HOPS = 10;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final String UA = "Mozilla/5.0 (compatible; nettools/1.0; +https://nettools.live)";

    public String executar(HttpStatusTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        String url = input.getUrl();
        try {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("url é obrigatória");
            }
            url = url.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            HostValidator.requireUrl(url, "http", "https");

            long totalStart = System.nanoTime();
            for (int hop = 1; hop <= MAX_HOPS; hop++) {
                HttpURLConnection conn = null;
                try {
                    URL u = new URL(url);
                    conn = (HttpURLConnection) u.openConnection();
                    InsecureSsl.apply(conn);
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                    conn.setReadTimeout(READ_TIMEOUT_MS);
                    conn.setRequestProperty("User-Agent", UA);
                    conn.setRequestProperty("Accept", "*/*");
                    conn.setRequestMethod("GET");

                    long start = System.nanoTime();
                    int status = conn.getResponseCode();
                    long hopMs = (System.nanoTime() - start) / 1_000_000L;
                    String statusMsg = conn.getResponseMessage();

                    sendLine(session, gson, input, "[" + hop + "] " + status + " " + (statusMsg == null ? "" : statusMsg) + "  " + url + "  (" + hopMs + " ms)");

                    if (status >= 300 && status < 400) {
                        String loc = conn.getHeaderField("Location");
                        if (loc == null || loc.isEmpty()) {
                            sendLine(session, gson, input, "Redirect without Location header; stopping.");
                            break;
                        }
                        sendLine(session, gson, input, "    Location: " + loc);
                        try {
                            URI next = URI.create(loc);
                            if (!next.isAbsolute()) {
                                next = u.toURI().resolve(next);
                            }
                            url = next.toString();
                        } catch (Exception bad) {
                            sendLine(session, gson, input, "Invalid redirect target; stopping.");
                            break;
                        }
                        continue;
                    }
                    // final response
                    sendLine(session, gson, input, "    Content-Type: " + stringOr(conn.getHeaderField("Content-Type")));
                    sendLine(session, gson, input, "    Content-Length: " + stringOr(conn.getHeaderField("Content-Length")));
                    sendLine(session, gson, input, "    Server: " + stringOr(conn.getHeaderField("Server")));
                    long totalMs = (System.nanoTime() - totalStart) / 1_000_000L;
                    sendLine(session, gson, input, "Final status: " + status + " · total " + totalMs + " ms · " + hop + (hop == 1 ? " hop" : " hops"));
                    sb.append(status);
                    break;
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em HttpStatusService: {0}", ex.getMessage());
            sendLine(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em HttpStatusService", e);
            sendLine(session, gson, input, "Error: " + e.getMessage());
        } finally {
            sendLine(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private String stringOr(String v) { return v == null ? "(none)" : v; }

    private void sendLine(Session session, Gson gson, HttpStatusTO input, String line) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(gson.toJson(new HttpStatusTO(input.getUrl(), line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }
}
