package io.github.iss_2025_2026.service.tts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalVoxtralTtsClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer server;
    private TtsConfig config;

    @BeforeEach
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/models", exchange -> send(exchange, 200, "{}".getBytes(StandardCharsets.UTF_8),
                "application/json"));
        server.createContext("/v1/audio/speech", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            send(exchange, 200, minimalWav(), "audio/wav");
        });
        server.start();
        config = configFor(server.getAddress().getPort());
    }

    @AfterEach
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void callsLocalSpeechEndpointAndReturnsRawWav() throws Exception {
        LocalVoxtralTtsClient client = new LocalVoxtralTtsClient(config);

        TtsResult result = client.synthesize(new TtsRequest("Ciao picciotti", "it_male", 0.9f));

        assertEquals("audio/wav", result.getContentType());
        assertTrue(result.getAudioData().length >= 12);
        JsonNode json = mapper.readTree(requestBody.get());
        assertEquals("Ciao picciotti.", json.path("input").asText());
        assertEquals("local-model", json.path("model").asText());
        assertEquals("it_male", json.path("voice").asText());
        assertEquals("wav", json.path("response_format").asText());
        assertEquals(0.9, json.path("speed").asDouble(), 0.001);
    }

    @Test
    public void modelsEndpointReportsAvailability() {
        assertTrue(new LocalVoxtralTtsClient(config).isAvailable());
    }

    @Test
    public void rejectsNonWavResponse() {
        server.removeContext("/v1/audio/speech");
        server.createContext("/v1/audio/speech", exchange ->
                send(exchange, 200, "not audio".getBytes(StandardCharsets.UTF_8), "text/plain"));

        assertThrows(TtsException.class, () -> new LocalVoxtralTtsClient(config)
                .synthesize(new TtsRequest("Ciao", "it_male", 1f)));
    }

    private TtsConfig configFor(int port) {
        return TtsConfigTest.testConfig(TtsProvider.LOCAL, "http://127.0.0.1:" + port + "/v1",
                "/audio/speech", "https://api.mistral.ai/v1", "/audio/speech", "/audio/voices");
    }

    static byte[] minimalWav() {
        return new byte[] {
                'R', 'I', 'F', 'F', 4, 0, 0, 0,
                'W', 'A', 'V', 'E'
        };
    }

    static void send(HttpExchange exchange, int status, byte[] body, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
