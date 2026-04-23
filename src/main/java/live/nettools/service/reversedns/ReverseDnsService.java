package live.nettools.service.reversedns;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.ReverseDnsTO;
import live.nettools.util.HostValidator;

/**
 * Reverse DNS: dado um IP, devolve o hostname publicado em PTR.
 *
 * Usa o resolver DNS do sistema via JNDI (in-arpa) para fazer o lookup
 * explícito em vez de depender do cache do InetAddress. Pure Java — zero
 * shell, zero dependências externas.
 */
@Named
public class ReverseDnsService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ReverseDnsService.class.getName());

    public String executar(ReverseDnsTO input, Session session) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        try {
            String ip = HostValidator.requireIp(input.getIp());
            sendLine(session, gson, input, "Looking up PTR for " + ip + " ...");

            String hostname = resolvePtr(ip);
            String message;
            if (hostname == null || hostname.isEmpty() || hostname.equals(ip)) {
                message = "No PTR record found for " + ip;
                input.setHostname(null);
            } else {
                message = ip + " → " + hostname;
                input.setHostname(hostname);
            }
            sendLine(session, gson, input, message);
            sb.append(message);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em ReverseDnsService: {0}", ex.getMessage());
            sendLine(session, gson, input, "Invalid input: " + ex.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha em ReverseDnsService", e);
            sendLine(session, gson, input, "Error: " + e.getMessage());
        } finally {
            sendLine(session, gson, input, "FIM");
        }
        return sb.toString();
    }

    private String resolvePtr(String ip) {
        String arpa = toArpa(ip);
        if (arpa == null) {
            return fallbackPtr(ip);
        }
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "3000");
            env.put("com.sun.jndi.dns.timeout.retries", "2");
            DirContext ctx = new InitialDirContext(env);
            try {
                Attributes attrs = ctx.getAttributes(arpa, new String[] { "PTR" });
                Attribute ptr = attrs.get("PTR");
                if (ptr != null && ptr.size() > 0) {
                    String value = ptr.get(0).toString();
                    if (value.endsWith(".")) {
                        value = value.substring(0, value.length() - 1);
                    }
                    return value;
                }
            } finally {
                ctx.close();
            }
        } catch (NamingException ne) {
            LOG.log(Level.FINE, "JNDI PTR falhou; caindo pra InetAddress", ne);
            return fallbackPtr(ip);
        }
        return null;
    }

    private String fallbackPtr(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            String host = addr.getCanonicalHostName();
            if (host == null || host.equals(ip)) {
                return null;
            }
            return host;
        } catch (Exception e) {
            return null;
        }
    }

    static String toArpa(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        if (ip.contains(":")) {
            // IPv6 — expand and reverse nibbles into ip6.arpa
            try {
                byte[] raw = InetAddress.getByName(ip).getAddress();
                if (raw.length != 16) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 15; i >= 0; i--) {
                    int b = raw[i] & 0xFF;
                    sb.append(Integer.toHexString(b & 0x0F)).append('.');
                    sb.append(Integer.toHexString((b >> 4) & 0x0F)).append('.');
                }
                sb.append("ip6.arpa");
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return null;
        }
        return parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0] + ".in-addr.arpa";
    }

    private void sendLine(Session session, Gson gson, ReverseDnsTO input, String line) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.getBasicRemote().sendText(gson.toJson(new ReverseDnsTO(input.getIp(), line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar WS", e);
        }
    }
}
