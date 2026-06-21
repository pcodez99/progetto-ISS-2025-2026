package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.config.CollectibleCatalog;
import io.github.iss_2025_2026.config.CollectibleDefinition;
import io.github.iss_2025_2026.config.CollectibleVisualConfig;
import io.github.iss_2025_2026.config.CollectibleVisualType;
import io.github.iss_2025_2026.model.collectibles.CollectibleEffectFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Carica e valida il catalogo YAML dei collectible prima dell'avvio del livello. */
public final class CollectibleConfigLoader {
    private static final String DEFAULT_PATH = "configs/collectibles.yaml";

    private CollectibleConfigLoader() {
    }

    public static CollectibleCatalog loadDefault() {
        return load(DEFAULT_PATH);
    }

    static CollectibleCatalog load(String path) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        try (InputStream input = CollectibleConfigLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Configurazione collectible non trovata: " + path);
            }
            List<CollectibleDefinition> definitions = mapper.readValue(
                    input, new TypeReference<List<CollectibleDefinition>>() { });
            validate(definitions, path);
            return new CollectibleCatalog(definitions);
        } catch (IOException exception) {
            throw new IllegalStateException("Errore durante il parsing di " + path, exception);
        }
    }

    private static void validate(List<CollectibleDefinition> definitions, String path) {
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        if (definitions == null || definitions.isEmpty()) {
            throw new IllegalStateException("Nessun collectible configurato in " + path);
        }

        for (int index = 0; index < definitions.size(); index++) {
            CollectibleDefinition definition = definitions.get(index);
            String location = path + "[" + index + "]";
            if (definition == null) {
                errors.add(location + ": voce nulla");
                continue;
            }

            String id = trimToNull(definition.getId());
            if (id == null) {
                errors.add(location + ": id mancante");
            } else if (!ids.add(id)) {
                errors.add(location + ": id duplicato '" + id + "'");
            }
            if (definition.getEffectValue() < 0) {
                errors.add(location + ": effectValue non puo essere negativo");
            }
            try {
                CollectibleEffectFactory.getStrategy(definition.getEffectType());
            } catch (IllegalArgumentException exception) {
                errors.add(location + ": " + exception.getMessage());
            }
            validateVisual(definition.getVisual(), location, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configurazione collectible non valida:\n - "
                    + String.join("\n - ", errors));
        }
    }

    private static void validateVisual(
            CollectibleVisualConfig visual, String location, List<String> errors) {
        if (visual == null) {
            errors.add(location + ": sezione visual mancante");
            return;
        }
        if (visual.getType() == null) {
            errors.add(location + ": visual.type mancante");
        }
        if (trimToNull(visual.getAsset()) == null) {
            errors.add(location + ": visual.asset mancante");
        }
        if (visual.getType() == CollectibleVisualType.ANIMATED) {
            if (visual.getFrameWidth() <= 0 || visual.getFrameHeight() <= 0) {
                errors.add(location + ": dimensioni dei frame non valide");
            }
            if (visual.getFrameDuration() <= 0f) {
                errors.add(location + ": frameDuration deve essere maggiore di zero");
            }
            String playMode = trimToNull(visual.getPlayMode());
            if (playMode == null || !isSupportedPlayMode(playMode)) {
                errors.add(location + ": playMode non supportato '" + visual.getPlayMode() + "'");
            }
        }
    }

    private static boolean isSupportedPlayMode(String playMode) {
        String normalized = playMode.toUpperCase(Locale.ROOT);
        return "NORMAL".equals(normalized)
                || "REVERSED".equals(normalized)
                || "LOOP".equals(normalized)
                || "LOOP_REVERSED".equals(normalized)
                || "LOOP_PINGPONG".equals(normalized)
                || "LOOP_RANDOM".equals(normalized);
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
