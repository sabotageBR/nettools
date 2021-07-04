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

import live.nettools.service.dirb.DirbService;
import live.nettools.to.DirbTO;

  
  @ServerEndpoint("/DirbServer")
  public class DirbServer {
	  
	      private @Inject DirbService dirbService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("dirb: "+message);
        	   	DirbTO dirb = new Gson().fromJson(message,DirbTO.class);
        	   	dirb.setDateTime(LocalDateTime.now());
        	   	dirbService	.executar(dirb, session);
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