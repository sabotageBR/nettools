  package live.nettools.websocket;
   
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import live.nettools.service.peoplesearch.PeoplesearchService;
import live.nettools.to.PeopleSearchResultTO;
import live.nettools.to.PeoplesearchTO;
import live.nettools.util.UtilCollection;

  
  @Stateless
  @ServerEndpoint("/PeoplesearchServer")
  public class PeoplesearchServer {
	  	  //private final Integer SEGUNDOS_EXECUCAO = 10;
	  	  private final Integer SEGUNDOS_EXECUCAO = 240;
	  	  private LocalDateTime dateLastExecute;
	      private @Inject PeoplesearchService peoplesearchService;
	      private Map<Session,PeoplesearchTO> mapaSearch = new HashMap<Session, PeoplesearchTO>();
	      
	      @Schedule(info="Peoplesearch-executar-fila",second="*/2", minute = "*", hour = "*", persistent = false)
          public void executarFila() {
	    	  Gson gson = new Gson();
	    	  try {
		    	  if(getDateLastExecute() == null || Double.valueOf(ChronoUnit.SECONDS.between(getDateLastExecute(), LocalDateTime.now())).intValue() >= SEGUNDOS_EXECUCAO) {
		    		  if(getMapaSearch().keySet().size() > 0) {
		    			  Session sessionExec = null;
		    			  PeoplesearchTO peoplesearchExecTO = null;
		    			  for(Session sessionIt:getMapaSearch().keySet()) {
		    				  if(sessionExec == null && peoplesearchExecTO == null) {
		    					  sessionExec = sessionIt;
		    					  peoplesearchExecTO = getMapaSearch().get(sessionIt);
		    				  }else if(getMapaSearch().get(sessionIt).getPosicao() < peoplesearchExecTO.getPosicao()) {
		    					  sessionExec = sessionIt;
		    					  peoplesearchExecTO = getMapaSearch().get(sessionIt);
		    				  }
		    			  }
		    			  setDateLastExecute(LocalDateTime.now());
		    			  if(sessionExec.isOpen()) {
		    				  sessionExec.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(peoplesearchExecTO.getNome(),new PeopleSearchResultTO("STATUS","Booting search!"))));
		    				  peoplesearchService.executar(peoplesearchExecTO,sessionExec);
		    				  sessionExec.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(peoplesearchExecTO.getNome(),new PeopleSearchResultTO("STATUS","Completed!"))));
		    				  sessionExec.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(peoplesearchExecTO.getNome(),new PeopleSearchResultTO("","FIM"))));
		    			  }	  
		    			  getMapaSearch().remove(sessionExec);
		    			  reordenarLista();
		    		  }	  
		    	  }else {
		    		 
		    		  if(getMapaSearch().size() > 0) {
			    		  for(Session sessionIt:getMapaSearch().keySet()) {
			    			 int segundosTimer =  Double.valueOf(ChronoUnit.SECONDS.between(getDateLastExecute().plus(SEGUNDOS_EXECUCAO,ChronoUnit.SECONDS),LocalDateTime.now())).intValue();
			    			 if(getMapaSearch().get(sessionIt).getPosicao() > 0) {
			    				 segundosTimer += (getMapaSearch().get(sessionIt).getPosicao() * SEGUNDOS_EXECUCAO); 
			    			 }
			    			  String msg = "Position: "+getMapaSearch().get(sessionIt).getPosicao() + " | Time left: "+(Math.abs(segundosTimer))+"s";
			    			  if(sessionIt != null && sessionIt.isOpen()) {
			    				  sessionIt.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO(getMapaSearch().get(sessionIt).getNome(),
 			    						  new PeopleSearchResultTO("STATUS",msg))));
			    			  }	  
			    		  }	  
		    		  }	  
		    	  }
	    	  }catch(Exception e) {
	    		//  e.printStackTrace();
	    	  }
          }
	      
	      
           @OnMessage
           public void recebeMensagem(String message, Session session) {
        	    try {
	        	   	PeoplesearchTO peoplesearch = new Gson().fromJson(message,PeoplesearchTO.class);
	        	   	peoplesearch.setDateTime(LocalDateTime.now());
	        	   	if(getMapaSearch().get(session) == null) {
	        	   		peoplesearch.setPosicao(getMapaSearch().size());
	        	   		getMapaSearch().put(session, peoplesearch);
	        	   		String msg = "Entering in queue...";
	 	 			    session.getBasicRemote().sendText(new Gson().toJson(new PeoplesearchTO("", new PeopleSearchResultTO("STATUS",msg))));
	        	   	}
        	    }catch(Exception e) {
        	    	//e.printStackTrace();
        	    }
           }
           
           @OnOpen
           public void open(Session session) {
        	   try {
	        	   Gson gson = new Gson();
	        	   String msg = "Peoples Waiting: "+getMapaSearch().size() + " | Estimated time: "+Math.abs(getMapaSearch().size() * SEGUNDOS_EXECUCAO)+"s";
	 			   session.getBasicRemote().sendText(gson.toJson(new PeoplesearchTO("", new PeopleSearchResultTO("STATUS",msg))));
        	   }catch (Exception e) {
				//e.printStackTrace();
			   }	   
           }
           
           @OnError
           public void error(Session session, Throwable t) {
        	   if(getMapaSearch().get(session) != null) {
        		   getMapaSearch().remove(session);
        	   }
        	   reordenarLista();
           }
            
            
           @OnClose
           public void closedConnection(Session session) { 
        	   if(getMapaSearch().size() > 0 && getMapaSearch().get(session) != null) {
        		   getMapaSearch().remove(session);
        	   }
        	   reordenarLista();
        	   	   
           }
           
       private void reordenarLista() {
    	   UtilCollection<PeoplesearchTO> uc = new UtilCollection<PeoplesearchTO>();
    	   if(getMapaSearch().size() > 0) {
    		   List<PeoplesearchTO> list = new ArrayList<PeoplesearchTO>();
    		   for(Session sessionIt:getMapaSearch().keySet()) {
    			   list.add(getMapaSearch().get(sessionIt));
    		   }
    		   uc.ordenarListaAsc(list, "posicao");
    		   int ordem = 0;
    		   for(PeoplesearchTO people:list) {
    			   people.setPosicao(ordem);
    			   ordem++;
    		   }
    	   }
       }


		public LocalDateTime getDateLastExecute() {
			return dateLastExecute;
		}


		public void setDateLastExecute(LocalDateTime dateLastExecute) {
			this.dateLastExecute = dateLastExecute;
		}


		public Map<Session, PeoplesearchTO> getMapaSearch() {
			if (mapaSearch == null) {
				mapaSearch = new HashMap<Session, PeoplesearchTO>();
			}
			return mapaSearch;
		}


		public void setMapaSearch(Map<Session, PeoplesearchTO> mapaSearch) {
			this.mapaSearch = mapaSearch;
		}
           
  }