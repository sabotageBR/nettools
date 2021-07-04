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

import live.nettools.service.nslookup.NslookupService;
import live.nettools.to.NslookupTO;

  
  @ServerEndpoint("/NslookupServer")
  public class NslookupServer {
	  
	      private @Inject NslookupService nslookupService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("nslookup: "+message);
        	   	NslookupTO nslookup = new Gson().fromJson(message,NslookupTO.class);
        	   	nslookup.setDateTime(LocalDateTime.now());
        	   	nslookupService	.executar(nslookup, session);
           }
           
           @OnOpen
           public void open(Session session) {
           }
           
            @OnError
           public void error(Session session, Throwable t) {
            System.err.println("Error on session "+session.getId());  
           }
            
            
           @OnClose
           public void closedConnection(Session session) { 
           }
           
			
           
  }