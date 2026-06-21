package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

public class PlayerFactory {
    private static final String CHARACTERS_CONFIG_PATH = "configs/characters.yaml";
    private static final String CHARACTERS_ROOT_KEY = "characters";

    private final Map<String, Map<String, Object>> characterData;
    private final AbilityRegistry abilityRegistry;

    public PlayerFactory() {
        this(new CharacterConfigLoader().loadIndexedList(CHARACTERS_CONFIG_PATH, CHARACTERS_ROOT_KEY),
                AbilityRegistry.loadDefault());
    }

    PlayerFactory(Map<String, Map<String, Object>> characterData, AbilityRegistry abilityRegistry) {
        this.characterData = characterData;
        this.abilityRegistry = abilityRegistry;
    }

    public Player create(String id) {
        Map<String, Object> data = characterData.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Character ID not found in configuration: " + id);
        }
        return PlayerBuilder.fromConfig(data, abilityRegistry).build();
    }

    public SpecialAbility getAbility(String abilityId) {
        return abilityRegistry.get(abilityId);
    }

    public Map<String, Map<String, Object>> getCharacterData() {
        return characterData;
    }
}
