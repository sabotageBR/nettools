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

import live.nettools.service.ping.PingService;
import live.nettools.to.PingTO;

  
  @ServerEndpoint("/PingServer")
  public class PingServer {
	  
	      private @Inject PingService pingService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("ping: "+message);
        	   	PingTO ping = new Gson().fromJson(message,PingTO.class);
        	   	ping.setDateTime(LocalDateTime.now());
        	   	pingService	.executar(ping, session);
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