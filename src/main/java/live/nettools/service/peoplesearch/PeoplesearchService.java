package live.nettools.service.peoplesearch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.websocket.Session;

import com.google.gson.Gson;

import live.nettools.enums.PeoplesearchTypeEnum;
import live.nettools.to.PeopleSearchResultTO;
import live.nettools.to.PeoplesearchTO;
import live.nettools.util.HostValidator;
import live.nettools.util.OgImageResolver;
import live.nettools.util.SearxngClient;

/**
 * People Search consulta o DuckDuckGo (HTML endpoint) em vez do antigo
 * {@code googler}. Cada campo preenchido gera uma busca global e, quando
 * aplicável, uma busca filtrada por site (facebook.com, instagram.com, ...).
 * Os resultados são enviados em tempo real pelo WebSocket.
 */
@Named
public class PeoplesearchService implements Serializable {

    private static final long serialVersionUID = -6986454410309827919L;
    private static final Logger LOG = Logger.getLogger(PeoplesearchService.class.getName());

    private static final int MAX_RESULTS_PER_QUERY = 25;
    private static final long THROTTLE_MILLIS = 1000L;

    public List<PeopleSearchResultTO> executar(PeoplesearchTO input, Session session) {
        List<PeopleSearchResultTO> resultados = new ArrayList<PeopleSearchResultTO>();

        String nome;
        String facebook;
        String instagram;
        String twitter;
        String tiktok;
        try {
            nome = HostValidator.optionalFreeText(input.getNome(), "nome");
            facebook = HostValidator.optionalFreeText(input.getFacebook(), "facebook");
            instagram = HostValidator.optionalFreeText(input.getInstagram(), "instagram");
            twitter = HostValidator.optionalFreeText(input.getTwitter(), "twitter");
            tiktok = HostValidator.optionalFreeText(input.getTiktok(), "tiktok");
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em PeoplesearchService: {0}", ex.getMessage());
            sendStatus(session, "Invalid input: " + ex.getMessage());
            return resultados;
        }

        try {
            if (nome != null) {
                runQuery(session, resultados, input, PeoplesearchTypeEnum.GLOBAL, nome, null,
                        "Global search: \"" + nome + "\"");
                throttle();
            }
            if (facebook != null) {
                runQuery(session, resultados, input, PeoplesearchTypeEnum.GLOBAL, facebook, null,
                        "Facebook global: \"" + facebook + "\"");
                throttle();
                runQuery(session, resultados, input, PeoplesearchTypeEnum.FACEBOOK, facebook, "facebook.com",
                        "Facebook: \"" + facebook + "\"");
                throttle();
            }
            if (instagram != null) {
                String handle = instagram.startsWith("@") ? instagram : "@" + instagram;
                runQuery(session, resultados, input, PeoplesearchTypeEnum.GLOBAL, handle, null,
                        "Instagram global: \"" + handle + "\"");
                throttle();
                runQuery(session, resultados, input, PeoplesearchTypeEnum.INSTAGRAM, handle, "instagram.com",
                        "Instagram: \"" + handle + "\"");
                throttle();
            }
            if (twitter != null) {
                String handle = twitter.startsWith("@") ? twitter : "@" + twitter;
                runQuery(session, resultados, input, PeoplesearchTypeEnum.GLOBAL, handle, null,
                        "Twitter global: \"" + handle + "\"");
                throttle();
                runQuery(session, resultados, input, PeoplesearchTypeEnum.TWITTER, handle, "twitter.com",
                        "Twitter: \"" + handle + "\"");
                throttle();
            }
            if (tiktok != null) {
                String handle = tiktok.startsWith("@") ? tiktok : "@" + tiktok;
                runQuery(session, resultados, input, PeoplesearchTypeEnum.GLOBAL, handle, null,
                        "TikTok global: \"" + handle + "\"");
                throttle();
                runQuery(session, resultados, input, PeoplesearchTypeEnum.TIKTOK, handle, "tiktok.com",
                        "TikTok: \"" + handle + "\"");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return resultados;
    }

    private void runQuery(Session session,
                          List<PeopleSearchResultTO> resultados,
                          PeoplesearchTO input,
                          PeoplesearchTypeEnum tipo,
                          String term,
                          String siteFilter,
                          String statusMsg) {
        sendStatus(session, statusMsg);
        String query = siteFilter == null ? term : term + " site:" + siteFilter;
        List<SearxngClient.SearchResult> page;
        try {
            page = SearxngClient.search(query, MAX_RESULTS_PER_QUERY);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha ao consultar SearXNG em " + SearxngClient.baseUrl(), e);
            sendStatus(session, "Search temporarily unavailable (SearXNG unreachable at " + SearxngClient.baseUrl() + ")");
            return;
        }
        if (page.isEmpty()) {
            sendStatus(session, "No results");
            return;
        }
        List<String> urls = new ArrayList<String>();
        for (SearxngClient.SearchResult r : page) {
            PeopleSearchResultTO item = new PeopleSearchResultTO();
            item.setTitulo(r.title);
            item.setLink(r.url);
            item.setTexto(r.snippet == null ? "" : r.snippet);
            item.setTipo(tipo);
            resultados.add(item);
            sendResult(session, input, item);
            if (r.url != null) {
                urls.add(r.url);
            }
        }

        final Session sess = session;
        OgImageResolver.resolveManyAsync(urls, new java.util.function.BiConsumer<String, String>() {
            @Override
            public void accept(String pageUrl, String imageUrl) {
                sendImageUpdate(sess, pageUrl, imageUrl);
            }
        });
    }

    private void sendImageUpdate(Session session, String pageUrl, String imageUrl) {
        if (session == null || !session.isOpen()) {
            return;
        }
        PeopleSearchResultTO upd = new PeopleSearchResultTO();
        upd.setTitulo("IMAGE_UPDATE");
        upd.setLink(pageUrl);
        upd.setImagem(imageUrl);
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(new Gson().toJson(
                        new PeoplesearchTO("", upd)));
            } catch (Exception e) {
                LOG.log(Level.FINE, "Falha ao enviar IMAGE_UPDATE", e);
            }
        }
    }

    private void throttle() throws InterruptedException {
        Thread.sleep(THROTTLE_MILLIS);
    }

    private void sendStatus(Session session, String msg) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(new Gson().toJson(
                        new PeoplesearchTO("", new PeopleSearchResultTO("STATUS", msg))));
            } catch (Exception e) {
                LOG.log(Level.FINE, "Falha ao enviar status WebSocket", e);
            }
        }
    }

    private void sendResult(Session session, PeoplesearchTO input, PeopleSearchResultTO result) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(new Gson().toJson(
                        new PeoplesearchTO(input.getNome(), result)));
            } catch (Exception e) {
                LOG.log(Level.FINE, "Falha ao enviar resultado WebSocket", e);
            }
        }
    }
}
