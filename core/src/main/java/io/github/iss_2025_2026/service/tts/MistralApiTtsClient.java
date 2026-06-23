package io.github.iss_2025_2026.service.tts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MistralApiTtsClient implements TtsClient {
    private static final Logger LOGGER = Logger.getLogger(MistralApiTtsClient.class.getName());
    private static final int MAX_AUDIO_BYTES = 20 * 1024 * 1024;

    private final TtsConfig config;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public MistralApiTtsClient(TtsConfig config, String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key Mistral assente.");
        }
        this.config = config;
        this.apiKey = apiKey.trim();
    }

    @Override
    public TtsResult synthesize(TtsRequest request) throws TtsException {
        validateRequest(request);
        MistralTtsConfig mistral = config.getMistral();
        try {
            String requestJson = mapper.writeValueAsString(toRequestBody(request));
            HttpRequest httpRequest = authorizedRequest(mistral.getSpeechUrl())
                    .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            long startedAt = System.currentTimeMillis();
            HttpResponse<String> response = buildClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long elapsedMs = System.currentTimeMillis() - startedAt;
            LOGGER.info("[TTS/Mistral] HTTP " + response.statusCode() + " in " + elapsedMs
                    + "ms, chars=" + request.getText().length());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new TtsException("Mistral TTS ha risposto con HTTP " + response.statusCode()
                        + ": " + truncate(response.body()));
            }

            JsonNode root = mapper.readTree(response.body());
            String encodedAudio = root.path("audio_data").asText("");
            if (encodedAudio.isEmpty()) {
                throw new TtsException("Mistral TTS non ha restituito audio_data.");
            }

            byte[] audio;
            try {
                audio = Base64.getDecoder().decode(encodedAudio);
            } catch (IllegalArgumentException exception) {
                throw new TtsException("Mistral TTS ha restituito audio Base64 non valido.", exception);
            }
            validateAudio(audio);
            return new TtsResult(audio, "audio/" + config.getResponseFormat());
        } catch (TtsException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "[TTS/Mistral] Chiamata API fallita.", exception);
            throw new TtsException("Errore durante la chiamata API Mistral TTS.", exception);
        }
    }

    @Override
    public boolean isAvailable() {
        if (!config.isEnabled()) {
            return false;
        }
        String voicesUrl = config.getMistral().getVoicesUrl() + "?limit=1&type=preset";
        try {
            HttpRequest request = authorizedRequest(voicesUrl)
                    .timeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                    .GET()
                    .build();
            HttpResponse<Void> response = buildClient().send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception exception) {
            return false;
        }
    }

    private HttpRequest.Builder authorizedRequest(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + apiKey);
    }

    private HttpClient buildClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .build();
    }

    private Map<String, Object> toRequestBody(TtsRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", LocalVoxtralTtsClient.sanitizeText(request.getText()));
        body.put("model", config.getMistral().getModel());
        body.put("response_format", config.getResponseFormat());
        body.put("stream", false);
        if (request.getVoice() != null && !request.getVoice().trim().isEmpty()) {
            body.put("voice_id", request.getVoice().trim());
        }
        return body;
    }

    private void validateRequest(TtsRequest request) throws TtsException {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new TtsException("Testo TTS vuoto.");
        }
    }

    private void validateAudio(byte[] audio) throws TtsException {
        if (audio.length == 0) {
            throw new TtsException("Mistral TTS ha restituito audio vuoto.");
        }
        if (audio.length > MAX_AUDIO_BYTES) {
            throw new TtsException("La risposta audio supera il limite tecnico di " + MAX_AUDIO_BYTES + " byte.");
        }
        if ("wav".equals(config.getResponseFormat()) && !LocalVoxtralTtsClient.hasRiffHeader(audio)) {
            throw new TtsException("Mistral TTS non ha restituito un file WAV valido.");
        }
    }

    private String truncate(String text) {
        if (text == null || text.length() <= 500) {
            return text != null ? text : "";
        }
        return text.substring(0, 500) + "...";
    }
}
