package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Concrete Creator che costruisce prototipi Enemy configurati tramite YAML. */
public final class YamlEnemyFactory extends EnemyFactory {
    private static final String ENEMIES_CONFIG_PATH = "configs/enemies.yaml";
    private static final String ENEMIES_ROOT_KEY = "enemies";

    private final Map<String, Map<String, Object>> enemyData;
    private final Map<String, Enemy> prototypes;
    private final CharacterBuildDirector buildDirector;

    public YamlEnemyFactory() {
        this(new CharacterConfigLoader().loadIndexedList(ENEMIES_CONFIG_PATH, ENEMIES_ROOT_KEY),
                AbilityRegistry.loadDefault());
    }

    YamlEnemyFactory(Map<String, Map<String, Object>> enemyData, AbilityRegistry abilityRegistry) {
        this.enemyData = enemyData;
        this.buildDirector = new CharacterBuildDirector();
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

    @Override
    public Map<String, Map<String, Object>> getEnemyData() {
        return enemyData;
    }

    private Map<String, Enemy> buildPrototypes(Map<String, Map<String, Object>> configs,
            AbilityRegistry abilityRegistry) {
        Map<String, Enemy> loadedPrototypes = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : configs.entrySet()) {
            loadedPrototypes.put(entry.getKey(),
                    buildDirector.constructEnemy(new EnemyBuilder(), entry.getValue(), abilityRegistry));
        }
        return Collections.unmodifiableMap(loadedPrototypes);
    }
}
