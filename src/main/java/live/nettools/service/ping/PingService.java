package live.nettools.service.ping;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.PingTO;
import live.nettools.util.HostValidator;

@Named
public class PingService extends AbstractCommandService<PingTO> {

    private static final long serialVersionUID = -6986454410309827919L;
    private static final int MAX_COUNT = 30;
    private static final int MAX_SEND_BUFFER = 65507;

    @Override
    protected void validate(PingTO ping) {
        HostValidator.requireHost(ping.getHost());
        HostValidator.requirePositiveInt(ping.getQtd(), "qtd", MAX_COUNT);
        if (ping.getSendBuffer() != null && !ping.getSendBuffer().trim().isEmpty()) {
            HostValidator.requirePositiveInt(ping.getSendBuffer(), "sendBuffer", MAX_SEND_BUFFER);
        }
    }

    @Override
    protected List<String> buildCommand(PingTO ping) {
        List<String> cmd = new ArrayList<String>();
        cmd.add("ping");
        if (ping.getSendBuffer() != null && !ping.getSendBuffer().trim().isEmpty()) {
            cmd.add("-M");
            cmd.add("do");
            cmd.add("-s");
            cmd.add(ping.getSendBuffer().trim());
        }
        cmd.add("-c");
        cmd.add(ping.getQtd().trim());
        cmd.add(ping.getHost().trim());
        return cmd;
    }

    @Override
    protected PingTO buildMessageTO(PingTO input, String line) {
        return new PingTO(input.getHost(), line);
    }
}
