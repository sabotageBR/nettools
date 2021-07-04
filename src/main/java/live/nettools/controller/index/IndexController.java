package live.nettools.controller.index;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.IndexTO;

@Model
public class IndexController extends AbstractController<IndexTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	@PostConstruct
	private void init() {
		comporInformacoesHTTP();
	}
	
	public void comporInformacoesHTTP() {
		customIdentity.setIpExterno(getTo().getIp());
		
	}
}
