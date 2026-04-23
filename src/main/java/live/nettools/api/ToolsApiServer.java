package live.nettools.api;


import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import live.nettools.service.dirb.DirbService;
import live.nettools.service.httpstatus.HttpStatusService;
import live.nettools.service.isitdown.IsItDownService;
import live.nettools.service.mac.MacService;
import live.nettools.service.myip.MyIpService;
import live.nettools.service.nmap.NmapService;
import live.nettools.service.nslookup.NslookupService;
import live.nettools.service.peoplesearch.PeoplesearchService;
import live.nettools.service.ping.PingService;
import live.nettools.service.portcheck.PortCheckService;
import live.nettools.service.reversedns.ReverseDnsService;
import live.nettools.service.sip.SipService;
import live.nettools.service.snmp.SnmpService;
import live.nettools.service.ssl.SslService;
import live.nettools.service.traceroute.TracerouteService;
import live.nettools.service.whois.WhoisService;
import live.nettools.service.youtube.YoutubeService;
import live.nettools.to.DirbTO;
import live.nettools.to.HttpStatusTO;
import live.nettools.to.IsItDownTO;
import live.nettools.to.MacTO;
import live.nettools.to.MyIpTO;
import live.nettools.to.NmapTO;
import live.nettools.to.NslookupTO;
import live.nettools.to.PingTO;
import live.nettools.to.PortCheckTO;
import live.nettools.to.ReverseDnsTO;
import live.nettools.to.SipTO;
import live.nettools.to.SnmpTO;
import live.nettools.to.SslTO;
import live.nettools.to.TracerouteTO;
import live.nettools.to.WhoisTO;


@javax.ws.rs.Path(value="/tools")
public class ToolsApiServer {
	
	private @Inject PingService pingService;
	private @Inject WhoisService whoisService;
	private @Inject TracerouteService tracerouteService;
	private @Inject NmapService nmapService;
	private @Inject SnmpService snmpService;
	private @Inject SipService  sipService;
	private @Inject NslookupService  nslookupService;
	private @Inject DirbService  dirbService;
	private @Inject YoutubeService  youtubeService;
	private @Inject PeoplesearchService peoplesearchService;
	private @Inject PortCheckService portCheckService;
	private @Inject ReverseDnsService reverseDnsService;
	private @Inject MyIpService myIpService;
	private @Inject MacService macService;
	private @Inject HttpStatusService httpStatusService;
	private @Inject IsItDownService isItDownService;
	private @Inject SslService sslService;
	
	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping(@QueryParam(value = "host") String host,
						 @QueryParam(value = "qtd") String qtd,
						 @QueryParam(value = "sendbuffer") String sendbuffer) throws Exception {
		
		System.out.println("api-ping= host:"+host+" qtd:"+qtd+" sendBuffer:"+sendbuffer);
		PingTO pingTO = new PingTO(host,qtd,sendbuffer,LocalDateTime.now());
		pingTO.setResultado(pingService.executar(pingTO, null));
		
		return Response.ok().entity(pingTO).build();
	}
	
	
	
	@GET
	@Path("/whois")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping(@QueryParam(value = "host") String host) throws Exception {
		System.out.println("api-whois= host:"+host);
		WhoisTO whoisTO = new WhoisTO(host,LocalDateTime.now());
		whoisTO.setResultado(whoisService.executar(whoisTO, null));
		return Response.ok().entity(whoisTO).build();
	}
	
	
	@GET
	@Path("/traceroute")
	@Produces(MediaType.APPLICATION_JSON)
	public Response traceroute(@QueryParam(value = "host") String host) throws Exception {
		System.out.println("api-traceroute= host:"+host);
		TracerouteTO tracerouteTO = new TracerouteTO(host,LocalDateTime.now());
		tracerouteTO.setResultado(tracerouteService.executar(tracerouteTO, null));
		return Response.ok().entity(tracerouteTO).build();
	}
	
	@GET
	@Path("/nmap")
	@Produces(MediaType.APPLICATION_JSON)
	public Response nmap(@QueryParam(value = "host") String host) throws Exception {
		System.out.println("api-nmap= host:"+host);
		NmapTO nmapTO = new NmapTO(host,LocalDateTime.now());
		nmapTO.setResultado(nmapService.executar(nmapTO, null));
		return Response.ok().entity(nmapTO).build();
	}
	
