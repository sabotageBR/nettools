package live.nettools.to;

import java.time.LocalDateTime;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import live.nettools.util.UtilString;

public class HttpStatusTO extends NettoolsTO {

    private static final long serialVersionUID = 5400112879734505572L;

    private String url;
    private String resultado;
    private LocalDateTime dateTime;

    public HttpStatusTO() {}

    public HttpStatusTO(String url, String resultado) {
        setUrl(url);
        setResultado(resultado);
    }

    public HttpStatusTO(String url, LocalDateTime dateTime) {
        setUrl(url);
        setDateTime(dateTime);
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getDateTimeFormat() {
        PrettyTime p = new PrettyTime(new Locale("pt"));
        return p.format(getDateTime());
    }

    public String getDateTimeOrder() {
        UtilString us = new UtilString();
        return String.valueOf(getDateTime().getYear()) +
                us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMonth().getValue()), 2) +
                us.completaComZerosAEsquerda(String.valueOf(getDateTime().getDayOfMonth()), 2) +
                us.completaComZerosAEsquerda(String.valueOf(getDateTime().getHour()), 2) +
                us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMinute()), 2) +
                us.completaComZerosAEsquerda(String.valueOf(getDateTime().getSecond()), 2);
    }
}
