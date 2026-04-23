package live.nettools.to;

import java.time.LocalDateTime;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import live.nettools.util.UtilString;

public class MacTO extends NettoolsTO {

    private static final long serialVersionUID = 5400112879734505571L;

    private String mac;
    private String vendor;
    private String oui;
    private String resultado;
    private LocalDateTime dateTime;

    public MacTO() {}

    public MacTO(String mac, String resultado) {
        setMac(mac);
        setResultado(resultado);
    }

    public MacTO(String mac, LocalDateTime dateTime) {
        setMac(mac);
        setDateTime(dateTime);
    }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getOui() { return oui; }
    public void setOui(String oui) { this.oui = oui; }
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
