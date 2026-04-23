package live.nettools.util;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * SSL helper para ferramentas de diagnóstico que precisam acessar URLs
 * arbitrárias de terceiros (HttpStatusChecker, IsItDown, OgImageResolver).
 *
 * Desabilita validação de certificado e hostname verification — o comportamento
 * esperado para diagnóstico, equivalente ao <code>curl -k</code> /
 * <code>wget --no-check-certificate</code>. Sem isso, qualquer site com
 * cadeia de CA que a JVM instalada (especialmente Java 8 antigo) não conhece
 * retorna SSLHandshakeException antes de chegar ao HTTP.
 *
 * <b>NÃO</b> usar em chamadas para APIs confiáveis (Brave, SearXNG,
 * macvendors) — lá a validação TLS é importante.
 */
public final class InsecureSsl {

    private static final SSLSocketFactory TRUST_ALL_FACTORY;
    private static final HostnameVerifier TRUST_ALL_HOSTS = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
            return true;
        }
    };

    static {
        try {
            TrustManager[] trustAll = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new SecureRandom());
            TRUST_ALL_FACTORY = ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao configurar SSLContext permissivo", e);
        }
    }

    private InsecureSsl() {}

    /** Desabilita validação TLS numa conexão HTTPS. No-op em HTTP. */
    public static void apply(HttpURLConnection conn) {
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection) conn;
            https.setSSLSocketFactory(TRUST_ALL_FACTORY);
            https.setHostnameVerifier(TRUST_ALL_HOSTS);
        }
    }

    /**
     * Factory permissiva para uso direto (ex.: SslService precisa criar
     * SSLSocket manualmente para inspecionar cadeia mesmo quando a validação
     * padrão falharia).
     */
    public static SSLSocketFactory trustAllSocketFactory() {
        return TRUST_ALL_FACTORY;
    }
}
