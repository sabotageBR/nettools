package live.nettools.controller.whois;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.WhoisTO;
import live.nettools.util.UtilCollection;

@Model
public class WhoisController extends AbstractController<WhoisTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<WhoisTO> uc = new UtilCollection<WhoisTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: Whois");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
	}
	
	public void whois() {
		customIdentity.getWhoiss().add(new WhoisTO(getTo().getHost(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getWhoiss(),"dateTimeOrder");
	}
	
}
