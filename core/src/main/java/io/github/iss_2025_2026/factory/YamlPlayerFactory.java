package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

/** Concrete Creator che costruisce Player configurati tramite YAML. */
public final class YamlPlayerFactory extends PlayerFactory {
    private static final String CHARACTERS_CONFIG_PATH = "configs/characters.yaml";
    private static final String CHARACTERS_ROOT_KEY = "characters";

    private final Map<String, Map<String, Object>> characterData;
    private final AbilityRegistry abilityRegistry;
    private final CharacterBuildDirector buildDirector;

    public YamlPlayerFactory() {
        this(new CharacterConfigLoader().loadIndexedList(CHARACTERS_CONFIG_PATH, CHARACTERS_ROOT_KEY),
                AbilityRegistry.loadDefault());
    }

    YamlPlayerFactory(Map<String, Map<String, Object>> characterData, AbilityRegistry abilityRegistry) {
        this.characterData = characterData;
        this.abilityRegistry = abilityRegistry;
        this.buildDirector = new CharacterBuildDirector();
    }

    @Override
    public Player create(String id) {
        Map<String, Object> data = characterData.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Character ID not found in configuration: " + id);
        }
        return buildDirector.constructPlayer(new PlayerBuilder(), data, abilityRegistry);
    }

    @Override
    public SpecialAbility getAbility(String abilityId) {
        return abilityRegistry.get(abilityId);
    }

    @Override
    public Map<String, Map<String, Object>> getCharacterData() {
        return characterData;
    }
}