	@GET
	@Path("/snmp")
	@Produces(MediaType.APPLICATION_JSON)
	public Response snmp(@QueryParam(value = "host") String host,
						 @QueryParam(value = "porta") String porta,
						 @QueryParam(value = "oid") String oid,
						 @QueryParam(value = "versao") String versao,
						 @QueryParam(value = "community") String community) throws Exception {
		System.out.println("api-snmp= host:"+host+" porta:"+porta+" oid:"+oid+" versao:"+versao+" community:"+community);
		SnmpTO snmpTO = new SnmpTO(host,porta,oid,versao,community,LocalDateTime.now());
		snmpTO.setResultado(snmpService.executar(snmpTO, null));
		return Response.ok().entity(snmpTO).build();
	}
	
	@GET
	@Path("/sip")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sip(@QueryParam(value = "host") String host,
						 @QueryParam(value = "porta") String porta,
						 @QueryParam(value = "user") String user) throws Exception {
		System.out.println("api-ping= host:"+host+" porta:"+porta+" user:"+user);
		SipTO sipTO = new SipTO(host,porta,user,LocalDateTime.now());
		sipTO.setResultado(sipService.executar(sipTO, null));
		return Response.ok().entity(sipTO).build();
	}
	
	@GET
	@Path("/nslookup")
	@Produces(MediaType.APPLICATION_JSON)
	public Response nslookup(@QueryParam(value = "host") String host) throws Exception {
		System.out.println("api-nslookup= host:"+host);
		NslookupTO nslookupTO = new NslookupTO(host,LocalDateTime.now());
		nslookupTO.setResultado(nslookupService.executar(nslookupTO, null));
		return Response.ok().entity(nslookupTO).build();
	}
	
	
	@GET
	@Path("/pentest")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dirb(@QueryParam(value = "host") String host) throws Exception {
		System.out.println("api-pentest= host:"+host);
		DirbTO dirbTO = new DirbTO(host,LocalDateTime.now());
		dirbTO.setResultado(dirbService.executar(dirbTO, null));
		return Response.ok().entity(dirbTO).build();
	}

	@GET
	@Path("/portcheck")
	@Produces(MediaType.APPLICATION_JSON)
	public Response portcheck(@QueryParam(value = "host") String host,
	                          @QueryParam(value = "port") String port) throws Exception {
		PortCheckTO to = new PortCheckTO(host, port, LocalDateTime.now());
		to.setResultado(portCheckService.executar(to, null));
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/reversedns")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reversedns(@QueryParam(value = "ip") String ip) throws Exception {
		ReverseDnsTO to = new ReverseDnsTO(ip, LocalDateTime.now());
		to.setResultado(reverseDnsService.executar(to, null));
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/myip")
	@Produces(MediaType.APPLICATION_JSON)
	public Response myip(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest req) throws Exception {
		String ip = req.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = req.getRemoteAddr();
		if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
		String ua = req.getHeader("User-Agent");
		String lang = req.getHeader("Accept-Language");
		MyIpTO to = myIpService.describe(ip, ua, lang);
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/mac")
	@Produces(MediaType.APPLICATION_JSON)
	public Response mac(@QueryParam(value = "mac") String mac) throws Exception {
		MacTO to = new MacTO(mac, LocalDateTime.now());
		to.setResultado(macService.executar(to, null));
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/httpstatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response httpstatus(@QueryParam(value = "url") String url) throws Exception {
		HttpStatusTO to = new HttpStatusTO(url, LocalDateTime.now());
		to.setResultado(httpStatusService.executar(to, null));
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/isitdown")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isitdown(@QueryParam(value = "host") String host) throws Exception {
		IsItDownTO to = new IsItDownTO(host, LocalDateTime.now());
		to.setResultado(isItDownService.executar(to, null));
		return Response.ok().entity(to).build();
	}

	@GET
	@Path("/ssl")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ssl(@QueryParam(value = "host") String host,
	                    @QueryParam(value = "port") String port) throws Exception {
		SslTO to = new SslTO(host, port, LocalDateTime.now());
		to.setResultado(sslService.executar(to, null));
		return Response.ok().entity(to).build();
	}
}