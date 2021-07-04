package live.nettools.service.snmp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.SnmpTO;

@Named
public class SnmpService implements Serializable{

	private static final long serialVersionUID = -6986454410309827919L;

	public String executar(SnmpTO snmp, Session session) {
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String s = null;
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		try {
			Runtime rt = Runtime.getRuntime();
			String command = String.format("snmpwalk -v %s -On -c %s %s:%s %s",snmp.getVersao(),snmp.getCommunity(),snmp.getHost(),snmp.getPorta(),snmp.getOid());
			proc = rt.exec(command);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
				if(session != null) {
					session.getBasicRemote().sendText(gson.toJson(new SnmpTO(snmp.getHost(), s)));
				}	
				sb.append(s);
			}
			
			while ((s = stdError.readLine()) != null) {
				if(session != null) {
					session.getBasicRemote().sendText(gson.toJson(new SnmpTO(snmp.getHost(), s)));
				}	
				sb.append(s);
			}
			if(session != null) {
				session.getBasicRemote().sendText(gson.toJson(new SnmpTO(snmp.getHost(), "FIM")));
			}	
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			try {
				Field f = proc.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				stdInput.close();
				stdError.close();
				proc.destroy();
				proc.destroyForcibly();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
}
