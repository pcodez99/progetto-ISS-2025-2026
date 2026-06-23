package io.github.iss_2025_2026.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Gestisce le configurazioni globali del gioco tramite un file di proprietà (.properties)
 * in stile Minecraft (game.properties).
 */
public class GameProperties {
    private static final Logger LOGGER = Logger.getLogger(GameProperties.class.getName());
    private static final String FILE_PATH = "game.properties";
    private static final String PROPERTY_FILE_OVERRIDE = "viddani.game.properties";
    private static final Properties properties = new Properties();
    private static File activeFile;

    // Chiavi delle impostazioni
    public static final String KEY_MAX_PLAYER_DISTANCE = "max_player_distance";
    public static final String KEY_CAMERA_ZOOM = "camera_zoom";
    public static final String KEY_CAMERA_ZOOM_MIN = "camera_zoom_min";
    public static final String KEY_PLAYER_SIZE = "player_size";
    public static final String KEY_ENEMY_ENCOUNTER_RADIUS = "enemy_encounter_radius";
    public static final String KEY_NPC_INTERACTION_RADIUS = "npc_interaction_radius";
    public static final String KEY_DEFAULT_PLAYER_SPEED = "default_player_speed";
    public static final String KEY_DRAW_PHYSICS_DEBUG = "draw_physics_debug";
    public static final String KEY_MUSIC_VOLUME = "music_volume";
    public static final String KEY_SFX_VOLUME = "sfx_volume";
    public static final String KEY_DEV_MODE = "dev_mode";
    public static final String KEY_AI_ENABLED = "ai_enabled";
    public static final String KEY_AI_BASE_URL = "ai_base_url";
    public static final String KEY_AI_MODEL = "ai_model";
    public static final String KEY_AI_CHAT_ENDPOINT = "ai_chat_endpoint";
    public static final String KEY_AI_STREAM = "ai_stream";
    public static final String KEY_AI_CONNECT_TIMEOUT_MS = "ai_connect_timeout_ms";
    public static final String KEY_AI_READ_TIMEOUT_MS = "ai_read_timeout_ms";
    public static final String KEY_AI_DEFAULT_TEMPERATURE = "ai_default_temperature";
    public static final String KEY_AI_MAX_PROMPT_CHARS = "ai_max_prompt_chars";
    public static final String KEY_AI_FALLBACK_TO_STATIC_DIALOGUE = "ai_fallback_to_static_dialogue";

    static {
        // Impostiamo i valori di default
        properties.setProperty(KEY_MAX_PLAYER_DISTANCE, "400.0");
        properties.setProperty(KEY_CAMERA_ZOOM, "0.72");
        properties.setProperty(KEY_CAMERA_ZOOM_MIN, "2.0");
        properties.setProperty(KEY_PLAYER_SIZE, "160.0");
        properties.setProperty(KEY_ENEMY_ENCOUNTER_RADIUS, "130.0");
        properties.setProperty(KEY_NPC_INTERACTION_RADIUS, "190.0");
        properties.setProperty(KEY_DEFAULT_PLAYER_SPEED, "200.0");
        properties.setProperty(KEY_DRAW_PHYSICS_DEBUG, "true");
        properties.setProperty(KEY_MUSIC_VOLUME, "0.5");
        properties.setProperty(KEY_SFX_VOLUME, "0.5");
        properties.setProperty(KEY_DEV_MODE, "true");
        properties.setProperty(KEY_AI_ENABLED, "true");
        properties.setProperty(KEY_AI_BASE_URL, "http://localhost:11434");
        properties.setProperty(KEY_AI_MODEL, "llama3");
        properties.setProperty(KEY_AI_CHAT_ENDPOINT, "/api/chat");
        properties.setProperty(KEY_AI_STREAM, "false");
        properties.setProperty(KEY_AI_CONNECT_TIMEOUT_MS, "5000");
        properties.setProperty(KEY_AI_READ_TIMEOUT_MS, "30000");
        properties.setProperty(KEY_AI_DEFAULT_TEMPERATURE, "0.75");
        properties.setProperty(KEY_AI_MAX_PROMPT_CHARS, "4000");
        properties.setProperty(KEY_AI_FALLBACK_TO_STATIC_DIALOGUE, "true");

        load();
    }

    public static void load() {
        File file = resolvePropertiesFile();
        activeFile = file;
        LOGGER.info("[GameProperties] user.dir=" + System.getProperty("user.dir")
                + ", file=" + file.getAbsolutePath());
        if (!file.exists()) {
            save(); // Crea il file con i valori di default se non esiste
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento di " + FILE_PATH + ": " + e.getMessage());
        }
    }

    public static void save() {
        File file = activeFile != null ? activeFile : resolvePropertiesFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Impostazioni di gioco configurabili (in stile Minecraft)");
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio di " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    // Getters tipizzati
    public static float getFloat(String key, float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Setters
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    public static String getActiveFilePath() {
        File file = activeFile != null ? activeFile : resolvePropertiesFile();
        return file.getAbsolutePath();
    }

    static File resolvePropertiesFile() {
        String override = System.getProperty(PROPERTY_FILE_OVERRIDE);
        if (override != null && !override.trim().isEmpty()) {
            return new File(override.trim()).getAbsoluteFile();
        }

        File userDir = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        File currentDirectoryFile = new File(userDir, FILE_PATH);

        if ("assets".equals(userDir.getName())) {
            File projectRootFile = new File(userDir.getParentFile(), FILE_PATH);
            if (projectRootFile.exists()) {
                return projectRootFile.getAbsoluteFile();
            }
        }

        return currentDirectoryFile.getAbsoluteFile();
    }
}
