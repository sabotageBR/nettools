package live.nettools.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Carrega e expõe dois X509TrustManagers:
 *   1) {@link #modern()} → baseado no bundle Mozilla
 *      (src/main/resources/cacerts-mozilla.pem) — cobre roots modernas
 *      (Let's Encrypt ISRG X1/X2, Google Trust Services novas, etc.) que
 *      JDKs antigos (Java 8 pre-8u131) podem não ter.
 *   2) {@link #jdk()} → truststore padrão do JDK — cobre CAs privadas que o
 *      admin adicionou manualmente ao sistema.
 *
 * Para julgar "trusted", passe em QUALQUER UM dos dois. Isso abrange cert
 * público moderno E cert interno corporativo.
 */
public final class ModernTruststore {

    private static final Logger LOG = Logger.getLogger(ModernTruststore.class.getName());
    private static final String BUNDLE_RESOURCE = "/cacerts-mozilla.pem";

    private static final X509TrustManager MODERN = loadModern();
    private static final X509TrustManager JDK = loadJdkDefault();

    private ModernTruststore() {}

    public static X509TrustManager modern() { return MODERN; }
    public static X509TrustManager jdk()    { return JDK; }

    private static X509TrustManager loadModern() {
        try (InputStream is = ModernTruststore.class.getResourceAsStream(BUNDLE_RESOURCE)) {
            if (is == null) {
                LOG.warning(BUNDLE_RESOURCE + " não encontrado no classpath; só usando JDK default truststore");
                return null;
            }
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            List<X509Certificate> certs = parsePem(is);
            for (int i = 0; i < certs.size(); i++) {
                ks.setCertificateEntry("mozilla-" + i, certs.get(i));
            }
            LOG.log(Level.INFO, "Carregadas {0} root CAs do Mozilla bundle", certs.size());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) return (X509TrustManager) tm;
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha ao carregar Mozilla truststore", e);
            return null;
        }
    }

    private static X509TrustManager loadJdkDefault() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) return (X509TrustManager) tm;
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha ao carregar JDK default truststore", e);
        }
        return null;
    }

    private static List<X509Certificate> parsePem(InputStream is) throws Exception {
        List<X509Certificate> out = new ArrayList<X509Certificate>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII))) {
            StringBuilder sb = null;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.equals("-----BEGIN CERTIFICATE-----")) {
                    sb = new StringBuilder();
                } else if (line.equals("-----END CERTIFICATE-----") && sb != null) {
                    byte[] der = Base64.getDecoder().decode(sb.toString());
                    try {
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
                        out.add(cert);
                    } catch (Exception e) {
                        LOG.log(Level.FINE, "Cert inválido no bundle, pulando", e);
                    }
                    sb = null;
                } else if (sb != null) {
                    sb.append(line);
                }
            }
        }
        return out;
    }
}
