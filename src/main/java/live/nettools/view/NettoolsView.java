package live.nettools.view;

import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@URLMappings(mappings={
		
		  @URLMapping(id = "ping", pattern = "/ping", viewId = "/pages/ping/ping.xhtml"),
		  @URLMapping(id = "traceroute", pattern = "/traceroute", viewId = "/pages/traceroute/traceroute.xhtml"),
		  @URLMapping(id = "whois", pattern = "/whois", viewId = "/pages/whois/whois.xhtml"),
		  @URLMapping(id = "nmap", pattern = "/nmap", viewId = "/pages/nmap/nmap.xhtml"),
		  @URLMapping(id = "snmp", pattern = "/snmp", viewId = "/pages/snmp/snmp.xhtml"),
		  @URLMapping(id = "sip", pattern = "/sip", viewId = "/pages/sip/sip.xhtml"),
		  
		  @URLMapping(id = "nslookup", pattern = "/nslookup", viewId = "/pages/nslookup/nslookup.xhtml"),
		  @URLMapping(id = "dirb", pattern = "/dirb", viewId = "/pages/dirb/dirb.xhtml"),
		  @URLMapping(id = "videodownload", pattern = "/videodownload", viewId = "/pages/youtube/youtube.xhtml"),
		  @URLMapping(id = "peoplesearch", pattern = "/peoplesearch", viewId = "/pages/peoplesearch/peoplesearch.xhtml"),
		  
		  @URLMapping(id = "blog", pattern = "/blog", viewId = "/pages/blog/blog.xhtml"),
		  @URLMapping(id = "live", pattern = "/live", viewId = "/pages/live/live.xhtml"),
		  
		  @URLMapping(id = "blog-artigo-1", pattern = "/blog/article-1", viewId = "/pages/blog/artigo-1.xhtml"),
		  @URLMapping(id = "blog-artigo-1-url", pattern = "/blog/article-1/#{parametro}", viewId = "/pages/blog/artigo-1.xhtml"),
		  
		  @URLMapping(id = "blog-artigo-2", pattern = "/blog/article-2", viewId = "/pages/blog/artigo-2.xhtml"),
		  @URLMapping(id = "blog-artigo-2-url", pattern = "/blog/article-2/#{parametro}", viewId = "/pages/blog/artigo-2.xhtml"),
		  
		  @URLMapping(id = "blog-artigo-3", pattern = "/blog/article-3", viewId = "/pages/blog/artigo-3.xhtml"),
		  @URLMapping(id = "blog-artigo-3-url", pattern = "/blog/article-3/#{parametro}", viewId = "/pages/blog/artigo-3.xhtml"),
		  
		  @URLMapping(id = "blog-artigo-4", pattern = "/blog/article-4", viewId = "/pages/blog/artigo-4.xhtml"),
		  @URLMapping(id = "blog-artigo-4-url", pattern = "/blog/article-4/#{parametro}", viewId = "/pages/blog/artigo-4.xhtml")
		  
		})
public class NettoolsView {
	

}
