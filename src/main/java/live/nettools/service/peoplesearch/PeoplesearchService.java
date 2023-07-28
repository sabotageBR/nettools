package live.nettools.service.peoplesearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.enums.PeoplesearchTypeEnum;
import live.nettools.to.PeopleSearchResultTO;
import live.nettools.to.PeoplesearchTO;
import live.nettools.util.UtilString;

@Named
public class PeoplesearchService implements Serializable{

	private static final long serialVersionUID = -6986454410309827919L;

	private boolean execute = true;
	
	@SuppressWarnings("static-access")
	public List<PeopleSearchResultTO> executar(PeoplesearchTO peoplesearch, Session session) {
		
		UtilString us = new UtilString();
		List<PeopleSearchResultTO> resultados = new ArrayList<PeopleSearchResultTO>();
		try {
			
			if(!us.vazio(peoplesearch.getNome())) {
				enviarStatus(session, "Global search...");
				String comandoGlobal = String.format("googler -n 100 \"%s\" --unfilter --noprompt", peoplesearch.getNome());
				System.out.println("ComandoGlobal.: "+comandoGlobal);
				if(execute) {
					executarComando(comandoGlobal,PeoplesearchTypeEnum.GLOBAL,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
			}
			if(!us.vazio(peoplesearch.getFacebook())) {
				enviarStatus(session, "Facebook search...");
				String comandoGlobal = String.format("googler -n 100 %s --unfilter --noprompt", peoplesearch.getFacebook());
				System.out.println("ComandoGlobal.: "+comandoGlobal);
				if(execute) {
					executarComando(comandoGlobal,PeoplesearchTypeEnum.GLOBAL,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
				String comandoFacebook =String.format("googler -n 100 -w facebook.com %s --unfilter --noprompt", peoplesearch.getFacebook());
				System.out.println("Facebook.: "+comandoFacebook);
				if(execute) {
					executarComando(comandoFacebook,PeoplesearchTypeEnum.FACEBOOK,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
			}
			
			if(!us.vazio(peoplesearch.getInstagram())) {
				enviarStatus(session, "Instagram search...");
				String comandoGlobal = String.format("googler -n 100 @%s --unfilter --noprompt", peoplesearch.getInstagram());
				System.out.println("ComandoGlobal.: "+comandoGlobal);
				if(execute) {
					executarComando(comandoGlobal,PeoplesearchTypeEnum.GLOBAL,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
				String comandoInstagram = String.format("googler -n 100 -w instagram.com @%s --unfilter --noprompt", peoplesearch.getInstagram());
				System.out.println("Instagram: "+comandoInstagram);
				if(execute) {
					executarComando(comandoInstagram,PeoplesearchTypeEnum.INSTAGRAM,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
			}
			
			
			
			if(!us.vazio(peoplesearch.getTwitter())) {
				enviarStatus(session, "Twitter search...");
				String comandoGlobal = String.format("googler -n 100 %s --unfilter --noprompt", peoplesearch.getTwitter());
				System.out.println("ComandoGlobal.: "+comandoGlobal);
				if(execute) {
					executarComando(comandoGlobal,PeoplesearchTypeEnum.GLOBAL,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
				String comandoTwitter =String.format("googler -n 100 -w twitter.com @%s --unfilter --noprompt", peoplesearch.getTwitter());
				System.out.println("Twitter.: "+comandoTwitter);
				if(execute) {
					executarComando(comandoTwitter,PeoplesearchTypeEnum.TWITTER,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
			}
			
			if(!us.vazio(peoplesearch.getTiktok())) {
				enviarStatus(session, "TikTok search...");
				String comandoGlobal = String.format("googler -n 100 %s --unfilter --noprompt", peoplesearch.getTwitter());
				System.out.println("ComandoGlobal.: "+comandoGlobal);
				if(execute) {
					executarComando(comandoGlobal,PeoplesearchTypeEnum.GLOBAL,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
				String comandoTwitter =String.format("googler -n 100 -w tiktok.com %s --unfilter --noprompt", peoplesearch.getTwitter());
				System.out.println("TikTok.: "+comandoTwitter);
				if(execute) {
					executarComando(comandoTwitter,PeoplesearchTypeEnum.TIKTOK,peoplesearch, session, resultados);
				}	
				Thread.currentThread().sleep(5000);
				
			}
			
		}catch (Exception e) {
		}	
		return resultados;
	}
	
	private void enviarStatus(Session session, String msg) {
		try {
			Gson gson = new Gson();
			session.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO("",
					  new PeopleSearchResultTO("STATUS",msg))));
		}catch (Exception e) {
			//e.printStackTrace();
		}	
	}


	private void executarComando(String comando,PeoplesearchTypeEnum tipo, PeoplesearchTO peoplesearch, Session session, List<PeopleSearchResultTO> resultados) {
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String s = null;
		try {
			Runtime rt = Runtime.getRuntime();
			proc = rt.exec(comando);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(),"UTF-8"));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream(),"UTF-8"));
			PeopleSearchResultTO resultado = new PeopleSearchResultTO();
			Gson gson = new Gson();
			while ((s = stdInput.readLine()) != null) {
				if(session != null) {
					resultado = comporResultado(session,tipo,peoplesearch,resultado,resultados, s);
				}	
			}
			if(resultado.getTitulo() != null && resultado.getLink() != null) { 
				session.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(peoplesearch.getNome(), resultado)));
			}
			while ((s = stdError.readLine()) != null) {
				if(session != null) {
					resultado = comporResultado(session,tipo,peoplesearch,resultado,resultados,s);
				}	
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
	}
	
	
	public PeopleSearchResultTO comporResultado(Session session,PeoplesearchTypeEnum tipo, PeoplesearchTO peoplesearch, PeopleSearchResultTO result,List<PeopleSearchResultTO> resultados,String valor) {
		try {
			System.out.println("Compor resultado ="+valor);
			if(valor != null && !valor.equals("") && valor.trim().length() > 5) {
				
				Gson gson = new Gson();
				if(valor.trim().substring(0,2).matches("^[0-9]\\.") || valor.trim().substring(0,3).matches("^[0-9][0-9]\\.")) {
					System.out.println("entrou na regex");
					resultados.add(result);
					if(result.getTitulo() != null && result.getLink() != null) { 
						session.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(peoplesearch.getNome(), result)));
						System.out.println("Enviou");
					}
					result = null;
					result = new PeopleSearchResultTO();
					result.setTitulo(valor.trim());
					result.setTipo(tipo);
				}else if(valor.contains("http")) {                   
					result.setLink(valor.trim());
				}else {
					result.setTexto(result.getTexto() + valor.trim()+"<br />");
				}
			}	
		}catch(Exception e) {
		//	e.printStackTrace();
		}
		return result;
	}
	
}
