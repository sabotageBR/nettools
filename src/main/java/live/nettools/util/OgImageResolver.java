package live.nettools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolve a imagem principal (og:image / twitter:image / link rel=image_src)
 * de uma URL. Faz download parcial da página, extrai a meta tag via regex e
 * cacheia em memória por 1h para baratear buscas repetidas.
 *
 * Uso típico: {@link #resolveMany(Collection)} executa N resoluções em
 * paralelo com timeout global, devolvendo um mapa url→imagem.
 */
public final class OgImageResolver {

    private static final Logger LOG = Logger.getLogger(OgImageResolver.class.getName());

    private static final int MAX_PAGE_BYTES = 256 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final long CACHE_TTL_MS = 60L * 60L * 1000L;
    private static final int CACHE_MAX = 2048;
    private static final int PARALLELISM = 6;
    private static final long BATCH_TIMEOUT_SECONDS = 12L;

    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
          + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** og:image / twitter:image / og:image:secure_url — atributo content capturado no grupo 1. */
    private static final Pattern META_IMG = Pattern.compile(
            "<meta[^>]+(?:property|name)\\s*=\\s*[\"'](?:og:image(?::secure_url|:url)?|twitter:image(?::src)?)[\"'][^>]*?content\\s*=\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    /** forma inversa: content=... antes de property/name. */
    private static final Pattern META_IMG_REVERSE = Pattern.compile(
            "<meta[^>]+content\\s*=\\s*[\"']([^\"']+)[\"'][^>]+(?:property|name)\\s*=\\s*[\"'](?:og:image(?::secure_url|:url)?|twitter:image(?::src)?)[\"']",
            Pattern.CASE_INSENSITIVE);
    /** fallback: <link rel="image_src" href="..."/>. */
    private static final Pattern LINK_IMG = Pattern.compile(
            "<link[^>]+rel\\s*=\\s*[\"']image_src[\"'][^>]*?href\\s*=\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);

    private static final Map<String, CachedImage> CACHE = new LinkedHashMap<String, CachedImage>(16, 0.75f, true) {
        private static final long serialVersionUID = 1L;
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedImage> eldest) {
            return size() > CACHE_MAX;
        }
    };

    private static final ExecutorService POOL = Executors.newFixedThreadPool(PARALLELISM, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "OgImageResolver");
            t.setDaemon(true);
            return t;
        }
    });

    private OgImageResolver() {}

    public static String resolve(String pageUrl) {
        if (pageUrl == null || pageUrl.isEmpty()) {
            return null;
        }
        CachedImage c;
        synchronized (CACHE) {
            c = CACHE.get(pageUrl);
        }
        if (c != null && c.expiresAt > System.currentTimeMillis()) {
            return c.image;
        }
        String resolved = fetchAndExtract(pageUrl);
        synchronized (CACHE) {
            CACHE.put(pageUrl, new CachedImage(resolved, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return resolved;
    }

    /**
     * Variante assíncrona: dispara em pool diferente do pool interno (evita deadlock),
     * chama {@code onImage.accept(pageUrl, imageUrl)} para cada imagem resolvida com
     * sucesso. URLs sem imagem não disparam callback.
     */
    public static void resolveManyAsync(final Collection<String> urls,
                                        final BiConsumer<String, String> onImage) {
        if (urls == null || urls.isEmpty() || onImage == null) {
            return;
        }
        ForkJoinPool.commonPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> images = resolveMany(urls);
                    for (Map.Entry<String, String> e : images.entrySet()) {
                        if (e.getValue() != null && !e.getValue().isEmpty()) {
                            try {
                                onImage.accept(e.getKey(), e.getValue());
                            } catch (Exception inner) {
                                LOG.log(Level.FINE, "Callback de imagem falhou", inner);
                            }
                        }
                    }
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Resolução assíncrona falhou", ex);
                }
            }
        });
    }

    public static Map<String, String> resolveMany(Collection<String> urls) {
        Map<String, String> out = new HashMap<String, String>();
        if (urls == null || urls.isEmpty()) {
            return out;
        }
        Map<String, Future<String>> futures = new LinkedHashMap<String, Future<String>>();
        for (final String u : urls) {
            if (u == null || u.isEmpty() || futures.containsKey(u)) {
                continue;
            }
            futures.put(u, POOL.submit(new Callable<String>() {
                @Override
                public String call() {
                    return resolve(u);
                }
            }));
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(BATCH_TIMEOUT_SECONDS);
        for (Map.Entry<String, Future<String>> e : futures.entrySet()) {
            long remaining = deadline - System.nanoTime();
            try {
                if (remaining <= 0) {
                    e.getValue().cancel(true);
                    continue;
                }
                String img = e.getValue().get(remaining, TimeUnit.NANOSECONDS);
                if (img != null && !img.isEmpty()) {
                    out.put(e.getKey(), img);
                }
            } catch (Exception ex) {
                e.getValue().cancel(true);
            }
        }
        return out;
    }

    private static String fetchAndExtract(String pageUrl) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(pageUrl);
            if (!"http".equalsIgnoreCase(u.getProtocol()) && !"https".equalsIgnoreCase(u.getProtocol())) {
                return null;
            }
            conn = (HttpURLConnection) u.openConnection();
            InsecureSsl.apply(conn);
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", UA);
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,pt-BR;q=0.6");
            int status = conn.getResponseCode();
            if (status < 200 || status >= 400) {
                return null;
            }
            String ct = conn.getContentType();
            if (ct != null && !ct.toLowerCase().contains("html")) {
                return null;
            }
            String html = readBounded(conn.getInputStream(), MAX_PAGE_BYTES);
            if (html == null || html.isEmpty()) {
                return null;
            }
            String raw = firstMatch(META_IMG, html);
            if (raw == null) {
                raw = firstMatch(META_IMG_REVERSE, html);
            }
            if (raw == null) {
                raw = firstMatch(LINK_IMG, html);
            }
            if (raw == null) {
                return null;
            }
            return absolutize(u, decodeEntities(raw.trim()));
        } catch (IOException e) {
            LOG.log(Level.FINE, "Falha resolvendo og:image de " + pageUrl, e);
            return null;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Erro inesperado em " + pageUrl, e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String firstMatch(Pattern p, String html) {
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String readBounded(InputStream is, int maxBytes) throws IOException {
        char[] buf = new char[4096];
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            int total = 0;
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
                total += n;
                if (total >= maxBytes) {
                    break;
                }
                if (sb.indexOf("</head>") >= 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private static String absolutize(URL base, String maybeRelative) {
        if (maybeRelative == null) {
            return null;
        }
        try {
            URI uri = URI.create(maybeRelative);
            if (uri.isAbsolute()) {
                return maybeRelative;
            }
            URI resolved = base.toURI().resolve(uri);
            return resolved.toString();
        } catch (Exception e) {
            return maybeRelative;
        }
    }

    private static String decodeEntities(String s) {
        if (s == null || s.indexOf('&') < 0) {
            return s;
        }
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'");
    }

    private static final class CachedImage {
        final String image;
        final long expiresAt;
        CachedImage(String image, long expiresAt) {
            this.image = image;
            this.expiresAt = expiresAt;
        }
    }

    /** Suporte a testes — permite inspeção pontual, não faz parte da API pública. */
    static String extractFromHtmlForTest(String html) {
        String raw = firstMatch(META_IMG, html);
        if (raw == null) {
            raw = firstMatch(META_IMG_REVERSE, html);
        }
        if (raw == null) {
            raw = firstMatch(LINK_IMG, html);
        }
        return raw == null ? null : decodeEntities(raw.trim());
    }

    // keep List import used (for future use if extending); silence import-warning from IDE
    @SuppressWarnings("unused")
    private static void touchList(List<String> ignored) {}
}
