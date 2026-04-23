package live.nettools.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.nettools.util.DownloadPaths;

@WebServlet({ "/download/youtube" })
public class DownloadYoutube extends HttpServlet {

    private static final long serialVersionUID = 4043679811548773728L;
    private static final Logger LOG = Logger.getLogger(DownloadYoutube.class.getName());
    private static final int BUFFER_SIZE = 8 * 1024;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rawName = req.getParameter("file");
        File arquivo;
        try {
            arquivo = DownloadPaths.resolveSafe(rawName);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Download rejeitado: {0}", ex.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!arquivo.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + arquivo.getName() + "\"");
        resp.setContentLengthLong(arquivo.length());

        try (FileInputStream fis = new FileInputStream(arquivo)) {
            OutputStream os = resp.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha ao servir download", e);
            throw e;
        }
    }
}
