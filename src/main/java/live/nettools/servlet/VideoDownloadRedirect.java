package live.nettools.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect 301 permanente de /videodownload para gettvid.com.
 * Substitui o antigo meta-refresh (que o Google trata como soft-404) por um
 * redirect HTTP que preserva PageRank.
 */
@WebServlet({ "/videodownload" })
public class VideoDownloadRedirect extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TARGET = "https://gettvid.com/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        resp.setHeader("Location", TARGET);
        resp.setHeader("Cache-Control", "public, max-age=86400");
    }
}
