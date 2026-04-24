package live.nettools.controller.reversedns;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.ReverseDnsTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class ReverseDnsController extends AbstractController<ReverseDnsTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289652L;

    private UtilCollection<ReverseDnsTO> uc = new UtilCollection<ReverseDnsTO>();

    @Override
    @PostConstruct
    protected void init() {
        comporInformacoesHTTP();
    }

    private void comporInformacoesHTTP() {
        getTo().setIp("8.8.8.8");
    }

    public void reversedns() {
        customIdentity.getReverseDns().add(new ReverseDnsTO(getTo().getIp(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getReverseDns(), "dateTimeOrder");
    }

    @Override
    public void clearHistory() {
        customIdentity.getReverseDns().clear();
    }
}
