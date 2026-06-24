package io.github.iss_2025_2026.service.tts;

import io.github.iss_2025_2026.model.NpcVoiceConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TtsRequestTest {
    @Test
    public void usesNpcMistralVoiceInsteadOfGlobalFallback() {
        TtsConfig config = config(TtsProvider.MISTRAL);
        NpcVoiceConfig voice = new NpcVoiceConfig();
        voice.setMistralVoiceId("npc-voice-id");

        TtsRequest request = TtsRequest.of("Ciao", config, voice);

        assertEquals("npc-voice-id", request.getVoice());
        assertEquals(1f, request.getSpeed(), 0.001f);
    }

    @Test
    public void usesGlobalMistralVoiceWhenNpcVoiceIsMissing() {
        TtsRequest request = TtsRequest.of("Ciao", config(TtsProvider.MISTRAL), null);

        assertEquals("global-voice-id", request.getVoice());
    }

    @Test
    public void doesNotSendMistralUuidToLocalProvider() {
        NpcVoiceConfig voice = new NpcVoiceConfig();
        voice.setMistralVoiceId("npc-mistral-uuid");

        TtsRequest request = TtsRequest.of("Ciao", config(TtsProvider.LOCAL), voice);

        assertEquals("it_male", request.getVoice());
        assertEquals(0.9f, request.getSpeed(), 0.001f);
    }

    @Test
    public void usesNpcLocalVoiceWhenConfigured() {
        NpcVoiceConfig voice = new NpcVoiceConfig();
        voice.setLocalVoice("it_old_woman");

        TtsRequest request = TtsRequest.of("Ciao", config(TtsProvider.LOCAL), voice);

        assertEquals("it_old_woman", request.getVoice());
    }

    private TtsConfig config(TtsProvider provider) {
        return new TtsConfig(true, provider, "wav", 0.85f, 1000, 1000,
                new LocalTtsConfig("http://127.0.0.1:8000/v1", "/audio/speech",
                        "local-model", "it_male", 0.9f),
                new MistralTtsConfig("https://api.mistral.ai/v1", "/audio/speech", "/audio/voices",
                        "voxtral-mini-tts-2603", "global-voice-id"));
    }
}
