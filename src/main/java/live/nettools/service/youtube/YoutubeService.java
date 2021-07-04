package live.nettools.service.youtube;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.enums.TypeVideoDownload;
import live.nettools.to.YoutubeTO;

@Named
public class YoutubeService implements Serializable{

	private static final long serialVersionUID = -6986454410309827919L;

	public String executar(YoutubeTO youtube, Session session) {
		Process proc = null;
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String s = null;
		boolean mkv = false;
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		
		try {
			Runtime rt = Runtime.getRuntime();
			String command = "";
			String nomeArquivo = "nettools-video-"+UUID.randomUUID().toString();
			String nomeArquivoCompleto = nomeArquivo+".mp4";
			String nomeFinal = "";
			if(youtube.getTipo().equals(TypeVideoDownload.VIDEO)) {
				
				command = String.format("youtube-dl -f best -o %s %s",nomeArquivoCompleto, youtube.getHost());
			}else if(youtube.getTipo().equals(TypeVideoDownload.MP3)) {
				nomeArquivoCompleto = nomeArquivo+".mp3";
				command = String.format("youtube-dl -o %s --extract-audio --audio-format mp3 --audio-quality 0 %s",nomeArquivoCompleto, youtube.getHost());
			}else {
				command = String.format("youtube-dl -o %s %s",nomeArquivoCompleto, youtube.getHost());
			}
			System.out.println(command);
			proc = rt.exec(command);
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
				if(session != null) {
					if(s.contains(".mkv")) {
						mkv = true;
					}
					session.getBasicRemote().sendText(gson.toJson(new YoutubeTO(youtube.getHost(), s)));
				}	
			}
			
			while ((s = stdError.readLine()) != null) {
				if(session != null) {
					session.getBasicRemote().sendText(gson.toJson(new YoutubeTO(youtube.getHost(), s)));
				}	
			}
			if(session != null) {
				if(new File(nomeArquivoCompleto).exists() && new File(nomeArquivoCompleto).length() > 0) {
					nomeFinal = nomeArquivoCompleto;
				}else if(new File(nomeArquivoCompleto+".mkv").exists() && new File(nomeArquivoCompleto+".mkv").length() > 0) {
					nomeFinal = nomeArquivoCompleto+".mkv";
				}else if(new File(nomeArquivoCompleto+".webm").exists() && new File(nomeArquivoCompleto+".webm").length() > 0) {
					nomeFinal = nomeArquivoCompleto+".webm";
				}else if(new File(nomeArquivo+".mkv").exists() && new File(nomeArquivo+".mkv").length() > 0) {
					nomeFinal = nomeArquivo+".mkv";
				}else if(new File(nomeArquivoCompleto+".ogg").exists() && new File(nomeArquivoCompleto+".ogg").length() > 0) {
					nomeFinal = nomeArquivoCompleto+".ogg";
				
				}else if(new File(nomeArquivoCompleto+".mp3").exists() && new File(nomeArquivoCompleto+".mp3").length() > 0) {
					nomeFinal = nomeArquivoCompleto+".mp3";	
				}
				
				if(!nomeFinal.equals("")) {
					session.getBasicRemote().sendText(gson.toJson(new YoutubeTO(youtube.getHost(), "button-down:"+nomeFinal)));
				}else {
					System.out.println("video não exportado");
					session.getBasicRemote().sendText(gson.toJson(new YoutubeTO(youtube.getHost(), "button-error:"+nomeFinal)));
				}
				session.getBasicRemote().sendText(gson.toJson(new YoutubeTO(youtube.getHost(), "FIM")));
			}
			sb.append("https://nettools.live/download/youtube?file="+nomeFinal);
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			try {
				Field f = proc.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				stdInput.close();
				stdError.close();
				proc.destroy();
				proc.destroyForcibly();
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
}
