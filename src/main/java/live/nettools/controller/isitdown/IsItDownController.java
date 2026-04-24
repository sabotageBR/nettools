package live.nettools.controller.isitdown;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.IsItDownTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class IsItDownController extends AbstractController<IsItDownTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289680L;

    private UtilCollection<IsItDownTO> uc = new UtilCollection<IsItDownTO>();

    @Override
    @PostConstruct
    protected void init() {
        getTo().setHost("example.com");
    }

    public void isitdown() {
        customIdentity.getIsitdowns().add(new IsItDownTO(getTo().getHost(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getIsitdowns(), "dateTimeOrder");
    }

    @Override
    public void clearHistory() {
        customIdentity.getIsitdowns().clear();
    }
}
