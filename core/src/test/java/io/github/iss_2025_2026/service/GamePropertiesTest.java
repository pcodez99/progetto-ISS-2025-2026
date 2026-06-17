package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.Test;

public class GamePropertiesTest {

    @Test
    public void testDefaultPropertiesExist() {
        // Properties should be initialized on load
        float maxDistance = GameProperties.getFloat(GameProperties.KEY_MAX_PLAYER_DISTANCE, 0f);
        float zoom = GameProperties.getFloat(GameProperties.KEY_CAMERA_ZOOM, 0f);
        float playerSize = GameProperties.getFloat(GameProperties.KEY_PLAYER_SIZE, 0f);
        float enemyEncounterRadius = GameProperties.getFloat(GameProperties.KEY_ENEMY_ENCOUNTER_RADIUS, 0f);
        float npcInteractionRadius = GameProperties.getFloat(GameProperties.KEY_NPC_INTERACTION_RADIUS, 0f);
        float speed = GameProperties.getFloat(GameProperties.KEY_DEFAULT_PLAYER_SPEED, 0f);
        boolean debug = GameProperties.getBoolean(GameProperties.KEY_DRAW_PHYSICS_DEBUG, false);
        boolean devMode = GameProperties.getBoolean(GameProperties.KEY_DEV_MODE, false);
        String aiBaseUrl = GameProperties.getString(GameProperties.KEY_AI_BASE_URL, "");
        int aiTimeout = GameProperties.getInt(GameProperties.KEY_AI_CONNECT_TIMEOUT_MS, 0);

        assertEquals(400f, maxDistance, 0.001f);
        assertEquals(0.72f, zoom, 0.001f);
        assertEquals(160f, playerSize, 0.001f);
        assertEquals(130f, enemyEncounterRadius, 0.001f);
        assertEquals(190f, npcInteractionRadius, 0.001f);
        assertEquals(200f, speed, 0.001f);
        assertTrue(debug);
        assertTrue(devMode);
        assertEquals("http://localhost:11434", aiBaseUrl);
        assertEquals(5000, aiTimeout);
    }

    @Test
    public void testTypedGettersWithInvalidKeys() {
        float defaultVal = 123.4f;
        float value = GameProperties.getFloat("invalid_key_non_existent", defaultVal);
        assertEquals(defaultVal, value, 0.001f);

        GameProperties.setProperty("bad_number_format_key", "not_a_float");
        float valueBad = GameProperties.getFloat("bad_number_format_key", defaultVal);
        assertEquals(defaultVal, valueBad, 0.001f);
        assertEquals(42, GameProperties.getInt("bad_number_format_key", 42));
    }

    @Test
    public void testSetPropertyPersists() {
        // Set a property
        GameProperties.setProperty("test_key_temp", "999.0");

        // Read it back
        float val = GameProperties.getFloat("test_key_temp", 0f);
        assertEquals(999.0f, val, 0.001f);

        // Check file exists
        File file = new File("game.properties");
        assertTrue(file.exists());
    }

    @Test
    public void resolvesRootPropertiesWhenRuntimeStartsFromAssetsDirectory() {
        String originalUserDir = System.getProperty("user.dir");
        File assetsDirectory = new File("../assets").getAbsoluteFile();
        try {
            System.setProperty("user.dir", assetsDirectory.getAbsolutePath());

            File resolved = GameProperties.resolvePropertiesFile();

            assertEquals(new File(assetsDirectory.getParentFile(), "game.properties").getAbsolutePath(),
                    resolved.getAbsolutePath());
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }
}
