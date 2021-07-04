package live.nettools.to;

import java.time.LocalDateTime;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import live.nettools.util.UtilString;

public class TracerouteTO extends NettoolsTO{

	private static final long serialVersionUID = 5400112879734505569L;

	private String host;
	private String resultado;
	private LocalDateTime dateTime;
	
	
	public TracerouteTO() {
		
	}
	public TracerouteTO(String host,String resultado) {
		setHost(host);
		setResultado(resultado);
	}
	
	public TracerouteTO(String host,LocalDateTime dateTime) {
		setHost(host);
		setDateTime(dateTime);
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getResultado() {
		return resultado;
	}
	public void setResultado(String resultado) {
		this.resultado = resultado;
	}
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	public String getDateTimeFormat() {
		PrettyTime p = new PrettyTime(new Locale("pt"));
		return p.format(getDateTime());
	}
	public String getDateTimeOrder() {
		UtilString us = new UtilString();
		return String.valueOf(getDateTime().getYear())+
				us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMonth().getValue()),2)+
				us.completaComZerosAEsquerda(String.valueOf(getDateTime().getDayOfMonth()),2)+
				us.completaComZerosAEsquerda(String.valueOf(getDateTime().getHour()),2)+
				us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMinute()),2)+
				us.completaComZerosAEsquerda(String.valueOf(getDateTime().getSecond()),2);
	}
	
	
}
