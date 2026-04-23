package live.nettools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Cliente simples para ip-api.com (free tier: 45 req/min por IP de origem,
 * sem chave). Cache em memória com TTL de 1h — o geo de um IP não muda no
 * curto prazo.
 *
 * Endpoint: http://ip-api.com/json/<ip>?fields=...
 * HTTPS só é oferecido no tier pago; como a chamada é server-side, HTTP serve.
 */
public final class IpApiClient {

    private static final Logger LOG = Logger.getLogger(IpApiClient.class.getName());
    private static final String ENDPOINT = "http://ip-api.com/json/";
    private static final String FIELDS = "status,message,country,countryCode,region,regionName,"
            + "city,zip,lat,lon,timezone,isp,org,as,reverse,mobile,proxy,hosting,query";
    private static final int TIMEOUT_MS = 5000;
    private static final long CACHE_TTL_MS = 60L * 60L * 1000L;
    private static final int CACHE_MAX = 1024;

    @SuppressWarnings("serial")
    private static final Map<String, Cached> CACHE = new LinkedHashMap<String, Cached>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Cached> eldest) {
            return size() > CACHE_MAX;
        }
    };

    private IpApiClient() {}

    public static Result lookup(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        synchronized (CACHE) {
            Cached c = CACHE.get(ip);
            if (c != null && c.expiresAt > System.currentTimeMillis()) {
                return c.result;
            }
        }
        Result r = fetch(ip);
        synchronized (CACHE) {
            CACHE.put(ip, new Cached(r, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return r;
    }

    private static Result fetch(String ip) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT + ip + "?fields=" + FIELDS);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            int status = conn.getResponseCode();
            if (status != 200) {
                LOG.log(Level.FINE, "ip-api HTTP {0}", status);
                return null;
            }
            String body = readFully(conn.getInputStream());
            JsonElement root = JsonParser.parseString(body);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonObject o = root.getAsJsonObject();
            if (!"success".equals(str(o, "status"))) {
                return null;
            }
            Result r = new Result();
            r.ip = str(o, "query");
            r.country = str(o, "country");
            r.countryCode = str(o, "countryCode");
            r.region = str(o, "regionName");
            r.city = str(o, "city");
            r.postal = str(o, "zip");
            r.timezone = str(o, "timezone");
            r.isp = str(o, "isp");
            r.org = str(o, "org");
            r.asn = str(o, "as");
            r.reverse = str(o, "reverse");
            r.lat = o.has("lat") && !o.get("lat").isJsonNull() ? o.get("lat").getAsDouble() : null;
            r.lon = o.has("lon") && !o.get("lon").isJsonNull() ? o.get("lon").getAsDouble() : null;
            r.proxy = o.has("proxy") && o.get("proxy").getAsBoolean();
            r.hosting = o.has("hosting") && o.get("hosting").getAsBoolean();
            r.mobile = o.has("mobile") && o.get("mobile").getAsBoolean();
            return r;
        } catch (IOException e) {
            LOG.log(Level.FINE, "Falha ip-api lookup para " + ip, e);
            return null;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Erro inesperado ip-api", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private static String readFully(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    public static final class Result {
        public String ip;
        public String country;
        public String countryCode;
        public String region;
        public String city;
        public String postal;
        public String timezone;
        public String isp;
        public String org;
        public String asn;
        public String reverse;
        public Double lat;
        public Double lon;
        public boolean proxy;
        public boolean hosting;
        public boolean mobile;
    }

    private static final class Cached {
        final Result result;
        final long expiresAt;
        Cached(Result r, long e) { this.result = r; this.expiresAt = e; }
    }
}
