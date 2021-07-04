package live.nettools.to;

import java.io.Serializable;

public class NettoolsTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -423589529053585425L;
	private String ipExterno;

	public String getIpExterno() {
		return ipExterno;
	}

	public void setIpExterno(String ipExterno) {
		this.ipExterno = ipExterno;
	}
}
