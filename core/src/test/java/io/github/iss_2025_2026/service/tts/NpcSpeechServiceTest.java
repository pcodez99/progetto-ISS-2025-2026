package io.github.iss_2025_2026.service.tts;

import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcVoiceConfig;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NpcSpeechServiceTest {
    @Test
    public void forwardsNpcVoiceToTtsClient() throws Exception {
        AtomicReference<TtsRequest> capturedRequest = new AtomicReference<>();
        CountDownLatch synthesized = new CountDownLatch(1);
        TtsClient client = new TtsClient() {
            @Override
            public TtsResult synthesize(TtsRequest request) {
                capturedRequest.set(request);
                synthesized.countDown();
                return new TtsResult(minimalWav(), "audio/wav");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };
        NpcSpeechService service = new NpcSpeechService(config(), client);

        try {
            Npc npc = new Npc();
            NpcVoiceConfig voice = new NpcVoiceConfig();
            voice.setMistralVoiceId("npc-voice-id");
            npc.setVoice(voice);

            service.speak(npc, "Ciao picciotti");

            assertTrue(synthesized.await(2, TimeUnit.SECONDS));
            assertEquals("npc-voice-id", capturedRequest.get().getVoice());
            assertEquals("Ciao picciotti", capturedRequest.get().getText());
        } finally {
            service.close();
        }
    }

    private TtsConfig config() {
        return new TtsConfig(true, TtsProvider.MISTRAL, "wav", 0.85f, 1000, 1000,
                new LocalTtsConfig("http://127.0.0.1:8000/v1", "/audio/speech",
                        "local-model", "it_male", 1f),
                new MistralTtsConfig("https://api.mistral.ai/v1", "/audio/speech", "/audio/voices",
                        "voxtral-mini-tts-2603", "global-voice-id"));
    }

    private byte[] minimalWav() {
        return new byte[] {
                'R', 'I', 'F', 'F', 4, 0, 0, 0,
                'W', 'A', 'V', 'E'
        };
    }
}
