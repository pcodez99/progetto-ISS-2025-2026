package io.github.iss_2025_2026.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import io.github.iss_2025_2026.model.GameSettings;

/**
 * Gestisce la musica condivisa tra menu principale e menu di pausa.
 */
public final class MenuMusicManager {
    private static final String MENU_MUSIC_PATH = "menu-sound-effects.mp3";
    private static final String SETTINGS_FILE = "settings.json";

    private static final SettingsManager settingsManager = new SettingsManager();
    private static Music menuMusic;

    private MenuMusicManager() {
    }

    public static void play() {
        if (!ensureLoaded()) {
            return;
        }

        syncVolume();
        menuMusic.setLooping(true);
        if (!menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public static void pause() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.pause();
        }
    }

    public static void stop() {
        if (menuMusic != null) {
            menuMusic.stop();
        }
    }

    public static void syncVolume() {
        if (menuMusic == null) {
            return;
        }

        GameSettings settings = settingsManager.load(SETTINGS_FILE);
        applySettings(settings);
    }

    public static void applySettings(GameSettings settings) {
        if (menuMusic == null || settings == null) {
            return;
        }

        menuMusic.setVolume(clamp(settings.getMasterVolume()) * clamp(settings.getMusicVolume()));
    }

    private static boolean ensureLoaded() {
        if (menuMusic != null) {
            return true;
        }

        if (!Gdx.files.internal(MENU_MUSIC_PATH).exists()) {
            Gdx.app.error("MenuMusicManager", "File audio menu non trovato: " + MENU_MUSIC_PATH);
            return false;
        }

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal(MENU_MUSIC_PATH));
        return true;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
