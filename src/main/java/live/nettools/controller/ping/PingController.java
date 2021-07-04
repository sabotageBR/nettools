package live.nettools.controller.ping;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.PingTO;
import live.nettools.util.UtilCollection;

@Model
public class PingController extends AbstractController<PingTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<PingTO> uc = new UtilCollection<PingTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: Ping");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
		getTo().setQtd("3");
	}
	
	public void ping() {
		customIdentity.getPings().add(new PingTO(getTo().getHost(), getTo().getQtd(), getTo().getSendBuffer(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getPings(),"dateTimeOrder");
	}
	
}
