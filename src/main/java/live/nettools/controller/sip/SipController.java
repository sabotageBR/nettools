package live.nettools.controller.sip;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.SipTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class SipController extends AbstractController<SipTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<SipTO> uc = new UtilCollection<SipTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: SIP");	
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
		getTo().setHost("192.168.10.41");
		getTo().setPorta("5060");
		getTo().setUser("user");
	}
	
	public void sip() {
		customIdentity.getSips().add(new SipTO(getTo().getHost(), getTo().getPorta(),getTo().getUser(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getSips(),"dateTimeOrder");
	}
	
}
