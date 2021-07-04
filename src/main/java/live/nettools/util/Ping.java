package live.nettools.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.faces.context.FacesContext;

public class Ping implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2522602327767840978L;

	public String executar(String host){
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String s = null;
		StringBuilder sb = new StringBuilder();
		try {
			Runtime rt = Runtime.getRuntime();
			String command = String.format("ping %s -c 20",host);
			
			proc = rt.exec(command);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
				FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("concatPingResult('"+s+"')");
				sb.append(s);
			}
		} catch (Exception e) {
		}finally {
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
	
	public static int indexOf(String str, String search, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = str.length() + fromIndex; // convert the negative index to a positive index, treating the negative index -1 as the index of the last character
            int index = str.lastIndexOf(search, fromIndex);
            if (index == -1) {
                index = Integer.MIN_VALUE; // String.indexOf normally returns -1 if the character is not found, but we need to define a new contract since -1 is a valid index for our new implementation
            }
            else {
                index = -(str.length() - index); // convert the result to a negative index--again, -1 is the index of the last character 
            }
            return index;
        }
        else {
            return str.indexOf(str, fromIndex);
        }
    }
 
}