package live.nettools.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SnmpWalk implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1522590058500975959L;

	public Map<String,String> executar(String community,String host,String porta, SnmpWalkVersion versao, String oid,boolean withId){
		Map<String, String> mapaRetorno = new HashMap<String, String>();
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		UtilString utilString = new UtilString();
		String chave = "";
		String chaveOriginal = "";
		String tipo = "";
		String valor = "";
		String mib = "";
		String s = null;
		try {
			Runtime rt = Runtime.getRuntime();
			String command = String.format("snmpwalk -v %s -On -c %s %s:%s %s",versao.getValor(),community,host,porta,oid);
//			System.out.println(command);
			proc = rt.exec(command);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			
			while ((s = stdInput.readLine()) != null) {
				try {
					//mib = s.substring(0, s.indexOf("::") - 1);
					if(s.indexOf("=") > 0) {
						chaveOriginal = s.substring(0, s.indexOf("=") - 1);
						chave = s.substring(0, s.indexOf("=") - 1);
					}	
					
					if(s.indexOf("=") > 0 && s.indexOf(": ") > 0) {
						tipo = s.substring(s.indexOf("=")+1, s.indexOf(": "));
					}
					if(s.indexOf(": ") > 0) {
						valor = s.substring(s.indexOf(": ")+2,s.length());
						valor = valor.replace("\"","");
					}
					if(chave.lastIndexOf(".") > 0) {
						chave = chave.substring(chave.lastIndexOf(".")+1,chave.length());
					}	
					
					if(!utilString.vazio(chave) && !utilString.vazio(valor)) {
						if(withId) {
							
							if(chave.indexOf(".") > 0) {
								chave = chave.substring(chave.indexOf(".")+1, chave.length());
							}
							if(mapaRetorno.get(chave) == null) {
								mapaRetorno.put(chave, valor);
							}
						}else {
							if(mapaRetorno.get(chaveOriginal) == null) {
								mapaRetorno.put(chaveOriginal, valor);
							}
						}
					}	
				}catch(Exception e) {
					e.printStackTrace();
				}	
			}
//			System.out.println(mapaRetorno);
//			while ((s = stdError.readLine()) != null) {
//			    System.out.println(s);
//			}
			
		} catch (Exception e) {
			System.out.println("VALOR DA LINHA ERRO:"+s);
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
		return mapaRetorno;
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