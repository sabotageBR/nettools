package live.nettools.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Valida entradas de usuário antes de passar para ProcessBuilder.
 * Recusa qualquer string que contenha metacaracteres de shell mesmo quando
 * o ProcessBuilder já protege contra injeção: defesa em profundidade e
 * defesa contra flags injetadas na posição do host (ex.: "-oProxyCommand=...").
 */
public final class HostValidator {

    private static final Pattern HOSTNAME = Pattern.compile(
            "^(?=.{1,253}$)([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");
    private static final Pattern IPV6 = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$|^([0-9a-fA-F]{1,4}:){1,7}:$|^:(:[0-9a-fA-F]{1,4}){1,7}$");
    private static final Pattern DIGITS = Pattern.compile("^\\d{1,6}$");
    private static final Pattern OID = Pattern.compile("^\\.?\\d+(\\.\\d+)*$");
    private static final Pattern SNMP_COMMUNITY = Pattern.compile("^[A-Za-z0-9_\\-]{1,64}$");
    private static final Pattern SNMP_VERSION = Pattern.compile("^(1|2c|3)$");
    private static final Pattern FREE_TEXT = Pattern.compile("^[\\p{L}\\p{N} ._\\-@]{1,120}$");

    private HostValidator() {}

    public static String requireHost(String value) {
        String v = trimOrFail(value, "host");
        if (v.startsWith("-")) {
            throw new IllegalArgumentException("host não pode começar com '-'");
        }
        if (HOSTNAME.matcher(v).matches() || IPV4.matcher(v).matches() || IPV6.matcher(v).matches()) {
            return v;
        }
        throw new IllegalArgumentException("host inválido");
    }

    public static String requireIp(String value) {
        String v = trimOrFail(value, "ip");
        if (IPV4.matcher(v).matches() || IPV6.matcher(v).matches()) {
            return v;
        }
        throw new IllegalArgumentException("ip inválido");
    }

    /**
     * Aceita MAC em qualquer formato (AA:BB:CC:DD:EE:FF, aa-bb-cc-dd-ee-ff,
     * AABBCCDDEEFF, AABB.CCDD.EEFF) e devolve a versão normalizada
     * AA:BB:CC:DD:EE:FF (uppercase, separadores de dois-pontos).
     */
    public static String requireMac(String value) {
        String v = trimOrFail(value, "mac");
        String hex = v.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
        if (hex.length() != 12) {
            throw new IllegalArgumentException("mac inválido (precisa de 12 hex digits)");
        }
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 12; i += 2) {
            if (i > 0) sb.append(':');
            sb.append(hex.charAt(i)).append(hex.charAt(i + 1));
        }
        return sb.toString();
    }

    public static String requireUrl(String value, String... allowedSchemes) {
        String v = trimOrFail(value, "url");
        try {
            URI uri = new URI(v);
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException("url sem scheme");
            }
            boolean allowed = false;
            for (String s : allowedSchemes) {
                if (s.equalsIgnoreCase(scheme)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new IllegalArgumentException("scheme não permitido: " + scheme);
            }
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("url sem host");
            }
            requireHost(uri.getHost());
            return v;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("url mal formada", e);
        }
    }

    public static int requirePort(String value) {
        String v = trimOrFail(value, "porta");
        if (!DIGITS.matcher(v).matches()) {
            throw new IllegalArgumentException("porta deve ser numérica");
        }
        int port = Integer.parseInt(v);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("porta fora do intervalo 1..65535");
        }
        return port;
    }

    public static int requirePositiveInt(String value, String field, int max) {
        String v = trimOrFail(value, field);
        if (!DIGITS.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " deve ser numérico");
        }
        int n = Integer.parseInt(v);
        if (n < 1 || n > max) {
            throw new IllegalArgumentException(field + " fora do intervalo 1.." + max);
        }
        return n;
    }

    public static String requireOid(String value) {
        String v = trimOrFail(value, "oid");
        if (!OID.matcher(v).matches()) {
            throw new IllegalArgumentException("oid inválido");
        }
        return v;
    }

    public static String requireSnmpCommunity(String value) {
        String v = trimOrFail(value, "community");
        if (!SNMP_COMMUNITY.matcher(v).matches()) {
            throw new IllegalArgumentException("community inválida");
        }
        return v;
    }

    public static String requireSnmpVersion(String value) {
        String v = trimOrFail(value, "versao");
        if (!SNMP_VERSION.matcher(v).matches()) {
            throw new IllegalArgumentException("versao SNMP inválida");
        }
        return v;
    }

    public static String requireFreeText(String value, String field) {
        String v = trimOrFail(value, field);
        if (!FREE_TEXT.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " contém caracteres não permitidos");
        }
        return v;
    }

    public static String optionalFreeText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return requireFreeText(value, field);
    }

    private static String trimOrFail(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " é obrigatório");
        }
        String v = value.trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException(field + " é obrigatório");
        }
        return v;
    }
}
