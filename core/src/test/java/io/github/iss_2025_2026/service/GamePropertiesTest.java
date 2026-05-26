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
        float speed = GameProperties.getFloat(GameProperties.KEY_DEFAULT_PLAYER_SPEED, 0f);
        boolean debug = GameProperties.getBoolean(GameProperties.KEY_DRAW_PHYSICS_DEBUG, false);

        assertEquals(400f, maxDistance, 0.001f);
        assertEquals(0.72f, zoom, 0.001f);
        assertEquals(160f, playerSize, 0.001f);
        assertEquals(200f, speed, 0.001f);
        assertTrue(debug);
    }

    @Test
    public void testTypedGettersWithInvalidKeys() {
        float defaultVal = 123.4f;
        float value = GameProperties.getFloat("invalid_key_non_existent", defaultVal);
        assertEquals(defaultVal, value, 0.001f);

        GameProperties.setProperty("bad_number_format_key", "not_a_float");
        float valueBad = GameProperties.getFloat("bad_number_format_key", defaultVal);
        assertEquals(defaultVal, valueBad, 0.001f);
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
}
