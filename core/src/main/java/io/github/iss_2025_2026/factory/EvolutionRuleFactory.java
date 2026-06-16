package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.CharacterEvolutionRules;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvolutionRuleFactory {
    private static final Logger LOGGER = Logger.getLogger(EvolutionRuleFactory.class.getName());
    private static final String DEFAULT_PATH = "configs/evolution_rules.yaml";

    private final Map<String, CharacterEvolutionRules> rulesByCharacterId;
    private final ObjectMapper mapper;

    public EvolutionRuleFactory() {
        this(DEFAULT_PATH);
    }

    EvolutionRuleFactory(String path) {
        this.rulesByCharacterId = new HashMap<>();
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadRules(path);
    }

    private void loadRules(String path) {
        try (InputStream inputStream = EvolutionRuleFactory.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOGGER.severe("CRITICAL ERROR: File " + path + " non trovato nel classpath.");
                return;
            }

            EvolutionRuleCatalog catalog = mapper.readValue(inputStream, EvolutionRuleCatalog.class);
            rulesByCharacterId.putAll(catalog.getCharacters());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il parsing del file YAML: " + path, exception);
        }
    }

    public Optional<CharacterEvolutionRules> findRulesForCharacter(String characterId) {
        if (characterId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rulesByCharacterId.get(characterId));
    }

    public CharacterEvolutionRules getRulesForCharacter(String characterId) {
        return findRulesForCharacter(characterId).orElse(null);
    }

    public Map<String, CharacterEvolutionRules> getAllRules() {
        return Collections.unmodifiableMap(rulesByCharacterId);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class EvolutionRuleCatalog {
        private Map<String, CharacterEvolutionRules> characters = new HashMap<>();

        private Map<String, CharacterEvolutionRules> getCharacters() {
            return characters != null ? characters : Collections.<String, CharacterEvolutionRules>emptyMap();
        }

        @SuppressWarnings("unused")
        public void setCharacters(Map<String, CharacterEvolutionRules> characters) {
            this.characters = characters;
        }
    }
}
