package live.nettools.service.sip;

import java.io.Serializable;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.SipTO;

@Named
public class SipService implements Serializable{

	private static final long serialVersionUID = -6986454410309827919L;

	public String executar(SipTO sip, Session session) {
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		try {
			String result = CheckSipUdp.checkSipUdp(sip.getHost(), Integer.valueOf(sip.getPorta()), null);
			if(session != null) {
				session.getBasicRemote().sendText(gson.toJson(new SipTO(sip.getHost(), result )));
				session.getBasicRemote().sendText(gson.toJson(new SipTO(sip.getHost(), "FIM")));
			}
			sb.append(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
