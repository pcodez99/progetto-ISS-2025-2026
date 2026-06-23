package io.github.iss_2025_2026.service.tts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MistralApiTtsClientCreator extends TtsClientCreator {
    public static final String API_KEY_ENV = "MISTRAL_API_KEY";

    @Override
    protected void validate(TtsConfig config) {
        super.validate(config);
        String apiKey = readApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Variabile d'ambiente " + API_KEY_ENV
                    + " assente. La chiave Mistral non viene letta da file o game.properties.");
        }
    }

    @Override
    protected TtsClient buildClient(TtsConfig config) {
        return new MistralApiTtsClient(config, readApiKey().trim());
    }

    protected String readApiKey() {
        String key = System.getenv(API_KEY_ENV);
        if (key == null || key.trim().isEmpty()) {
            key = readFromDotEnv();
        }
        return key;
    }

    private String readFromDotEnv() {
        File cwd = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        File envFile = new File(cwd, ".env");
        if (!envFile.exists() && cwd.getParentFile() != null) {
            envFile = new File(cwd.getParentFile(), ".env");
        }
        if (!envFile.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith(API_KEY_ENV + "=")) {
                    String value = line.substring(API_KEY_ENV.length() + 1);
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value.isEmpty() ? null : value;
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
}
