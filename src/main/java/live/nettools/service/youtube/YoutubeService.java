package live.nettools.service.youtube;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Named;
import javax.websocket.Session;

import live.nettools.enums.TypeVideoDownload;
import live.nettools.service.AbstractCommandService;
import live.nettools.to.YoutubeTO;
import live.nettools.util.DownloadPaths;
import live.nettools.util.HostValidator;

@Named
public class YoutubeService extends AbstractCommandService<YoutubeTO> {

    private static final long serialVersionUID = -6986454410309827919L;
    private static final String[] POSSIBLE_EXTENSIONS = { "mp4", "mkv", "webm", "ogg", "mp3" };

    private transient String filenameBase;
    private transient String preferredExtension;

    @Override
    protected void validate(YoutubeTO youtube) {
        HostValidator.requireUrl(youtube.getHost(), "http", "https");
        if (youtube.getTipo() == null) {
            throw new IllegalArgumentException("tipo é obrigatório");
        }
    }

    @Override
    protected List<String> buildCommand(YoutubeTO youtube) {
        Path dir = DownloadPaths.directory();
        filenameBase = "nettools-video-" + UUID.randomUUID().toString();
        String primary;
        List<String> cmd = new ArrayList<String>();
        cmd.add("youtube-dl");
        if (TypeVideoDownload.MP3.equals(youtube.getTipo())) {
            preferredExtension = "mp3";
            primary = filenameBase + ".mp3";
            cmd.add("-o");
            cmd.add(dir.resolve(primary).toString());
            cmd.add("--extract-audio");
            cmd.add("--audio-format");
            cmd.add("mp3");
            cmd.add("--audio-quality");
            cmd.add("0");
        } else if (TypeVideoDownload.VIDEO.equals(youtube.getTipo())) {
            preferredExtension = "mp4";
            primary = filenameBase + ".mp4";
            cmd.add("-f");
            cmd.add("best");
            cmd.add("-o");
            cmd.add(dir.resolve(primary).toString());
        } else {
            preferredExtension = "mp4";
            primary = filenameBase + ".mp4";
            cmd.add("-o");
            cmd.add(dir.resolve(primary).toString());
        }
        cmd.add("--");
        cmd.add(youtube.getHost().trim());
        return cmd;
    }

    @Override
    protected YoutubeTO buildMessageTO(YoutubeTO input, String line) {
        return new YoutubeTO(input.getHost(), line);
    }

    @Override
    protected long timeoutSeconds() {
        return 600L;
    }

    @Override
    protected void afterExecution(YoutubeTO input, Session session, String aggregatedOutput) {
        if (session == null || filenameBase == null) {
            return;
        }
        Path dir = DownloadPaths.directory();
        String finalName = null;
        String preferred = filenameBase + "." + preferredExtension;
        File preferredFile = dir.resolve(preferred).toFile();
        if (preferredFile.exists() && preferredFile.length() > 0) {
            finalName = preferred;
        } else {
            for (String ext : POSSIBLE_EXTENSIONS) {
                String candidate = filenameBase + "." + ext;
                File f = dir.resolve(candidate).toFile();
                if (f.exists() && f.length() > 0) {
                    finalName = candidate;
                    break;
                }
            }
        }
        if (finalName != null) {
            emit(session, input, "button-down:" + finalName);
        } else {
            emit(session, input, "button-error:");
        }
    }
}
