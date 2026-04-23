package live.nettools.controller.myip;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import live.nettools.controller.AbstractController;
import live.nettools.service.myip.MyIpService;
import live.nettools.to.MyIpTO;

@Named
@RequestScoped
public class MyIpController extends AbstractController<MyIpTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MyIpService service;

    private MyIpTO info;

    @PostConstruct
    private void init() {
        HttpServletRequest req = getRequest();
        String ip = getClientIpAddr();
        String ua = req != null ? req.getHeader("User-Agent") : null;
        String lang = req != null ? req.getHeader("Accept-Language") : null;
        info = service.describe(ip, ua, lang);
    }

    public MyIpTO getInfo() { return info; }
}
