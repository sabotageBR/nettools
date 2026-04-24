package live.nettools.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class AbstractController<TO> implements Serializable {

	private static final long serialVersionUID = 6316180784775546650L;
	private TO to;
	protected final String SUCESSO = "sucesso";
	protected final String ERRO 	= "erro";

	/**
	 * Botão "Clear" das tool pages: zera apenas o TO (form volta vazio)
	 * e chama init() para reaplicar os defaults do formulário (mesmo
	 * método que o CDI executa via @PostConstruct no primeiro load).
	 * Não toca no histórico — pra isso o usuário tem o link
	 * "Clear history" dentro do painel History.
	 */
	public void clear() {
		this.to = null;
		init();
	}

	/**
	 * Cada controller concreto sobrescreve este método e o anota com
	 * @PostConstruct para que o CDI também o execute na criação do bean.
	 * Default no-op para controllers que não precisam de inicialização.
	 */
	protected void init() {
		// default: no-op
	}

	/**
	 * Limpa somente o histórico (sem mexer no form atual). Chamável direto
	 * do JSF via #{controller.clearHistory}. Cada controller sobrescreve
	 * para zerar sua lista no CustomIdentity.
	 */
	public void clearHistory() {
		// default: no-op
	}
	
	
	public HttpServletRequest getRequest() { 
		FacesContext ctx = getFacesContext();
		ExternalContext exc = ctx.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) exc.getRequest();
		return request;
	}
	
	public HttpServletResponse getResponse() { 
		FacesContext ctx = getFacesContext();
		ExternalContext exc = ctx.getExternalContext();
		HttpServletResponse response = (HttpServletResponse) exc.getResponse();
		return response;
	}
	
	public ServletContext getServletContext() { 
		FacesContext ctx = getFacesContext();
		ExternalContext exc = ctx.getExternalContext();
		return (ServletContext)exc.getContext();
	}
	
	
	protected FacesContext getFacesContext() {
		return FacesContext
				.getCurrentInstance();
	}
	
	
	protected void download(ByteArrayOutputStream outputStream, String fileName) throws IOException {
        try{
        	HttpServletResponse response = (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
	    	response.reset();
	        response.setContentLength(outputStream.size());
	        response.setContentType("application/".concat(fileName.substring(fileName.length()-3, fileName.length())));
	        response.setHeader("Content-Disposition", "attachment; filename=".concat(fileName).concat(";"));
	        ServletOutputStream outputStreamServlet = response.getOutputStream();
	        outputStream.toByteArray();
	        outputStream.writeTo(outputStreamServlet);
	        outputStreamServlet.flush();
	        outputStreamServlet.close();
	        outputStream.flush();
	        outputStream.close();
	        getFacesContext().responseComplete();
        }catch (Exception e) {
        	e.printStackTrace();
        }    
    }
	
		
	
	
	 public String getStackTrace() {
	        Throwable throwable = (Throwable)  FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("javax.servlet.error.exception");
	        StringBuilder builder = new StringBuilder();
	        if(throwable!= null && throwable.getMessage() != null){
		        builder.append(throwable.getMessage()).append("\n");
		        for (StackTraceElement element : throwable.getStackTrace()) {
		            builder.append(element).append("\n");
		        }
	        }   
	        return builder.toString();
	    }
	 
	 
	public String getMessage(String label){
		ResourceBundle rb = ResourceBundle.getBundle("resources", getFacesContext().getViewRoot().getLocale());
		return rb.getString(label);
	}
	
	 public String getMessage(String key, Object... params) {
		 	ResourceBundle rb = ResourceBundle.getBundle("resources", getFacesContext().getViewRoot().getLocale());
	        return MessageFormat.format(rb.getString(key), params);
	  }

	
	public void adicionarMensagem(String key,Severity severity){
		String mensagem = getMessage(key);
		getFacesContext().addMessage(null, new FacesMessage(severity, mensagem, mensagem));
	}
	
	public void adicionarMsgGlobalMessage(String key,Severity severity){
		String mensagem = getMessage(key);
		getFacesContext().addMessage(null, new FacesMessage(severity, mensagem, mensagem));
		getFacesContext().getPartialViewContext().getRenderIds().add("globalMessage");
		getFacesContext().getExternalContext().getFlash().setKeepMessages(true);
	}
	
	public void adicionarMsgGlobalMessage(String key,Severity severity,Object[] params){
		String mensagem = getMessage(key,params);
		getFacesContext().addMessage(null, new FacesMessage(severity, mensagem, mensagem));
		getFacesContext().getPartialViewContext().getRenderIds().add("globalMessage");
		getFacesContext().getExternalContext().getFlash().setKeepMessages(true);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public TO getTo(){
		if (to == null) {
			try{
				to = ((Class<TO>)((ParameterizedType)this.getClass().
					       getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
			}catch(ClassCastException cce){
				try{
					to = ((Class<TO>)((ParameterizedType)(((Class<TO>)
							this.getClass().getAnnotatedSuperclass().getType()).getGenericSuperclass()))
								.getActualTypeArguments()[0]).newInstance();
				}catch(Exception e){
					e.printStackTrace();
				}	
			}catch(Exception e){
				e.printStackTrace();
			}	
		}
		return to;
	}
	
	public String getClientIpAddr() {
		
	    String ip = getRequest().getHeader("X-Forwarded-For");  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("WL-Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_X_FORWARDED_FOR");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_X_FORWARDED");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_X_CLUSTER_CLIENT_IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_CLIENT_IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_FORWARDED_FOR");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_FORWARDED");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("HTTP_VIA");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getHeader("REMOTE_ADDR");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
	        ip = getRequest().getRemoteAddr();  
	    }  
	    return ip;  
	}
}
