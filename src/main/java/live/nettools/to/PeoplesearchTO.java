package live.nettools.to;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import live.nettools.util.UtilString;

public class PeoplesearchTO extends NettoolsTO{

	private static final long serialVersionUID = 5400112879734505569L;

	private Integer posicao;
	private String nome;
	private String instagram;
	private String facebook;
	private String twitter;
	private String tiktok;
	private String erome;
	
	private PeopleSearchResultTO resultado;
	private List<PeopleSearchResultTO> resultados;
	
	
	private LocalDateTime dateTime;

	public PeoplesearchTO() {
		
	}
	public PeoplesearchTO(String nome,PeopleSearchResultTO resultado) {
		setNome(nome);
		setResultado(resultado);
	}
	
	public PeoplesearchTO(String nome, String instagram,String facebook,String twitter, LocalDateTime dateTime) {
		setNome(nome);
		setInstagram(instagram);
		setFacebook(facebook);
		setTwitter(twitter);
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
		if(getDateTime() != null) {
			return String.valueOf(getDateTime().getYear())+
					us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMonth().getValue()),2)+
					us.completaComZerosAEsquerda(String.valueOf(getDateTime().getDayOfMonth()),2)+
					us.completaComZerosAEsquerda(String.valueOf(getDateTime().getHour()),2)+
					us.completaComZerosAEsquerda(String.valueOf(getDateTime().getMinute()),2)+
					us.completaComZerosAEsquerda(String.valueOf(getDateTime().getSecond()),2);
		}
		return "";
	}
	
	public String getInstagram() {
		return instagram;
	}
	public void setInstagram(String instagram) {
		this.instagram = instagram;
	}
	public String getFacebook() {
		return facebook;
	}
	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}
	public String getTwitter() {
		return twitter;
	}
	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public PeopleSearchResultTO getResultado() {
		return resultado;
	}
	public void setResultado(PeopleSearchResultTO resultado) {
		this.resultado = resultado;
	}
	public List<PeopleSearchResultTO> getResultados() {
		return resultados;
	}
	public void setResultados(List<PeopleSearchResultTO> resultados) {
		this.resultados = resultados;
	}
	public Integer getPosicao() {
		return posicao;
	}
	public void setPosicao(Integer posicao) {
		this.posicao = posicao;
	}
	public String getTiktok() {
		return tiktok;
	}
	public void setTiktok(String tiktok) {
		this.tiktok = tiktok;
	}
	public String getErome() {
		return erome;
	}
	public void setErome(String erome) {
		this.erome = erome;
	}
}
