package io.github.iss_2025_2026.service.tts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TtsConfigTest {
    @Test
    public void normalizesLocalAndMistralSettings() {
        TtsConfig config = new TtsConfig(true, TtsProvider.MISTRAL, "WAV", 10f, 0, 0,
                new LocalTtsConfig("http://127.0.0.1:8000/v1/", "audio/speech",
                        "local-model", "it_male", 0.9f),
                new MistralTtsConfig("https://api.mistral.ai/v1/", "audio/speech", "audio/voices",
                        "voxtral-mini-tts-2603", "cloud-voice-id"));

        assertEquals(TtsProvider.MISTRAL, config.getProvider());
        assertEquals("http://127.0.0.1:8000/v1/audio/speech", config.getLocal().getSpeechUrl());
        assertEquals("http://127.0.0.1:8000/v1/models", config.getLocal().getModelsUrl());
        assertEquals("https://api.mistral.ai/v1/audio/speech", config.getMistral().getSpeechUrl());
        assertEquals("https://api.mistral.ai/v1/audio/voices", config.getMistral().getVoicesUrl());
        assertEquals("wav", config.getResponseFormat());
        assertEquals(1f, config.getVolume(), 0.001f);
        assertEquals(4f, config.getGain(), 0.001f);
        assertEquals(1, config.getConnectTimeoutMs());
        assertEquals(1, config.getReadTimeoutMs());
    }

    @Test
    public void gamePropertiesProvideLocalDevelopmentDefaults() {
        TtsConfig config = TtsConfig.fromGameProperties();

        assertTrue(config.isEnabled());
        assertEquals(TtsProvider.LOCAL, config.getProvider());
        assertEquals("mlx-community/Voxtral-4B-TTS-2603-mlx-4bit", config.getLocal().getModel());
        assertEquals("it_male", config.getLocal().getVoice());
        assertEquals("voxtral-mini-tts-2603", config.getMistral().getModel());
        assertEquals("wav", config.getResponseFormat());
        assertEquals(4f, config.getGain(), 0.001f);
    }

    @Test
    public void rejectsUnknownProvider() {
        assertThrows(IllegalArgumentException.class, () -> TtsProvider.from("unknown"));
    }

    static TtsConfig testConfig(TtsProvider provider, String localBaseUrl, String localSpeechEndpoint,
            String mistralBaseUrl, String mistralSpeechEndpoint, String voicesEndpoint) {
        return new TtsConfig(true, provider, "WAV", 1f, 2000, 2000,
                new LocalTtsConfig(localBaseUrl, localSpeechEndpoint, "local-model", "it_male", 0.9f),
                new MistralTtsConfig(mistralBaseUrl, mistralSpeechEndpoint, voicesEndpoint,
                        "voxtral-mini-tts-2603", "cloud-voice-id"));
    }
}
