package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import java.util.Map;

/** Creator dei nemici. */
public abstract class EnemyFactory {
    public abstract Enemy create(String id);

    public abstract Map<String, Map<String, Object>> getEnemyData();
}
