package io.github.iss_2025_2026.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Gestisce le configurazioni globali del gioco tramite un file di proprietà (.properties)
 * in stile Minecraft (game.properties).
 */
public class GameProperties {
    private static final String FILE_PATH = "game.properties";
    private static final Properties properties = new Properties();

    // Chiavi delle impostazioni
    public static final String KEY_MAX_PLAYER_DISTANCE = "max_player_distance";
    public static final String KEY_CAMERA_ZOOM = "camera_zoom";
    public static final String KEY_PLAYER_SIZE = "player_size";
    public static final String KEY_DEFAULT_PLAYER_SPEED = "default_player_speed";
    public static final String KEY_DRAW_PHYSICS_DEBUG = "draw_physics_debug";
    public static final String KEY_MUSIC_VOLUME = "music_volume";
    public static final String KEY_SFX_VOLUME = "sfx_volume";

    static {
        // Impostiamo i valori di default
        properties.setProperty(KEY_MAX_PLAYER_DISTANCE, "400.0");
        properties.setProperty(KEY_CAMERA_ZOOM, "0.72");
        properties.setProperty(KEY_PLAYER_SIZE, "160.0");
        properties.setProperty(KEY_DEFAULT_PLAYER_SPEED, "200.0");
        properties.setProperty(KEY_DRAW_PHYSICS_DEBUG, "true");
        properties.setProperty(KEY_MUSIC_VOLUME, "0.5");
        properties.setProperty(KEY_SFX_VOLUME, "0.5");

        load();
    }

    public static void load() {
        File file = new File(FILE_PATH);
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
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            properties.store(fos, "Impostazioni di gioco configurabili (in stile Minecraft)");
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio di " + FILE_PATH + ": " + e.getMessage());
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

    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Setters
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        save();
    }
}
