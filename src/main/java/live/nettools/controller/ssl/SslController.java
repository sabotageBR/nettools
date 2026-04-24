package live.nettools.controller.ssl;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.SslTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class SslController extends AbstractController<SslTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289690L;

    private UtilCollection<SslTO> uc = new UtilCollection<SslTO>();

    @Override
    @PostConstruct
    protected void init() {
        getTo().setHost("google.com");
        getTo().setPorta("443");
    }

    public void ssl() {
        customIdentity.getSsls().add(new SslTO(getTo().getHost(), getTo().getPorta(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getSsls(), "dateTimeOrder");
    }

    @Override
    public void clearHistory() {
        customIdentity.getSsls().clear();
    }
}
