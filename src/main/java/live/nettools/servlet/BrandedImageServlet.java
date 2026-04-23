package live.nettools.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gera dinamicamente a Open Graph image (1200×630) e o favicon PNG (192×192)
 * com branding "NET TOOLS LIVE". Evita depender de um designer externo.
 *
 * Rotas:
 *   GET /og-image.png     → banner social 1200×630
 *   GET /icon-192.png     → ícone quadrado 192×192 pra Android/Google
 *
 * Resposta com Cache-Control agressivo (30 dias) — o browser e qualquer CDN
 * na frente cacheiam, o custo de geração acontece só uma vez por deploy.
 */
@WebServlet(urlPatterns = { "/og-image.png", "/icon-192.png" })
public class BrandedImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(BrandedImageServlet.class.getName());

    private static final Color BG_LEFT   = new Color(0x22, 0x2b, 0x3c);
    private static final Color BG_RIGHT  = new Color(0x62, 0xcb, 0x31);
    private static final Color BLUE      = new Color(0x23, 0xc6, 0xc8);
    private static final Color GREEN     = new Color(0x62, 0xcb, 0x31);
    private static final Color ORANGE    = new Color(0xf7, 0xa8, 0x35);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getRequestURI();
        BufferedImage img;
        try {
            if (path != null && path.endsWith("og-image.png")) {
                img = generateOgImage();
            } else {
                img = generateIcon();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Falha ao gerar imagem " + path, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        resp.setContentType("image/png");
        resp.setHeader("Cache-Control", "public, max-age=2592000, immutable");
        ImageIO.write(img, "png", resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    private BufferedImage generateOgImage() {
        int w = 1200, h = 630;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // gradient background
        g.setPaint(new GradientPaint(0, 0, BG_LEFT, w, h, new Color(0x2b, 0x5e, 0x2e)));
        g.fillRect(0, 0, w, h);

        // decorative rings top-right
        g.setColor(new Color(255, 255, 255, 20));
        for (int r = 120; r <= 500; r += 90) {
            g.drawOval(w - r / 2 - 40, -r / 2 + 40, r, r);
        }

        // Big "NET TOOLS LIVE" like nav labels, stacked into one row
        Font bigBold = new Font(Font.SANS_SERIF, Font.BOLD, 120);
        g.setFont(bigBold);
        FontMetrics fm = g.getFontMetrics();
        int baseY = 250;
        int x = 80;

        drawLabel(g, fm, "NET",   BLUE,   x, baseY); x += fm.stringWidth("NET")   + 24;
        drawLabel(g, fm, "TOOLS", GREEN,  x, baseY); x += fm.stringWidth("TOOLS") + 24;
        drawLabel(g, fm, "LIVE",  ORANGE, x, baseY);

        // Tagline
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        g.setColor(new Color(240, 240, 240));
        g.drawString("Free online network tools", 80, baseY + 90);

        // Tool list
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        g.setColor(new Color(200, 220, 200));
        g.drawString("ping · traceroute · whois · nslookup · nmap · SSL · port check", 80, baseY + 160);
        g.drawString("SNMP · SIP · pentest · reverse DNS · subnet · MAC · is it down", 80, baseY + 205);

        // Bottom URL
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("nettools.live", 80, h - 60);

        g.dispose();
        return img;
    }

    private void drawLabel(Graphics2D g, FontMetrics fm, String text, Color bg, int x, int baselineY) {
        int padX = 18, padY = 12;
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        int boxH = th + padY * 2;
        int boxY = baselineY - th - padY + fm.getDescent();
        g.setColor(bg);
        g.fillRoundRect(x, boxY, tw + padX * 2, boxH, 18, 18);
        g.setColor(Color.WHITE);
        g.drawString(text, x + padX, baselineY);
    }

    private BufferedImage generateIcon() {
        int s = 192;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // green rounded square
        g.setColor(GREEN);
        g.fillRoundRect(0, 0, s, s, 36, 36);

        // white "NT" centered
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 110));
        FontMetrics fm = g.getFontMetrics();
        String label = "NT";
        int tw = fm.stringWidth(label);
        int ascent = fm.getAscent();
        g.drawString(label, (s - tw) / 2, (s + ascent) / 2 - 10);

        g.dispose();
        return img;
    }
}
