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

import live.nettools.service.snmp.SnmpService;
import live.nettools.to.SnmpTO;

  
  @ServerEndpoint("/SnmpServer")
  public class SnmpServer {
	  
	      private @Inject SnmpService snmpService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("snmp: "+message);
        	   	SnmpTO snmp = new Gson().fromJson(message,SnmpTO.class);
        	   	snmp.setDateTime(LocalDateTime.now());
        	   	snmpService.executar(snmp, session);
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