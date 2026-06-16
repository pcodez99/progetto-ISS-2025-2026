package io.github.iss_2025_2026.service.ai;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiServiceTest {
    @AfterEach
    public void tearDown() {
        AiService.resetAi();
    }

    @Test
    public void singletonCanBeReplacedAndReset() {
        AiService replacement = new AiService(testConfig(true), new StubClient("ciao"));

        AiService.setAi(replacement);

        assertSame(replacement, AiService.getAi());

        AiService.resetAi();

        assertNotSame(replacement, AiService.getAi());
    }

    @Test
    public void chatDelegatesToClientWithPreparedDefaults() throws Exception {
        StubClient client = new StubClient("risposta");
        AiService.setAi(new AiService(testConfig(true), client));

        AiResponse response = AiService.getAi().chat(AiRequest.chat("system", "user", 0.33f));

        assertEquals("risposta", response.getContent());
        assertNotNull(client.lastRequest);
        assertEquals("test-model", client.lastRequest.getModel());
        assertEquals(Boolean.FALSE, client.lastRequest.getStream());
        assertEquals(0.33f, client.lastRequest.getTemperature(), 0.001f);
    }

    @Test
    public void disabledConfigBlocksChat() {
        AiService.setAi(new AiService(testConfig(false), new StubClient("risposta")));

        assertThrows(AiException.class, () -> AiService.getAi().chat(AiRequest.chat("system", "user", 0.4f)));
    }

    private AiConfig testConfig(boolean enabled) {
        return new AiConfig(enabled, "http://localhost:11434", "test-model", "/api/chat", false,
                10, 10, 0.75f, 4000, true);
    }

    private static class StubClient implements AiClient {
        private final String content;
        private AiRequest lastRequest;

        private StubClient(String content) {
            this.content = content;
        }

        @Override
        public AiResponse chat(AiRequest request, AiConfig config) {
            this.lastRequest = request.withDefaults(config);
            return new AiResponse(content, config.getModel(), "{}");
        }

        @Override
        public boolean isAvailable(AiConfig config) {
            return true;
        }
    }
}
