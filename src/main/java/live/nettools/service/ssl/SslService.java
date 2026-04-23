package live.nettools.service.ssl;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.SslTO;
import live.nettools.util.HostValidator;
import live.nettools.util.InsecureSsl;
import live.nettools.util.ModernTruststore;

/**
 * SSL/TLS Certificate Checker.
 *
 * Abre handshake TLS usando factory permissiva (para ser capaz de inspecionar
 * certificados auto-assinados ou com cadeia incompleta), extrai a chain,
 * mostra informações detalhadas e faz uma segunda validação usando o
 * TrustManager padrão do JDK para reportar se é confiável.
 *
 * Sem dependências externas — puro JDK.
 */
@Named
public class SslService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(SslService.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final long MS_PER_DAY = 24L * 60L * 60L * 1000L;

    public String executar(SslTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        SSLSocket sslSocket = null;
        try {
            String host = HostValidator.requireHost(input.getHost());
            int port = 443;
            if (input.getPorta() != null && !input.getPorta().trim().isEmpty()) {
                port = HostValidator.requirePort(input.getPorta());
            }

            line(session, gson, input, "Connecting to " + host + ":" + port + " ...");

            long start = System.nanoTime();
            SSLSocketFactory factory = InsecureSsl.trustAllSocketFactory();
            sslSocket = (SSLSocket) factory.createSocket();
            sslSocket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            sslSocket.setSoTimeout(READ_TIMEOUT_MS);

            // SNI: muitos servidores exigem o nome certo pro cert correto
            SSLParameters params = sslSocket.getSSLParameters();
            params.setServerNames(java.util.Collections.<SNIServerName>singletonList(new SNIHostName(host)));
            sslSocket.setSSLParameters(params);

            sslSocket.startHandshake();
            long handshakeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            SSLSession ss = sslSocket.getSession();
            line(session, gson, input, "Handshake OK in " + handshakeMs + " ms");
            line(session, gson, input, "  Protocol : " + ss.getProtocol());
            line(session, gson, input, "  Cipher   : " + ss.getCipherSuite());

            Certificate[] chain = ss.getPeerCertificates();
            if (chain == null || chain.length == 0) {
                line(session, gson, input, "No certificates received.");
                return "";
            }
            X509Certificate[] x509Chain = new X509Certificate[chain.length];
            for (int i = 0; i < chain.length; i++) {
                x509Chain[i] = (X509Certificate) chain[i];
            }

            line(session, gson, input, "");
            line(session, gson, input, "=== Leaf Certificate ===");
            X509Certificate leaf = x509Chain[0];
            printCert(session, gson, input, leaf, host);

            if (x509Chain.length > 1) {
                line(session, gson, input, "");
                line(session, gson, input, "=== Chain (" + x509Chain.length + " certificates) ===");
                for (int i = 0; i < x509Chain.length; i++) {
                    X509Certificate c = x509Chain[i];
                    String role = (i == 0) ? "leaf" : (i == x509Chain.length - 1 ? "root/intermediate" : "intermediate");
                    line(session, gson, input, "  [" + i + " " + role + "] " + c.getSubjectX500Principal().getName());
                    line(session, gson, input, "        issuer: " + c.getIssuerX500Principal().getName());
                }
            }

            line(session, gson, input, "");
            line(session, gson, input, "=== Trust ===");
            TrustResult tr = validateTrust(x509Chain);
            if (tr.trusted) {
                line(session, gson, input, "  Chain is TRUSTED by the default JDK truststore.");
            } else {
                line(session, gson, input, "  Chain is NOT TRUSTED: " + tr.message);
            }

            sb.append("OK");
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em SslService: {0}", ex.getMessage());
            line(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (javax.net.ssl.SSLHandshakeException hs) {
            LOG.log(Level.FINE, "SSL handshake failed", hs);
            line(session, gson, input, "Handshake failed: " + hs.getMessage());
        } catch (java.net.SocketTimeoutException st) {
            line(session, gson, input, "Timed out connecting to target (check host/port).");
        } catch (java.net.ConnectException ce) {
            line(session, gson, input, "Connection refused (is TLS listening on that port?)");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em SslService", e);
            line(session, gson, input, "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (sslSocket != null) {
                try { sslSocket.close(); } catch (Exception ignored) {}
            }
            line(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private void printCert(Session session, Gson gson, SslTO input, X509Certificate c, String hostname) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        line(session, gson, input, "  Subject       : " + c.getSubjectX500Principal().getName());
        line(session, gson, input, "  Issuer        : " + c.getIssuerX500Principal().getName());
        line(session, gson, input, "  Serial        : " + c.getSerialNumber().toString(16));
        line(session, gson, input, "  Valid from    : " + df.format(c.getNotBefore()));
        line(session, gson, input, "  Valid to      : " + df.format(c.getNotAfter()));

        long daysRemaining = (c.getNotAfter().getTime() - System.currentTimeMillis()) / MS_PER_DAY;
        String expiryTag = "";
        if (daysRemaining < 0) {
            expiryTag = " [EXPIRED " + Math.abs(daysRemaining) + " days ago]";
        } else if (daysRemaining < 30) {
            expiryTag = " [EXPIRES SOON — " + daysRemaining + " days]";
        } else {
            expiryTag = " (" + daysRemaining + " days remaining)";
        }
        line(session, gson, input, "  Days to expiry: " + daysRemaining + expiryTag);

        line(session, gson, input, "  Signature alg : " + c.getSigAlgName());

        try {
            String keyAlg = c.getPublicKey().getAlgorithm();
            String keySize = "";
            if (c.getPublicKey() instanceof java.security.interfaces.RSAPublicKey) {
                keySize = " " + ((java.security.interfaces.RSAPublicKey) c.getPublicKey()).getModulus().bitLength() + " bits";
            } else if (c.getPublicKey() instanceof java.security.interfaces.ECPublicKey) {
                keySize = " " + ((java.security.interfaces.ECPublicKey) c.getPublicKey()).getParams().getCurve().getField().getFieldSize() + " bits";
            }
            line(session, gson, input, "  Public key    : " + keyAlg + keySize);
        } catch (Exception ignored) {}

        // Subject Alternative Names
        try {
            Collection<List<?>> sans = c.getSubjectAlternativeNames();
            if (sans != null && !sans.isEmpty()) {
                StringBuilder sansLine = new StringBuilder();
                boolean matches = false;
                for (List<?> san : sans) {
                    if (san.size() >= 2) {
                        Object val = san.get(1);
                        String valStr = val == null ? "" : val.toString();
                        if (sansLine.length() > 0) sansLine.append(", ");
                        sansLine.append(valStr);
                        if (matchesHost(valStr, hostname)) matches = true;
                    }
                }
                line(session, gson, input, "  SANs          : " + sansLine);
                line(session, gson, input, "  Hostname match: " + (matches ? hostname + " ✓" : hostname + " ✗ (cert doesn't cover this hostname)"));
            }
        } catch (CertificateException ignored) {}
    }

    private boolean matchesHost(String pattern, String hostname) {
        if (pattern == null || hostname == null) return false;
        String p = pattern.toLowerCase();
        String h = hostname.toLowerCase();
        if (p.equals(h)) return true;
        if (p.startsWith("*.")) {
            String suffix = p.substring(1);
            if (h.endsWith(suffix)) {
                String head = h.substring(0, h.length() - suffix.length());
                return !head.contains(".") && !head.isEmpty();
            }
        }
        return false;
    }

    private TrustResult validateTrust(X509Certificate[] chain) {
        // Tenta primeiro o Mozilla bundle (roots modernas), depois o JDK default
        // (cobre CAs privadas instaladas manualmente no sistema).
        String firstFailure = null;
        X509TrustManager[] managers = new X509TrustManager[] {
            ModernTruststore.modern(), ModernTruststore.jdk()
        };
        for (X509TrustManager tm : managers) {
            if (tm == null) continue;
            for (String keyType : new String[] { "RSA", "ECDSA", "DSA", "EC" }) {
                try {
                    tm.checkServerTrusted(chain, keyType);
                    return new TrustResult(true, null);
                } catch (CertificateException e) {
                    if (firstFailure == null) firstFailure = e.getMessage();
                } catch (Exception e) {
                    if (firstFailure == null) firstFailure = e.getMessage();
                }
            }
        }
        return new TrustResult(false, firstFailure != null ? firstFailure : "no trust manager available");
    }

    private void line(Session session, Gson gson, SslTO input, String text) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(gson.toJson(new SslTO(input.getHost(), text)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }

    private static final class TrustResult {
        final boolean trusted;
        final String message;
        TrustResult(boolean t, String m) { this.trusted = t; this.message = m; }
    }

    @SuppressWarnings("unused") private static void touch(Date d) {}
}
