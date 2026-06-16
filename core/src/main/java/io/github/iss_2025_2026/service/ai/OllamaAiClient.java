package io.github.iss_2025_2026.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OllamaAiClient implements AiClient {
    private static final Logger LOGGER = Logger.getLogger(OllamaAiClient.class.getName());
    private static final int LOG_TEXT_LIMIT = 8000;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AiResponse chat(AiRequest request, AiConfig config) throws AiException {
        if (request == null) {
            throw new AiException("Richiesta AI nulla.");
        }
        AiRequest preparedRequest = request.withDefaults(config);
        try {
            String requestJson = mapper.writeValueAsString(toOllamaBody(preparedRequest));
            byte[] body = requestJson.getBytes(StandardCharsets.UTF_8);
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(config.getChatUrl()))
                    .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            LOGGER.info("[AI/Ollama] POST " + config.getChatUrl());
            LOGGER.info("[AI/Ollama] Request JSON: " + truncateForLog(requestJson));
            long startedAt = System.currentTimeMillis();
            HttpResponse<String> response = buildClient(config)
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long elapsedMs = System.currentTimeMillis() - startedAt;
            int status = response.statusCode();
            String responseText = response.body() != null ? response.body() : "";
            LOGGER.info("[AI/Ollama] HTTP " + status + " ricevuto in " + elapsedMs + "ms");
            LOGGER.info("[AI/Ollama] Raw response: " + truncateForLog(responseText));
            if (status < 200 || status >= 300) {
                LOGGER.warning("[AI/Ollama] Risposta HTTP non valida: " + status
                        + " body=" + truncateForLog(responseText));
                throw new AiException("Ollama ha risposto con HTTP " + status + ": " + responseText);
            }

            AiResponse parsedResponse = parseResponse(responseText, preparedRequest.getModel());
            LOGGER.info("[AI/Ollama] Parsed response model=" + parsedResponse.getModel()
                    + ", contentLength=" + safeLength(parsedResponse.getContent()));
            LOGGER.info("[AI/Ollama] Model content: " + truncateForLog(parsedResponse.getContent()));
            return parsedResponse;
        } catch (AiException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "[AI/Ollama] Errore durante la chiamata REST a Ollama.", exception);
            throw new AiException("Errore durante la chiamata REST a Ollama.", exception);
        }
    }

    @Override
    public boolean isAvailable(AiConfig config) {
        String tagsUrl = config.getBaseUrl() + "/api/tags";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tagsUrl))
                    .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .GET()
                    .build();
            LOGGER.info("[AI/Ollama] Availability GET " + tagsUrl);
            HttpResponse<String> response = buildClient(config)
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseText = response.body() != null ? response.body() : "";
            LOGGER.info("[AI/Ollama] Availability HTTP " + response.statusCode()
                    + " body=" + truncateForLog(responseText));
            boolean available = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!available) {
                LOGGER.warning("[AI/Ollama] Availability fallita per " + tagsUrl);
            }
            return available;
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "[AI/Ollama] Availability error per " + tagsUrl, exception);
            return false;
        }
    }

    private HttpClient buildClient(AiConfig config) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .build();
    }

    private Map<String, Object> toOllamaBody(AiRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel());
        body.put("stream", request.getStream());
        body.put("messages", request.getMessages());

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", request.getTemperature());
        body.put("options", options);
        return body;
    }

    private AiResponse parseResponse(String responseText, String fallbackModel) throws Exception {
        JsonNode root = mapper.readTree(responseText);
        String content = "";
        JsonNode messageContent = root.path("message").path("content");
        if (!messageContent.isMissingNode()) {
            content = messageContent.asText();
        } else if (!root.path("response").isMissingNode()) {
            content = root.path("response").asText();
        }
        String model = !root.path("model").isMissingNode() ? root.path("model").asText() : fallbackModel;
        return new AiResponse(content, model, responseText);
    }

    private static String truncateForLog(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= LOG_TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, LOG_TEXT_LIMIT) + "... [truncated, length=" + text.length() + "]";
    }

    private static int safeLength(String value) {
        return value != null ? value.length() : 0;
    }
}
