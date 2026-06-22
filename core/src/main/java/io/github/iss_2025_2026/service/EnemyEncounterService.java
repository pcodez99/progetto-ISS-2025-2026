package io.github.iss_2025_2026.service;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.factory.EnemyFactory;
import io.github.iss_2025_2026.map.IsoMapGeometry;
import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Rileva collisioni tra giocatore e oggetti nemici sulla mappa TMX.
 */
public class EnemyEncounterService {
    private final List<EncounterPoint> encounterPoints;
    private final Function<String, Enemy> enemyProvider;
    private final float encounterRadius;

    public EnemyEncounterService(List<MapObject> enemyObjects, EnemyFactory enemyFactory,
            IsoMapGeometry geometry, float encounterRadius) {
        this.enemyProvider = enemyFactory::create;
        this.encounterRadius = encounterRadius;
        this.encounterPoints = new ArrayList<>();
        if (enemyObjects != null && geometry != null) {
            for (MapObject object : enemyObjects) {
                Vector2 world = geometry.objectToWorld(object);
                String enemyType = readStringProperty(object, TmxMapContract.PROPERTY_ENEMY_TYPE, "alieno_base");
                int enemiesNumber = readIntProperty(object, TmxMapContract.PROPERTY_ENEMIES_NUMBER, 1);
                encounterPoints.add(new MapEncounterPoint(object, world.x, world.y, enemyType, enemiesNumber));
            }
        }
    }

    private EnemyEncounterService(List<EncounterPoint> encounterPoints, Function<String, Enemy> enemyProvider,
            float encounterRadius) {
        this.encounterPoints = encounterPoints;
        this.enemyProvider = enemyProvider;
        this.encounterRadius = encounterRadius;
    }

    public static EnemyEncounterService forTesting(float x, float y, String enemyType, int enemiesNumber,
            float encounterRadius) {
        List<EncounterPoint> points = new ArrayList<>();
        points.add(new TestEncounterPoint(x, y, enemyType, enemiesNumber));
        return new EnemyEncounterService(points,
                id -> new Enemy("Alieno Test", id, 45, 8, 15, false), encounterRadius);
    }

    /**
     * Restituisce le informazioni su tutti gli encounter point (attivi e non)
     * per il rendering degli sprite idle sulla mappa isometrica.
     */
    public List<EnemyEncounterInfo> getAllEncounterInfo() {
        List<EnemyEncounterInfo> result = new ArrayList<>();
        for (EncounterPoint point : encounterPoints) {
            result.add(new EnemyEncounterInfo(point.getX(), point.getY(), point.getEnemyType(), point.isActive()));
        }
        return result;
    }

    /**
     * Dati di un singolo punto di spawn nemico, usato dalla view per disegnare lo sprite idle.
     */
    public static final class EnemyEncounterInfo {
        private final float x;
        private final float y;
        private final String enemyType;
        private final boolean active;

        private EnemyEncounterInfo(float x, float y, String enemyType, boolean active) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
            this.active = active;
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public String getEnemyType() { return enemyType; }
        public boolean isActive() { return active; }
    }

    public EnemyEncounter checkEncounter(Player player) {
        if (player == null) {
            return null;
        }

        float playerCenterX = player.getX();
        float playerCenterY = player.getY();

        for (EncounterPoint point : encounterPoints) {
            if (!point.isActive()) {
                continue;
            }
            float dx = playerCenterX - point.getX();
            float dy = playerCenterY - point.getY();
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= encounterRadius * encounterRadius) {
                List<Enemy> enemies = spawnEnemies(point.getEnemyType(), point.getEnemiesNumber());
                return new EnemyEncounter(point.getMapObject(), enemies);
            }
        }
        return null;
    }

    public void removeEncounter(EnemyEncounter encounter) {
        if (encounter == null) {
            return;
        }
        MapObject mapObject = encounter.getMapObject();
        if (mapObject != null) {
            for (EncounterPoint point : encounterPoints) {
                if (point.getMapObject() == mapObject) {
                    point.deactivate();
                    return;
                }
            }
        }
        deactivateFirstActive();
    }

    private void deactivateFirstActive() {
        Iterator<EncounterPoint> iterator = encounterPoints.iterator();
        if (iterator.hasNext()) {
            iterator.next().deactivate();
        }
    }

    private List<Enemy> spawnEnemies(String enemyType, int count) {
        List<Enemy> enemies = new ArrayList<>();
        int safeCount = Math.max(1, count);
        for (int i = 0; i < safeCount; i++) {
            Enemy template = enemyProvider.apply(enemyType);
            if (template != null) {
                enemies.add(template.copy());
            }
        }
        return enemies;
    }

    private static String readStringProperty(MapObject object, String name, String fallback) {
        Object value = object.getProperties().get(name);
        if (value == null) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private static int readIntProperty(MapObject object, String name, int fallback) {
        Object value = object.getProperties().get(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private interface EncounterPoint {
        float getX();

        float getY();

        String getEnemyType();

        int getEnemiesNumber();

        boolean isActive();

        void deactivate();

        MapObject getMapObject();
    }

    private static final class MapEncounterPoint implements EncounterPoint {
        private final MapObject mapObject;
        private final float x;
        private final float y;
        private final String enemyType;
        private final int enemiesNumber;
        private boolean active = true;

        private MapEncounterPoint(MapObject mapObject, float x, float y, String enemyType, int enemiesNumber) {
            this.mapObject = mapObject;
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
            this.enemiesNumber = enemiesNumber;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public String getEnemyType() {
            return enemyType;
        }

        @Override
        public int getEnemiesNumber() {
            return enemiesNumber;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public void deactivate() {
            active = false;
        }

        @Override
        public MapObject getMapObject() {
            return mapObject;
        }
    }

    private static final class TestEncounterPoint implements EncounterPoint {
        private final float x;
        private final float y;
        private final String enemyType;
        private final int enemiesNumber;
        private boolean active = true;

        private TestEncounterPoint(float x, float y, String enemyType, int enemiesNumber) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
            this.enemiesNumber = enemiesNumber;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public String getEnemyType() {
            return enemyType;
        }

        @Override
        public int getEnemiesNumber() {
            return enemiesNumber;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public void deactivate() {
            active = false;
        }

        @Override
        public MapObject getMapObject() {
            return null;
        }
    }
}
