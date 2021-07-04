package live.nettools.controller.nslookup;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.NslookupTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class NslookupController extends AbstractController<NslookupTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<NslookupTO> uc = new UtilCollection<NslookupTO>();
	
	@PostConstruct
	private void init() {
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
	}
	
	public void nslookup() {
		customIdentity.getNslookups().add(new NslookupTO(getTo().getHost(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getNslookups(),"dateTimeOrder");
	}
	
}
