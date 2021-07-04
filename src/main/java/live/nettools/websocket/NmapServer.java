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

import live.nettools.service.nmap.NmapService;
import live.nettools.to.NmapTO;

  
  @ServerEndpoint("/NmapServer")
  public class NmapServer {
	  
	      private @Inject NmapService nmapService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("nmap: "+message);
        	   	NmapTO nmap = new Gson().fromJson(message,NmapTO.class);
        	   	nmap.setDateTime(LocalDateTime.now());
        	   	nmapService	.executar(nmap, session);
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