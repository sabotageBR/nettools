package live.nettools.controller.traceroute;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.TracerouteTO;
import live.nettools.util.UtilCollection;

@Model
public class TracerouteController extends AbstractController<TracerouteTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<TracerouteTO> uc = new UtilCollection<TracerouteTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: Traceroute");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
	}
	
	public void traceroute() {
		customIdentity.getTraceroutes().add(new TracerouteTO(getTo().getHost(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getTraceroutes(),"dateTimeOrder");
	}
	
}
