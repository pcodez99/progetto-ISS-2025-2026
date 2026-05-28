package io.github.iss_2025_2026.map;

/**
 * Names and properties that form the contract between Tiled and the runtime.
 */
public final class TmxMapContract {
    public static final int EXPECTED_LEVEL_COUNT = 3;
    public static final int DEFAULT_LEVEL_ID = 1;
    public static final String LEVELS_MANIFEST_PATH = "map/levels.yaml";
    public static final String LEVEL_ONE_MAP_PATH = "map/levels/1/level.tmx";

    public static final String LAYER_SPAWN = "Spawn";
    public static final String LAYER_OBSTACLES = "Ostacoli";
    public static final String LAYER_RIGID_BODIES = "RigidBodies";

    public static final String PROPERTY_COLLISION = "collision";
    public static final String SPAWN_OBJECT_NAME = "Spawn";

    private TmxMapContract() {
    }

    public static boolean isPlayerSpawnName(String name) {
        return SPAWN_OBJECT_NAME.equals(name)
                || "SpawnPlayer".equals(name)
                || "PlayerSpawn".equals(name);
    }
}
