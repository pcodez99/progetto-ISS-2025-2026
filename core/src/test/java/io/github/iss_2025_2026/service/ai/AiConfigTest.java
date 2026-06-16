package io.github.iss_2025_2026.service.ai;

import io.github.iss_2025_2026.service.GameProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiConfigTest {
    @Test
    public void fromGamePropertiesUsesConfiguredDefaults() {
        AiConfig config = AiConfig.fromGameProperties();

        assertTrue(config.isEnabled());
        assertEquals("http://localhost:11434", config.getBaseUrl());
        assertEquals(GameProperties.getString(GameProperties.KEY_AI_MODEL, AiConfig.DEFAULT_MODEL),
                config.getModel());
        assertFalse(config.getModel().trim().isEmpty());
        assertEquals("/api/chat", config.getChatEndpoint());
        assertEquals("http://localhost:11434/api/chat", config.getChatUrl());
        assertFalse(config.isStream());
        assertEquals(5000, config.getConnectTimeoutMs());
        assertEquals(30000, config.getReadTimeoutMs());
        assertEquals(0.75f, config.getDefaultTemperature(), 0.001f);
        assertEquals(4000, config.getMaxPromptChars());
        assertTrue(config.isFallbackToStaticDialogue());
    }

    @Test
    public void constructorNormalizesUrlAndEndpoint() {
        AiConfig config = new AiConfig(true, "http://localhost:11434/", "test", "api/chat", false,
                1, 1, 0.4f, 512, true);

        assertEquals("http://localhost:11434", config.getBaseUrl());
        assertEquals("/api/chat", config.getChatEndpoint());
    }
}
