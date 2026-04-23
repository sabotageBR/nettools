package live.nettools.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Resolve caminhos de downloads em um diretório controlado.
 * Evita path traversal: o nome recebido é validado contra um padrão estrito e
 * o caminho final tem que estar fisicamente contido no diretório-base.
 */
public final class DownloadPaths {

    private static final String ENV_DIR = "NETTOOLS_DOWNLOAD_DIR";
    private static final String DEFAULT_SUBDIR = "nettools-downloads";
    private static final Pattern FILENAME = Pattern.compile(
            "^nettools-video-[A-Fa-f0-9\\-]{36}\\.[A-Za-z0-9]{1,5}$");

    private DownloadPaths() {}

    public static Path directory() {
        String override = System.getenv(ENV_DIR);
        Path dir = (override != null && !override.isEmpty())
                ? Paths.get(override)
                : Paths.get(System.getProperty("java.io.tmpdir"), DEFAULT_SUBDIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar diretório de downloads: " + dir, e);
        }
        return dir;
    }

    /**
     * Valida o nome recebido e devolve o caminho absoluto dentro do diretório
     * oficial. Lança {@link IllegalArgumentException} se o nome for inválido
     * ou se após canonicalização apontar para fora do diretório.
     */
    public static File resolveSafe(String rawFilename) {
        if (rawFilename == null) {
            throw new IllegalArgumentException("filename ausente");
        }
        String name = rawFilename.trim();
        if (!FILENAME.matcher(name).matches()) {
            throw new IllegalArgumentException("filename inválido");
        }
        Path base;
        Path resolved;
        try {
            base = directory().toRealPath();
            resolved = base.resolve(name).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao resolver diretório de downloads", e);
        }
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("caminho fora do diretório permitido");
        }
        return resolved.toFile();
    }
}
