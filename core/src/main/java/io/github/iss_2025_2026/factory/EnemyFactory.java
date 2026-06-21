package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnemyFactory {
    private static final String ENEMIES_CONFIG_PATH = "configs/enemies.yaml";
    private static final String ENEMIES_ROOT_KEY = "enemies";

    private final Map<String, Map<String, Object>> enemyData;
    private final Map<String, Enemy> prototypes;

    public EnemyFactory() {
        this(new CharacterConfigLoader().loadIndexedList(ENEMIES_CONFIG_PATH, ENEMIES_ROOT_KEY),
                AbilityRegistry.loadDefault());
    }

    EnemyFactory(Map<String, Map<String, Object>> enemyData, AbilityRegistry abilityRegistry) {
        this.enemyData = enemyData;
        this.prototypes = buildPrototypes(enemyData, abilityRegistry);
    }

    public Enemy create(String id) {
        Enemy prototype = prototypes.get(id);
        if (prototype == null) {
            throw new IllegalArgumentException("Enemy ID not found in configuration: " + id);
        }
        return prototype.copy();
    }

    public Map<String, Map<String, Object>> getEnemyData() {
        return enemyData;
    }

    private Map<String, Enemy> buildPrototypes(Map<String, Map<String, Object>> configs,
            AbilityRegistry abilityRegistry) {
        Map<String, Enemy> loadedPrototypes = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : configs.entrySet()) {
            loadedPrototypes.put(entry.getKey(), EnemyBuilder.fromConfig(entry.getValue(), abilityRegistry).build());
        }
        return Collections.unmodifiableMap(loadedPrototypes);
    }
}
