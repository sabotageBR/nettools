  package live.nettools.websocket;
   
  import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import live.nettools.service.whois.WhoisService;
import live.nettools.to.WhoisTO;

  
  @ServerEndpoint("/WhoisServer")
  public class WhoisServer {
	  
	      private @Inject WhoisService whoisService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("whois:"+message);
        	   	WhoisTO whois = new Gson().fromJson(message,WhoisTO.class);
        	   	whois.setDateTime(LocalDateTime.now());
        	   	whoisService.executar(whois, session);
           }
           
           @OnOpen
           public void open(Session session) {
           }
           
            @OnError
           public void error(Session session, Throwable t) {
           }
            
            
           @OnClose
           public void closedConnection(Session session) { 
           }
           
			
           
  }