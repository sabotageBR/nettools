package live.nettools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Cliente para SearXNG — metasearch engine self-hosted e livre
 * (https://github.com/searxng/searxng). Agrega resultados de Google, Bing,
 * DuckDuckGo, Brave, Qwant, Mojeek, etc. a partir do SEU servidor, sem chave,
 * sem cota e sem rate limit imposto por terceiros.
 *
 * URL configurável via variável de ambiente {@code SEARXNG_URL}
 * (default: {@code http://localhost:8888}).
 *
 * A instância precisa ter o formato JSON habilitado em settings.yml:
 * <pre>
 * search:
 *   formats:
 *     - html
 *     - json
 * </pre>
 *
 * Rodando local:
 * <pre>
 * docker run -d --name searxng -p 8888:8080 \
 *   -v /path/to/settings.yml:/etc/searxng/settings.yml \
 *   searxng/searxng
 * </pre>
 */
public final class SearxngClient {

    private static final String ENV_URL = "SEARXNG_URL";
    private static final String DEFAULT_URL = "http://localhost:8888";
    private static final int TIMEOUT_MS = 15000;

    private SearxngClient() {}

    public static String baseUrl() {
        String env = System.getenv(ENV_URL);
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_URL;
    }

    public static List<SearchResult> search(String query, int max) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<SearchResult>();
        }
        String url = baseUrl().replaceAll("/+$", "") + "/search?q="
                + URLEncoder.encode(query.trim(), "UTF-8") + "&format=json";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "nettools/1.0");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            int status = conn.getResponseCode();
            if (status != 200) {
                String err = readFully(conn.getErrorStream());
                throw new IOException("SearXNG retornou HTTP " + status + ": " + truncate(err, 200));
            }
            String body = readFully(conn.getInputStream());
            return parse(body, max);
        } finally {
            conn.disconnect();
        }
    }

    static List<SearchResult> parse(String body, int max) {
        List<SearchResult> out = new ArrayList<SearchResult>();
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonObject()) {
            return out;
        }
        JsonObject obj = root.getAsJsonObject();
        if (!obj.has("results") || !obj.get("results").isJsonArray()) {
            return out;
        }
        JsonArray arr = obj.getAsJsonArray("results");
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject r = el.getAsJsonObject();
            SearchResult sr = new SearchResult();
            sr.title = stringOr(r, "title", "");
            sr.url = stringOr(r, "url", "");
            sr.snippet = stringOr(r, "content", "");
            if (sr.title.isEmpty() || sr.url.isEmpty()) {
                continue;
            }
            out.add(sr);
            if (out.size() >= max) {
                break;
            }
        }
        return out;
    }

    private static String stringOr(JsonObject o, String k, String def) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : def;
    }

    private static String readFully(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public static final class SearchResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public String title;
        public String url;
        public String snippet;
    }
}
