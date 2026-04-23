package live.nettools.service.mac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.MacTO;
import live.nettools.util.HostValidator;

/**
 * MAC vendor lookup. Usa a API pública macvendors.com (free, sem chave).
 * Resposta é texto puro com o nome do fabricante (ou 404 se desconhecido).
 *
 * Cache LRU em memória (8k entradas, TTL 7 dias) — reduz hits repetidos à API.
 */
@Named
public class MacService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MacService.class.getName());
    private static final String ENDPOINT = "https://api.macvendors.com/";
    private static final int TIMEOUT_MS = 5000;
    private static final int CACHE_MAX = 8192;
    private static final long CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L;

    @SuppressWarnings("serial")
    private static final Map<String, Cached> CACHE = new LinkedHashMap<String, Cached>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Cached> e) { return size() > CACHE_MAX; }
    };

    public String executar(MacTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        String mac = null;
        try {
            mac = HostValidator.requireMac(input.getMac());
            input.setMac(mac);
            String oui = mac.substring(0, 8); // AA:BB:CC
            input.setOui(oui);

            sendLine(session, gson, input, "Looking up vendor for " + mac + " (OUI " + oui + ") ...");

            String vendor = resolve(oui);
            if (vendor == null || vendor.isEmpty()) {
                sendLine(session, gson, input, "No vendor found for OUI " + oui + ". Either the prefix is unallocated or the registry is stale.");
                input.setVendor(null);
            } else {
                input.setVendor(vendor);
                sendLine(session, gson, input, "Vendor: " + vendor);
                sb.append(vendor);
            }
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em MacService: {0}", ex.getMessage());
            sendLine(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em MacService", e);
            sendLine(session, gson, input, "Error: " + e.getMessage());
        } finally {
            sendLine(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private String resolve(String oui) {
        synchronized (CACHE) {
            Cached c = CACHE.get(oui);
            if (c != null && c.expiresAt > System.currentTimeMillis()) {
                return c.vendor;
            }
        }
        String vendor = fetch(oui);
        synchronized (CACHE) {
            CACHE.put(oui, new Cached(vendor, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return vendor;
    }

    private String fetch(String oui) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT + oui);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "text/plain");
            conn.setRequestProperty("User-Agent", "nettools/1.0");
            int status = conn.getResponseCode();
            if (status == 404) {
                return "";
            }
            if (status == 429) {
                LOG.warning("macvendors rate limit");
                return null;
            }
            if (status != 200) {
                LOG.log(Level.FINE, "macvendors HTTP {0}", status);
                return null;
            }
            return readFully(conn.getInputStream()).trim();
        } catch (IOException e) {
            LOG.log(Level.FINE, "macvendors falhou para " + oui, e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readFully(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buf = new char[1024];
            int n;
            while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    private void sendLine(Session session, Gson gson, MacTO input, String line) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(gson.toJson(new MacTO(input.getMac(), line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }

    private static final class Cached {
        final String vendor;
        final long expiresAt;
        Cached(String v, long e) { this.vendor = v; this.expiresAt = e; }
    }
}
