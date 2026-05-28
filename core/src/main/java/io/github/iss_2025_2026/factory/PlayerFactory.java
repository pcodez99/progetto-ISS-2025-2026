package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Player;
import java.util.Map;

public class PlayerFactory implements CharacterTypeFactory<Player> {
    private final Map<String, Map<String, Object>> characterData;
    private final AbilityRegistry abilityRegistry;

    PlayerFactory(Map<String, Map<String, Object>> characterData, AbilityRegistry abilityRegistry) {
        this.characterData = characterData;
        this.abilityRegistry = abilityRegistry;
    }

    @Override
    public Player create(String id) {
        Map<String, Object> data = characterData.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Character ID not found in configuration: " + id);
        }
        return PlayerBuilder.fromConfig(data, abilityRegistry).build();
    }

    Map<String, Map<String, Object>> getCharacterData() {
        return characterData;
    }
}
