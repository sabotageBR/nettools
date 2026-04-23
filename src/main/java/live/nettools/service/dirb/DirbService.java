package live.nettools.service.dirb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.DirbTO;
import live.nettools.util.HostValidator;

@Named
public class DirbService extends AbstractCommandService<DirbTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(DirbTO dirb) {
        String raw = dirb.getHost() == null ? "" : dirb.getHost().trim();
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            HostValidator.requireUrl(raw, "http", "https");
        } else {
            HostValidator.requireHost(raw);
        }
    }

    @Override
    protected List<String> buildCommand(DirbTO dirb) {
        String target = dirb.getHost().trim();
        if (!target.startsWith("http://") && !target.startsWith("https://")) {
            target = "http://" + target;
        }
        return Arrays.asList("dirb", target, "-f");
    }

    @Override
    protected DirbTO buildMessageTO(DirbTO input, String line) {
        return new DirbTO(input.getHost(), line);
    }

    @Override
    protected long timeoutSeconds() {
        return 300L;
    }
}
