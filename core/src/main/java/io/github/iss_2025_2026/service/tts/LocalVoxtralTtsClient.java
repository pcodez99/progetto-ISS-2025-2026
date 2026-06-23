package io.github.iss_2025_2026.service.tts;

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

public class LocalVoxtralTtsClient implements TtsClient {
    private static final Logger LOGGER = Logger.getLogger(LocalVoxtralTtsClient.class.getName());
    private static final int MAX_AUDIO_BYTES = 20 * 1024 * 1024;

    private final TtsConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public LocalVoxtralTtsClient(TtsConfig config) {
        this.config = config;
    }

    @Override
    public TtsResult synthesize(TtsRequest request) throws TtsException {
        validateRequest(request);
        LocalTtsConfig local = config.getLocal();
        try {
            String requestJson = mapper.writeValueAsString(toRequestBody(request));
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(local.getSpeechUrl()))
                    .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            long startedAt = System.currentTimeMillis();
            HttpResponse<byte[]> response = buildClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            long elapsedMs = System.currentTimeMillis() - startedAt;
            byte[] audio = response.body() != null ? response.body() : new byte[0];
            LOGGER.info("[TTS/Local] HTTP " + response.statusCode() + " in " + elapsedMs
                    + "ms, bytes=" + audio.length + ", voice=" + request.getVoice());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(audio, StandardCharsets.UTF_8);
                throw new TtsException("Voxtral locale ha risposto con HTTP " + response.statusCode()
                        + ": " + truncate(errorBody));
            }
            validateAudio(audio);
            String contentType = response.headers().firstValue("Content-Type").orElse("audio/wav");
            return new TtsResult(audio, contentType);
        } catch (TtsException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "[TTS/Local] Chiamata REST fallita.", exception);
            throw new TtsException("Errore durante la chiamata REST a Voxtral locale.", exception);
        }
    }

    @Override
    public boolean isAvailable() {
        if (!config.isEnabled()) {
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.getLocal().getModelsUrl()))
                    .timeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                    .GET()
                    .build();
            HttpResponse<Void> response = buildClient().send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception exception) {
            return false;
        }
    }

    private HttpClient buildClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .build();
    }

    private Map<String, Object> toRequestBody(TtsRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", sanitizeText(request.getText()));
        body.put("model", config.getLocal().getModel());
        body.put("response_format", config.getResponseFormat());
        body.put("voice", request.getVoice());
        body.put("speed", request.getSpeed());
        return body;
    }

    private void validateRequest(TtsRequest request) throws TtsException {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new TtsException("Testo TTS vuoto.");
        }
    }

    private void validateAudio(byte[] audio) throws TtsException {
        if (audio.length == 0) {
            throw new TtsException("Voxtral locale ha restituito audio vuoto.");
        }
        if (audio.length > MAX_AUDIO_BYTES) {
            throw new TtsException("La risposta audio supera il limite tecnico di " + MAX_AUDIO_BYTES + " byte.");
        }
        if ("wav".equals(config.getResponseFormat()) && !hasRiffHeader(audio)) {
            throw new TtsException("Voxtral locale non ha restituito un file WAV valido.");
        }
    }

    static String sanitizeText(String text) {
        String sanitized = text.replaceAll("[\\u200B\\u200E\\u200F\\u2060\\uFEFF]", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (!sanitized.matches(".*[.!?…]$")) {
            sanitized += ".";
        }
        return sanitized;
    }

    static boolean hasRiffHeader(byte[] audio) {
        return audio.length >= 12
                && audio[0] == 'R' && audio[1] == 'I' && audio[2] == 'F' && audio[3] == 'F'
                && audio[8] == 'W' && audio[9] == 'A' && audio[10] == 'V' && audio[11] == 'E';
    }

    private String truncate(String text) {
        if (text == null || text.length() <= 500) {
            return text != null ? text : "";
        }
        return text.substring(0, 500) + "...";
    }
}
