package live.nettools.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.Session;

import com.google.gson.Gson;

/**
 * Base para services que executam um comando externo e opcionalmente
 * transmitem a saída linha a linha via WebSocket.
 *
 * Substitui o antigo padrão {@code Runtime.exec(String)} que era vulnerável
 * a command injection pela combinação segura de {@link ProcessBuilder} com
 * argumentos como {@code List<String>}. Adiciona timeout, logging,
 * fechamento de recursos seguro e mensagem terminal "FIM" padronizada.
 */
public abstract class AbstractCommandService<TO> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AbstractCommandService.class.getName());
    protected static final Gson GSON = new Gson();

    /** Sentinela enviada ao cliente quando o comando termina, a qualquer título. */
    protected static final String END_MARKER = "FIM";
    protected static final String TIMEOUT_MARKER = "TIMEOUT";

    /**
     * Valida o TO de entrada antes da execução. Implementações devem lançar
     * {@link IllegalArgumentException} para entradas inseguras. A mensagem é
     * retornada ao cliente sem expor stack trace.
     */
    protected abstract void validate(TO input);

    /** Monta o comando como lista de argumentos. NUNCA concatene o input do usuário em um único String. */
    protected abstract List<String> buildCommand(TO input);

    /** Constrói o TO que será serializado como JSON e enviado ao cliente para uma linha de saída. */
    protected abstract TO buildMessageTO(TO input, String line);

    /** Timeout máximo da execução. Subclasses podem aumentar. */
    protected long timeoutSeconds() {
        return 60L;
    }

    /** Hook opcional para lógica pós-execução (ex.: YoutubeService avisa sobre o arquivo gerado). */
    protected void afterExecution(TO input, Session session, String aggregatedOutput) {
        // vazio por padrão
    }

    public String executar(TO input, Session session) {
        try {
            validate(input);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em {0}: {1}",
                    new Object[]{getClass().getSimpleName(), ex.getMessage()});
            emit(session, input, "Invalid input: " + ex.getMessage());
            emit(session, input, END_MARKER);
            return "";
        }

        List<String> command;
        try {
            command = buildCommand(input);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Entrada rejeitada em {0}: {1}",
                    new Object[]{getClass().getSimpleName(), ex.getMessage()});
            emit(session, input, "Invalid input: " + ex.getMessage());
            emit(session, input, END_MARKER);
            return "";
        }

        LOG.log(Level.INFO, "{0} executando: {1}",
                new Object[]{getClass().getSimpleName(), command});

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        StringBuilder aggregated = new StringBuilder();
        Process proc = null;
        boolean timedOut = false;

        try {
            proc = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds());
                while ((line = reader.readLine()) != null) {
                    emit(session, input, line);
                    aggregated.append(line).append(System.lineSeparator());
                    if (System.nanoTime() > deadline) {
                        timedOut = true;
                        break;
                    }
                }
            }
            if (!timedOut) {
                if (!proc.waitFor(timeoutSeconds(), TimeUnit.SECONDS)) {
                    timedOut = true;
                }
            }
            if (timedOut) {
                LOG.log(Level.WARNING, "Timeout em {0} ({1}s)",
                        new Object[]{getClass().getSimpleName(), timeoutSeconds()});
                emit(session, input, TIMEOUT_MARKER);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha de IO em " + getClass().getSimpleName(), e);
            emit(session, input, "Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Interrompido em " + getClass().getSimpleName(), e);
        } finally {
            if (proc != null && proc.isAlive()) {
                proc.destroy();
                try {
                    if (!proc.waitFor(2, TimeUnit.SECONDS)) {
                        proc.destroyForcibly();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    proc.destroyForcibly();
                }
            }
        }

        String output = aggregated.toString();
        try {
            afterExecution(input, session, output);
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Falha em afterExecution de " + getClass().getSimpleName(), ex);
        }
        emit(session, input, END_MARKER);
        return output;
    }

    protected void emit(Session session, TO input, String line) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(GSON.toJson(buildMessageTO(input, line)));
        } catch (Exception e) {
            LOG.log(Level.FINE, "Falha ao enviar mensagem WebSocket", e);
        }
    }
}
