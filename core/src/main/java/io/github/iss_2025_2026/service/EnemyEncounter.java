package io.github.iss_2025_2026.service;

import com.badlogic.gdx.maps.MapObject;
import io.github.iss_2025_2026.model.Enemy;
import java.util.Collections;
import java.util.List;

/**
 * DTO con i dati di un incontro: oggetto TMX triggerato e nemici da affrontare.
 */
public final class EnemyEncounter {
    private final MapObject mapObject;
    private final List<Enemy> enemies;

    public EnemyEncounter(MapObject mapObject, List<Enemy> enemies) {
        this.mapObject = mapObject;
        this.enemies = enemies;
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
}
