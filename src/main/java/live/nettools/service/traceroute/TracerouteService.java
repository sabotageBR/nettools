package live.nettools.service.traceroute;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.TracerouteTO;
import live.nettools.util.HostValidator;

@Named
public class TracerouteService extends AbstractCommandService<TracerouteTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(TracerouteTO traceroute) {
        HostValidator.requireHost(traceroute.getHost());
    }

    @Override
    protected List<String> buildCommand(TracerouteTO traceroute) {
        return Arrays.asList("traceroute", "--", traceroute.getHost().trim());
    }

    @Override
    protected TracerouteTO buildMessageTO(TracerouteTO input, String line) {
        return new TracerouteTO(input.getHost(), line);
    }
}
