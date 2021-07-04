  package live.nettools.websocket;
   
  import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Schedule;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

   
  @ServerEndpoint("/NettoolServer")
  public class NettoolServer {
	  
	  
	  		private static Map<Session,String> mapaQueue = new ConcurrentHashMap<Session,String>();
	  		private static Map<Integer,Map<String,String>> mapaGeral = new HashMap<Integer,Map<String,String>>();
	  		

           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	   	 mapaQueue.put(session, message);
           }
           
           
           @OnOpen
           public void open(Session session) {
        	mapaQueue.put(session, "");   
            System.out.println("New session opened: "+session.getId());
           }
           
            @OnError
           public void error(Session session, Throwable t) {
            mapaQueue.remove(session);
            System.err.println("Error on session "+session.getId());  
           }
            
            
           @OnClose
           public void closedConnection(Session session) { 
            mapaQueue.remove(session);
            System.out.println("session closed: "+session.getId());
           }
           
           public Map<Integer,Map<String,String>> recuperarMapaGeral(){
        	   return mapaGeral;
           }
           
           @Schedule(info="Update-Snmp-Client",second="*/10", minute = "*", hour = "*", persistent = false)
           public void atualizar(){
        	   if(mapaQueue.size() > 0){
        		  
        	   }  
           }
           
  }