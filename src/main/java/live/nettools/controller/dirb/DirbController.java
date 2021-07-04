package live.nettools.controller.dirb;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.DirbTO;
import live.nettools.util.UtilCollection;
import live.nettools.util.UtilString;

@Model
public class DirbController extends AbstractController<DirbTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<DirbTO> uc = new UtilCollection<DirbTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: Dirb");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
	}
	
	public void dirb() {
		UtilString us = new UtilString();
		if(!us.vazio(getTo().getHost())) {
			customIdentity.getDirbs().add(new DirbTO(getTo().getHost(), LocalDateTime.now()));
			uc.ordenarListaDesc(customIdentity.getDirbs(),"dateTimeOrder");
		}	
	}
	
}
