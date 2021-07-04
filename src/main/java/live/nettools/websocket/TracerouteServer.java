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

import live.nettools.service.traceroute.TracerouteService;
import live.nettools.to.TracerouteTO;

  
  @ServerEndpoint("/TracerouteServer")
  public class TracerouteServer {
	  
	      private @Inject TracerouteService tracerouteService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	   	System.out.println("traceroute: "+message); 
        	   	TracerouteTO traceroute = new Gson().fromJson(message,TracerouteTO.class);
        	   	traceroute.setDateTime(LocalDateTime.now());
        	   	tracerouteService.executar(traceroute, session);
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