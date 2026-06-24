package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.DialogueProfile;
import io.github.iss_2025_2026.model.EvolutionPath;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DialogueProfileFactory {
    private static final Logger LOGGER = Logger.getLogger(DialogueProfileFactory.class.getName());
    private static final String DEFAULT_PATH = "configs/dialogue_profiles.yaml";

    private final Map<EvolutionPath, DialogueProfile> profiles;
    private final ObjectMapper mapper;

    public DialogueProfileFactory() {
        this(DEFAULT_PATH);
    }

    DialogueProfileFactory(String path) {
        this.profiles = new EnumMap<>(EvolutionPath.class);
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadProfiles(path);
        ensureFallbacks();
    }

    private void loadProfiles(String path) {
        try (InputStream inputStream = DialogueProfileFactory.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOGGER.severe("CRITICAL ERROR: File " + path + " non trovato nel classpath.");
                return;
            }

            DialogueProfileCatalog catalog = mapper.readValue(inputStream, DialogueProfileCatalog.class);
            for (Map.Entry<String, DialogueProfile> entry : catalog.getProfiles().entrySet()) {
                EvolutionPath pathKey = EvolutionPath.valueOf(entry.getKey().trim().toUpperCase());
                DialogueProfile profile = entry.getValue();
                profile.setPath(pathKey);
                profiles.put(pathKey, profile);
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il parsing del file YAML: " + path, exception);
        }
    }

    private void ensureFallbacks() {
        if (!profiles.containsKey(EvolutionPath.BALANCED)) {
            DialogueProfile balanced = new DialogueProfile();
            balanced.setPath(EvolutionPath.BALANCED);
            profiles.put(EvolutionPath.BALANCED, balanced);
        }
        if (!profiles.containsKey(EvolutionPath.ALTRUISTIC)) {
            profiles.put(EvolutionPath.ALTRUISTIC, profiles.get(EvolutionPath.BALANCED));
        }
        if (!profiles.containsKey(EvolutionPath.EGOISTIC)) {
            profiles.put(EvolutionPath.EGOISTIC, profiles.get(EvolutionPath.BALANCED));
        }
    }

    public DialogueProfile getProfile(EvolutionPath path) {
        EvolutionPath resolvedPath = path != null ? path : EvolutionPath.BALANCED;
        return profiles.get(resolvedPath);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class DialogueProfileCatalog {
        private Map<String, DialogueProfile> profiles = Collections.emptyMap();

        private Map<String, DialogueProfile> getProfiles() {
            return profiles != null ? profiles : Collections.<String, DialogueProfile>emptyMap();
        }

        @SuppressWarnings("unused")
        public void setProfiles(Map<String, DialogueProfile> profiles) {
            this.profiles = profiles;
        }
    }
}
