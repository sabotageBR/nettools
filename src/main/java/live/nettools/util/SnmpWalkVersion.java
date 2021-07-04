package live.nettools.util;

public enum SnmpWalkVersion {

	
	v2c("2c");
	
	
	private SnmpWalkVersion(String valor) {
		setValor(valor);
	}
	
	private String valor;
	
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
}
