package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

/**
 * Abstract Factory for the family of runtime characters.
 */
public interface CharacterFactory {
    Player createPlayer(String id);

    Enemy createEnemy(String id);

    SpecialAbility getAbility(String abilityId);

    Map<String, Map<String, Object>> getCharacterData();

    Map<String, Map<String, Object>> getEnemyData();
}
