package live.nettools.service.snmp;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import live.nettools.service.AbstractCommandService;
import live.nettools.to.SnmpTO;
import live.nettools.util.HostValidator;

@Named
public class SnmpService extends AbstractCommandService<SnmpTO> {

    private static final long serialVersionUID = -6986454410309827919L;

    @Override
    protected void validate(SnmpTO snmp) {
        HostValidator.requireHost(snmp.getHost());
        HostValidator.requirePort(snmp.getPorta());
        HostValidator.requireSnmpVersion(snmp.getVersao());
        HostValidator.requireSnmpCommunity(snmp.getCommunity());
        HostValidator.requireOid(snmp.getOid());
    }

    @Override
    protected List<String> buildCommand(SnmpTO snmp) {
        String target = snmp.getHost().trim() + ":" + snmp.getPorta().trim();
        return Arrays.asList(
                "snmpwalk",
                "-v", snmp.getVersao().trim(),
                "-On",
                "-c", snmp.getCommunity().trim(),
                target,
                snmp.getOid().trim());
    }

    @Override
    protected SnmpTO buildMessageTO(SnmpTO input, String line) {
        return new SnmpTO(input.getHost(), line);
    }
}
