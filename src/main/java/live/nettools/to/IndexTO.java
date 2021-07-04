package live.nettools.to;

import java.util.ArrayList;
import java.util.List;

public class IndexTO extends NettoolsTO{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6787216954122034892L;
	private String ip;
	private String header;
	private List<HeaderTO> headers;
	private PingTO pingTO;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public List<HeaderTO> getHeaders() {
		if (headers == null) {
			headers = new ArrayList<HeaderTO>();
		}
		return headers;
	}

	public void setHeaders(List<HeaderTO> headers) {
		this.headers = headers;
	}

	public PingTO getPingTO() {
		if (pingTO == null) {
			pingTO = new PingTO();
		}
		return pingTO;
	}

	public void setPingTO(PingTO pingTO) {
		this.pingTO = pingTO;
	}
	

}
