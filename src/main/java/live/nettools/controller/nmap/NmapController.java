package live.nettools.controller.nmap;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.NmapTO;
import live.nettools.util.UtilCollection;

@Model
public class NmapController extends AbstractController<NmapTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<NmapTO> uc = new UtilCollection<NmapTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: NMap");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
		
	}
	
	public void nmap() {
		customIdentity.getNmaps().add(new NmapTO(getTo().getHost(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getNmaps(),"dateTimeOrder");
	}
	
}
