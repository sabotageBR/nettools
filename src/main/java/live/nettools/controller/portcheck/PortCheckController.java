package live.nettools.controller.portcheck;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.PortCheckTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class PortCheckController extends AbstractController<PortCheckTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289651L;

    private UtilCollection<PortCheckTO> uc = new UtilCollection<PortCheckTO>();

    @PostConstruct
    private void init() {
        comporInformacoesHTTP();
    }

    private void comporInformacoesHTTP() {
        getTo().setHost("example.com");
        getTo().setPorta("443");
    }

    public void portcheck() {
        customIdentity.getPortchecks().add(new PortCheckTO(
                getTo().getHost(), getTo().getPorta(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getPortchecks(), "dateTimeOrder");
    }
}
