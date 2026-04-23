package live.nettools.service.nmap;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.NmapTO;
import live.nettools.util.HostValidator;

@Named
public class NmapService extends AbstractCommandService<NmapTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(NmapTO nmap) {
        HostValidator.requireHost(nmap.getHost());
    }

    @Override
    protected List<String> buildCommand(NmapTO nmap) {
        return Arrays.asList("nmap", nmap.getHost().trim());
    }

    @Override
    protected NmapTO buildMessageTO(NmapTO input, String line) {
        return new NmapTO(input.getHost(), line);
    }

    @Override
    protected long timeoutSeconds() {
        return 120L;
    }
}
