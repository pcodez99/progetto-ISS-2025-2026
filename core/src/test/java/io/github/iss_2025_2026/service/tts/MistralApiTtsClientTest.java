package io.github.iss_2025_2026.service.tts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MistralApiTtsClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer server;
    private TtsConfig config;

    @BeforeEach
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/audio/voices", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            LocalVoxtralTtsClientTest.send(exchange, 200, "{\"items\":[]}".getBytes(StandardCharsets.UTF_8),
                    "application/json");
        });
        server.createContext("/v1/audio/speech", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(LocalVoxtralTtsClientTest.minimalWav());
            String response = "{\"audio_data\":\"" + encoded + "\"}";
            LocalVoxtralTtsClientTest.send(exchange, 200, response.getBytes(StandardCharsets.UTF_8),
                    "application/json");
        });
        server.start();
        int port = server.getAddress().getPort();
        config = TtsConfigTest.testConfig(TtsProvider.MISTRAL, "http://127.0.0.1:8000/v1",
                "/audio/speech", "http://127.0.0.1:" + port + "/v1", "/audio/speech", "/audio/voices");
    }

    @AfterEach
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void callsMistralEndpointAndDecodesBase64Audio() throws Exception {
        MistralApiTtsClient client = new MistralApiTtsClient(config, "secret-test-key");

        TtsResult result = client.synthesize(new TtsRequest("Ciao picciotti", "cloud-voice-id", 1f));

        assertEquals("Bearer secret-test-key", authorization.get());
        assertTrue(result.getAudioData().length >= 12);
        JsonNode json = mapper.readTree(requestBody.get());
        assertEquals("Ciao picciotti.", json.path("input").asText());
        assertEquals("voxtral-mini-tts-2603", json.path("model").asText());
        assertEquals("cloud-voice-id", json.path("voice_id").asText());
        assertEquals("wav", json.path("response_format").asText());
        assertFalse(json.has("voice"));
        assertFalse(json.has("speed"));
        assertFalse(requestBody.get().contains("secret-test-key"));
    }

    @Test
    public void voicesEndpointReportsAvailabilityWithBearerAuth() {
        MistralApiTtsClient client = new MistralApiTtsClient(config, "secret-test-key");

        assertTrue(client.isAvailable());
        assertEquals("Bearer secret-test-key", authorization.get());
    }

    @Test
    public void rejectsInvalidBase64Response() {
        server.removeContext("/v1/audio/speech");
        server.createContext("/v1/audio/speech", exchange ->
                LocalVoxtralTtsClientTest.send(exchange, 200,
                        "{\"audio_data\":\"%%%\"}".getBytes(StandardCharsets.UTF_8), "application/json"));

        assertThrows(TtsException.class, () -> new MistralApiTtsClient(config, "secret-test-key")
                .synthesize(new TtsRequest("Ciao", "", 1f)));
    }
}
