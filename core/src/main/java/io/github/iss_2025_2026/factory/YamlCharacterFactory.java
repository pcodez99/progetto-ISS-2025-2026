package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

/**
 * Concrete Abstract Factory backed by YAML configuration files.
 */
public class YamlCharacterFactory implements CharacterFactory {
    private final AbilityRegistry abilityRegistry;
    private final PlayerFactory playerFactory;
    private final EnemyFactory enemyFactory;

    public YamlCharacterFactory() {
        CharacterConfigLoader configLoader = new CharacterConfigLoader();
        this.abilityRegistry = AbilityRegistry.loadDefault();
        this.playerFactory = new PlayerFactory(
                configLoader.loadIndexedList("configs/characters.yaml", "characters"),
                abilityRegistry);
        this.enemyFactory = new EnemyFactory(
                configLoader.loadIndexedList("configs/enemies.yaml", "enemies"),
                abilityRegistry);
    }

    @Override
    public Player createPlayer(String id) {
        return playerFactory.create(id);
    }

    @Override
    public Enemy createEnemy(String id) {
        return enemyFactory.create(id);
    }

    @Override
    public SpecialAbility getAbility(String abilityId) {
        return abilityRegistry.get(abilityId);
    }

    @Override
    public Map<String, Map<String, Object>> getCharacterData() {
        return playerFactory.getCharacterData();
    }

    @Override
    public Map<String, Map<String, Object>> getEnemyData() {
        return enemyFactory.getEnemyData();
    }
}
