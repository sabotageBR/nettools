package live.nettools.controller.mac;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.MacTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class MacController extends AbstractController<MacTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289660L;

    private UtilCollection<MacTO> uc = new UtilCollection<MacTO>();

    @Override
    @PostConstruct
    protected void init() {
        getTo().setMac("00:1A:2B:3C:4D:5E");
    }

    public void mac() {
        customIdentity.getMacs().add(new MacTO(getTo().getMac(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getMacs(), "dateTimeOrder");
    }

    @Override
    public void clearHistory() {
        customIdentity.getMacs().clear();
    }
}
