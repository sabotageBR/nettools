package live.nettools.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import live.nettools.to.DirbTO;
import live.nettools.to.NmapTO;
import live.nettools.to.NslookupTO;
import live.nettools.to.PeoplesearchTO;
import live.nettools.to.PingTO;
import live.nettools.to.SipTO;
import live.nettools.to.SnmpTO;
import live.nettools.to.TracerouteTO;
import live.nettools.to.WhoisTO;
import live.nettools.to.YoutubeTO;

@Named
@SessionScoped
public class CustomIdentity implements Serializable{
	
	private static final long serialVersionUID = 7348450650298360640L;
	
	private List<WhoisTO> whoiss;
	
	private List<TracerouteTO> traceroutes;
	
	private List<NmapTO> nmaps;
	
	private List<SnmpTO> snmps;
	
	
	
	private String ipExterno;
	
	private List<PingTO> pings;
	public List<PingTO> getPings() {
		if (pings == null) {
			pings = new ArrayList<PingTO>();
		}

		return pings;
	}

	public void setPings(List<PingTO> pings) {
		this.pings = pings;
	}

	public List<WhoisTO> getWhoiss() {
		if (whoiss == null) {
			whoiss = new ArrayList<WhoisTO>();
		}

		return whoiss;
	}

	public void setWhoiss(List<WhoisTO> whoiss) {
		this.whoiss = whoiss;
	}
	
	
	

	public List<TracerouteTO> getTraceroutes() {
		if (traceroutes == null) {
			traceroutes = new ArrayList<TracerouteTO>();
		}

		return traceroutes;
	}

	public void setTraceroutes(List<TracerouteTO> traceroutes) {
		this.traceroutes = traceroutes;
	}
	
	
	
	public List<NmapTO> getNmaps() {
		if (nmaps == null) {
			nmaps = new ArrayList<NmapTO>();
		}

		return nmaps;
	}

	public void setNmaps(List<NmapTO> nmaps) {
		this.nmaps = nmaps;
	}
	
	
	
	public List<SnmpTO> getSnmps() {
		if (snmps == null) {
			snmps = new ArrayList<SnmpTO>();
		}

		return snmps;
	}

	public void setSnmps(List<SnmpTO> snmps) {
		this.snmps = snmps;
	}
	
	private List<SipTO> sips;
	
	public List<SipTO> getSips() {
		if (sips == null) {
			sips = new ArrayList<SipTO>();
		}

		return sips;
	}

	public void setSips(List<SipTO> sips) {
		this.sips = sips;
	}
	
	private List<NslookupTO> nslookups;
	
	public List<NslookupTO> getNslookups() {
		if (nslookups == null) {
			nslookups = new ArrayList<NslookupTO>();
		}

		return nslookups;
	}

	public void setNslookups(List<NslookupTO> nslookups) {
		this.nslookups = nslookups;
	}
	
	private List<DirbTO> dirbs;
	
	public List<DirbTO> getDirbs() {
		if (dirbs == null) {
			dirbs = new ArrayList<DirbTO>();
		}

		return dirbs;
	}

	public void setDirbs(List<DirbTO> dirbs) {
		this.dirbs = dirbs;
	}
	
	private List<YoutubeTO> youtubes;
	
	public List<YoutubeTO> getYoutubes() {
		if (youtubes == null) {
			youtubes = new ArrayList<YoutubeTO>();
		}

		return youtubes;
	}

	public void setYoutubes(List<YoutubeTO> youtubes) {
		this.youtubes = youtubes;
	}

	public String getIpExterno() {
		if (ipExterno == null) {
			ipExterno = new String();
		}
		return ipExterno;
	}

	public void setIpExterno(String ipExterno) {
		this.ipExterno = ipExterno;
	}
	
	
	private List<PeoplesearchTO> peoplesearchs;
	
	public List<PeoplesearchTO> getPeoplesearchs() {
		if (peoplesearchs == null) {
			peoplesearchs = new ArrayList<PeoplesearchTO>();
		}

		return peoplesearchs;
	}

	public void setPeoplesearchs(List<PeoplesearchTO> peoplesearchs) {
		this.peoplesearchs = peoplesearchs;
	}
	
	public void comporInformacoesHTTP() {
	}

}
