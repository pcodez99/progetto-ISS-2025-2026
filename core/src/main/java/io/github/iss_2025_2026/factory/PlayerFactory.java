package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

/** Creator dei personaggi giocabili. */
public abstract class PlayerFactory {
    public abstract Player create(String id);

    public abstract SpecialAbility getAbility(String abilityId);

    public abstract Map<String, Map<String, Object>> getCharacterData();
}
