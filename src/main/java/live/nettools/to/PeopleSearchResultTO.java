package live.nettools.to;

import java.io.Serializable;

import live.nettools.enums.PeoplesearchTypeEnum;

public class PeopleSearchResultTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1260648534621130930L;
	
	private String titulo;
	private String link;
	private String texto;
	private PeoplesearchTypeEnum tipo;
	
	public PeopleSearchResultTO() {
		
	}
	
	public PeopleSearchResultTO(String titulo, String texto) {
		setTitulo(titulo);
		setTexto(texto);
	}
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTexto() {
		if (texto == null) {
			texto = new String();
		}
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
	public PeoplesearchTypeEnum getTipo() {
		return tipo;
	}
	public void setTipo(PeoplesearchTypeEnum tipo) {
		this.tipo = tipo;
	}
	

}
