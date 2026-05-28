package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnemyFactory implements CharacterTypeFactory<Enemy> {
    private final Map<String, Map<String, Object>> enemyData;
    private final Map<String, Enemy> prototypes;

    EnemyFactory(Map<String, Map<String, Object>> enemyData, AbilityRegistry abilityRegistry) {
        this.enemyData = enemyData;
        this.prototypes = buildPrototypes(enemyData, abilityRegistry);
    }

    @Override
    public Enemy create(String id) {
        Enemy prototype = prototypes.get(id);
        if (prototype == null) {
            throw new IllegalArgumentException("Enemy ID not found in configuration: " + id);
        }
        return prototype.copy();
    }

    Map<String, Map<String, Object>> getEnemyData() {
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
