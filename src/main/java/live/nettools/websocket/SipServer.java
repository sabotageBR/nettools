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

import live.nettools.service.sip.SipService;
import live.nettools.to.SipTO;

  
  @ServerEndpoint("/SipServer")
  public class SipServer {
	  
	      private @Inject SipService sipService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("sip: "+message);
        	   	SipTO sip = new Gson().fromJson(message,SipTO.class);
        	   	sip.setDateTime(LocalDateTime.now());
        	   	sipService.executar(sip, session);
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