package io.github.iss_2025_2026.service.tts;

public final class TtsClientCreatorResolver {
    private TtsClientCreatorResolver() {
    }

    public static TtsClientCreator resolve(TtsProvider provider) {
        TtsProvider selected = provider != null ? provider : TtsProvider.LOCAL;
        switch (selected) {
            case LOCAL:
                return new LocalVoxtralTtsClientCreator();
            case MISTRAL:
                return new MistralApiTtsClientCreator();
            default:
                throw new IllegalArgumentException("Provider TTS non supportato: " + selected);
        }
    }
}
