package io.github.iss_2025_2026.service.ai;

import java.util.logging.Logger;

public class AiService {
    private static final Logger LOGGER = Logger.getLogger(AiService.class.getName());
    private static volatile AiService ai;

    private final AiConfig config;
    private final AiClient client;

    public AiService(AiConfig config, AiClient client) {
        this.config = config != null ? config : AiConfig.fromGameProperties();
        this.client = client != null ? client : new OllamaAiClient();
    }

    public static AiService getAi() {
        AiService current = ai;
        if (current == null) {
            synchronized (AiService.class) {
                current = ai;
                if (current == null) {
                    current = createDefault();
                    ai = current;
                }
            }
        }
        return current;
    }

    public static void setAi(AiService replacement) {
        synchronized (AiService.class) {
            ai = replacement != null ? replacement : createDefault();
        }
    }

    public static void resetAi() {
        synchronized (AiService.class) {
            ai = createDefault();
        }
    }

    public AiResponse chat(AiRequest request) throws AiException {
        if (!config.isEnabled()) {
            LOGGER.warning("[AI] Chat bloccata: servizio disabilitato da game.properties.");
            throw new AiException("Servizio AI disabilitato da game.properties.");
        }
        if (request == null) {
            LOGGER.warning("[AI] Chat bloccata: richiesta nulla.");
            throw new AiException("Richiesta AI nulla.");
        }
        AiRequest preparedRequest = request.withDefaults(config);
        LOGGER.info("[AI] Chat request preparata: model=" + preparedRequest.getModel()
                + ", url=" + config.getChatUrl()
                + ", messages=" + preparedRequest.getMessages().size()
                + ", stream=" + preparedRequest.getStream()
                + ", temperature=" + preparedRequest.getTemperature());
        AiResponse response = client.chat(preparedRequest, config);
        LOGGER.info("[AI] Chat response ricevuta: model=" + response.getModel()
                + ", contentLength=" + safeLength(response.getContent()));
        return response;
    }

    public boolean isAvailable() {
        boolean available = config.isEnabled() && client.isAvailable(config);
        LOGGER.info("[AI] Availability check: enabled=" + config.isEnabled()
                + ", baseUrl=" + config.getBaseUrl()
                + ", available=" + available);
        return available;
    }

    public AiConfig getConfig() {
        return config;
    }

    public AiClient getClient() {
        return client;
    }

    private static AiService createDefault() {
        AiConfig config = AiConfig.fromGameProperties();
        LOGGER.info("[AI] Creazione servizio AI default: enabled=" + config.isEnabled()
                + ", baseUrl=" + config.getBaseUrl()
                + ", chatUrl=" + config.getChatUrl()
                + ", model=" + config.getModel()
                + ", stream=" + config.isStream()
                + ", connectTimeoutMs=" + config.getConnectTimeoutMs()
                + ", readTimeoutMs=" + config.getReadTimeoutMs());
        return new AiService(config, new OllamaAiClient());
    }

    private static int safeLength(String value) {
        return value != null ? value.length() : 0;
    }
}
