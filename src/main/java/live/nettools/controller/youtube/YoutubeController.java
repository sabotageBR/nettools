package live.nettools.controller.youtube;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import live.nettools.controller.AbstractController;
import live.nettools.enums.TypeVideoDownload;
import live.nettools.session.CustomIdentity;
import live.nettools.to.YoutubeTO;
import live.nettools.util.UtilCollection;

@Model
public class YoutubeController extends AbstractController<YoutubeTO>{

	private @Inject CustomIdentity customIdentity;
	
	private static final long serialVersionUID = -5104849116507289650L;
	
	private UtilCollection<YoutubeTO> uc = new UtilCollection<YoutubeTO>();
	
	@PostConstruct
	private void init() {
		System.out.println(customIdentity.getIpExterno()+"- Nav: VideoDownload");
		comporInformacoesHTTP();
	}
	
	private void comporInformacoesHTTP() {
		getTo().setTipo(TypeVideoDownload.VIDEO);
	}
	
	public void youtube() {
		if(getTo().getHost() != null && !getTo().getHost().equals("")) {
			customIdentity.getYoutubes().add(new YoutubeTO(getTo().getHost(),getTo().getTipo(), LocalDateTime.now()));
			uc.ordenarListaDesc(customIdentity.getYoutubes(),"dateTimeOrder");
		} 
	}
	
}
