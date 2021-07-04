package live.nettools.service.nmap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.to.NmapTO;

@Named
public class NmapService implements Serializable{

	private static final long serialVersionUID = -6986454410309827919L;

	public String executar(NmapTO nmap, Session session) {
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String s = null;
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		try {
			Runtime rt = Runtime.getRuntime();
			String command = "";
			command = String.format("nmap %s", nmap.getHost());
			proc = rt.exec(command);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
				if(session != null) {
					session.getBasicRemote().sendText(gson.toJson(new NmapTO(nmap.getHost(), s)));
				}	
				sb.append(s);
			}
			
			while ((s = stdError.readLine()) != null) {
				if(session != null) {
					session.getBasicRemote().sendText(gson.toJson(new NmapTO(nmap.getHost(), s)));
				}	
				sb.append(s);
			}
			if(session != null) {
				session.getBasicRemote().sendText(gson.toJson(new NmapTO(nmap.getHost(), "FIM")));
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
