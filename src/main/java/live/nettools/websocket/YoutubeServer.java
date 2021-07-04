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

import live.nettools.service.youtube.YoutubeService;
import live.nettools.to.YoutubeTO;

  
  @ServerEndpoint("/YoutubeServer")
  public class YoutubeServer {
	  
	      private @Inject YoutubeService youtubeService;
	  
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    System.out.println("videodownload: "+message);
        	   	YoutubeTO youtube = new Gson().fromJson(message,YoutubeTO.class);
        	   	youtube.setDateTime(LocalDateTime.now());
        	   	youtubeService.executar(youtube, session);
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