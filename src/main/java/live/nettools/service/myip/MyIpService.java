package live.nettools.service.myip;

import java.io.Serializable;

import javax.inject.Named;

import live.nettools.to.MyIpTO;
import live.nettools.util.IpApiClient;

@Named
public class MyIpService implements Serializable {

    private static final long serialVersionUID = 1L;

    public MyIpTO describe(String ip, String userAgent, String acceptLanguage) {
        MyIpTO to = new MyIpTO();
        to.setIp(ip);
        to.setUserAgent(userAgent);
        to.setAcceptLanguage(acceptLanguage);
        to.setIpv6(ip != null && ip.contains(":"));

        IpApiClient.Result geo = IpApiClient.lookup(ip);
        if (geo != null) {
            to.setCountry(geo.country);
            to.setCountryCode(geo.countryCode);
            to.setRegion(geo.region);
            to.setCity(geo.city);
            to.setPostal(geo.postal);
            to.setTimezone(geo.timezone);
            to.setIsp(geo.isp);
            to.setOrg(geo.org);
            to.setAsn(geo.asn);
            to.setLatitude(geo.lat);
            to.setLongitude(geo.lon);
            to.setProxy(geo.proxy);
            to.setHosting(geo.hosting);
            to.setHostname(geo.reverse);
        }
        return to;
    }
}
