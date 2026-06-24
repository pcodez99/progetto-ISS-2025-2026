package io.github.iss_2025_2026.service.tts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TtsClientCreatorTest {
    @Test
    public void localConcreteCreatorBuildsLocalConcreteProduct() {
        TtsConfig config = config(TtsProvider.LOCAL);

        TtsClient product = new LocalVoxtralTtsClientCreator().createClient(config);

        assertInstanceOf(LocalVoxtralTtsClient.class, product);
    }

    @Test
    public void mistralConcreteCreatorBuildsMistralConcreteProductFromEnvironmentKey() {
        TtsConfig config = config(TtsProvider.MISTRAL);
        MistralApiTtsClientCreator creator = new MistralApiTtsClientCreator() {
            @Override
            protected String readApiKey() {
                return "environment-test-key";
            }
        };

        TtsClient product = creator.createClient(config);

        assertInstanceOf(MistralApiTtsClient.class, product);
    }

    @Test
    public void mistralConcreteCreatorFailsFastWhenEnvironmentKeyIsMissing() {
        MistralApiTtsClientCreator creator = new MistralApiTtsClientCreator() {
            @Override
            protected String readApiKey() {
                return " ";
            }
        };

        assertThrows(IllegalStateException.class, () -> creator.createClient(config(TtsProvider.MISTRAL)));
    }

    @Test
    public void resolverSelectsTheTwoConcreteCreators() {
        assertInstanceOf(LocalVoxtralTtsClientCreator.class,
                TtsClientCreatorResolver.resolve(TtsProvider.LOCAL));
        assertInstanceOf(MistralApiTtsClientCreator.class,
                TtsClientCreatorResolver.resolve(TtsProvider.MISTRAL));
    }

    private TtsConfig config(TtsProvider provider) {
        return TtsConfigTest.testConfig(provider, "http://127.0.0.1:8000/v1", "/audio/speech",
                "https://api.mistral.ai/v1", "/audio/speech", "/audio/voices");
    }
}
