package live.nettools.controller.peoplesearch;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.PeoplesearchTO;
import live.nettools.util.UtilCollection;
import live.nettools.util.UtilString;

@Named
@ViewScoped
public class PeoplesearchController extends AbstractController<PeoplesearchTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<PeoplesearchTO> uc = new UtilCollection<PeoplesearchTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: Peoplesearch");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
	}
	
	public void peoplesearch() {
		UtilString us = new UtilString();
		if(!us.vazio(getTo().getNome()) || !us.vazio(getTo().getFacebook()) ||
				!us.vazio(getTo().getInstagram()) ||!us.vazio(getTo().getTwitter())) {
			
		customIdentity.getPeoplesearchs().add(new PeoplesearchTO(getTo().getNome(), getTo().getInstagram(),
				getTo().getFacebook(), getTo().getTwitter(), LocalDateTime.now()));
		
		uc.ordenarListaDesc(customIdentity.getPeoplesearchs(),"dateTimeOrder");
		}
	}
	
}
