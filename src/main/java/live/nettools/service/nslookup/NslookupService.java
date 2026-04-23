package live.nettools.service.nslookup;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.NslookupTO;
import live.nettools.util.HostValidator;

@Named
public class NslookupService extends AbstractCommandService<NslookupTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(NslookupTO nslookup) {
        HostValidator.requireHost(nslookup.getHost());
    }

    @Override
    protected List<String> buildCommand(NslookupTO nslookup) {
        return Arrays.asList("nslookup", nslookup.getHost().trim());
    }

    @Override
    protected NslookupTO buildMessageTO(NslookupTO input, String line) {
        return new NslookupTO(input.getHost(), line);
    }
}
