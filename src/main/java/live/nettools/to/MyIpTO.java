package live.nettools.to;

public class MyIpTO extends NettoolsTO {

    private static final long serialVersionUID = 5400112879734505570L;

    private String ip;
    private String hostname;
    private String country;
    private String countryCode;
    private String region;
    private String city;
    private String postal;
    private String timezone;
    private String isp;
    private String org;
    private String asn;
    private Double latitude;
    private Double longitude;
    private boolean proxy;
    private boolean hosting;
    private String userAgent;
    private String acceptLanguage;
    private boolean ipv6;

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostal() { return postal; }
    public void setPostal(String postal) { this.postal = postal; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }
    public String getOrg() { return org; }
    public void setOrg(String org) { this.org = org; }
    public String getAsn() { return asn; }
    public void setAsn(String asn) { this.asn = asn; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isProxy() { return proxy; }
    public void setProxy(boolean proxy) { this.proxy = proxy; }
    public boolean isHosting() { return hosting; }
    public void setHosting(boolean hosting) { this.hosting = hosting; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getAcceptLanguage() { return acceptLanguage; }
    public void setAcceptLanguage(String acceptLanguage) { this.acceptLanguage = acceptLanguage; }
    public boolean isIpv6() { return ipv6; }
    public void setIpv6(boolean ipv6) { this.ipv6 = ipv6; }
}
