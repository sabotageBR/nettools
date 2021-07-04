package live.nettools.controller.snmp;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.SnmpTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class SnmpController extends AbstractController<SnmpTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<SnmpTO> uc = new UtilCollection<SnmpTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: SNMP");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
		getTo().setHost("192.168.100.100");
		getTo().setOid("1.3.6.1.2.1.1");
		getTo().setVersao("2c");
		getTo().setCommunity("public");
		getTo().setPorta("161");
		
	}
	
	public void snmp() {
		customIdentity.getSnmps().add(new SnmpTO(getTo().getHost(), getTo().getPorta(), getTo().getOid(),
				getTo().getVersao(), getTo().getCommunity(), LocalDateTime.now()));
		uc.ordenarListaDesc(customIdentity.getSnmps(),"dateTimeOrder");
	}
	
}
