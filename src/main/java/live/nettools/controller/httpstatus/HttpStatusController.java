package live.nettools.controller.httpstatus;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import live.nettools.controller.AbstractController;
import live.nettools.session.CustomIdentity;
import live.nettools.to.HttpStatusTO;
import live.nettools.util.UtilCollection;

@Named
@ViewScoped
public class HttpStatusController extends AbstractController<HttpStatusTO> {

    private @Inject CustomIdentity customIdentity;

    private static final long serialVersionUID = -5104849116507289670L;

    private UtilCollection<HttpStatusTO> uc = new UtilCollection<HttpStatusTO>();

    @Override
    @PostConstruct
    protected void init() {
        getTo().setUrl("https://example.com");
    }

    public void httpstatus() {
        customIdentity.getHttpstatuses().add(new HttpStatusTO(getTo().getUrl(), LocalDateTime.now()));
        uc.ordenarListaDesc(customIdentity.getHttpstatuses(), "dateTimeOrder");
    }

    @Override
    public void clearHistory() {
        customIdentity.getHttpstatuses().clear();
    }
}
