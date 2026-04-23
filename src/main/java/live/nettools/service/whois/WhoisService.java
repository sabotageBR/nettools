package live.nettools.service.whois;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.WhoisTO;
import live.nettools.util.HostValidator;

@Named
public class WhoisService extends AbstractCommandService<WhoisTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(WhoisTO whois) {
        HostValidator.requireHost(whois.getHost());
    }

    @Override
    protected List<String> buildCommand(WhoisTO whois) {
        return Arrays.asList("whois", "--", whois.getHost().trim());
    }

    @Override
    protected WhoisTO buildMessageTO(WhoisTO input, String line) {
        return new WhoisTO(input.getHost(), line);
    }
}
