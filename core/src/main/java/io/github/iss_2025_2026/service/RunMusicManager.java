package io.github.iss_2025_2026.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import io.github.iss_2025_2026.model.GameSettings;

/**
 * Gestisce la musica della run di gioco.
 */
public final class RunMusicManager {
    private static final String RUN_MUSIC_PATH = "final_boss.mp3";
    private static final String SETTINGS_FILE = "settings.json";

    private static final SettingsManager settingsManager = new SettingsManager();
    private static Music runMusic;

    private RunMusicManager() {
    }

    public static void play() {
        if (!ensureLoaded()) {
            return;
        }

        syncVolume();
        runMusic.setLooping(true);
        if (!runMusic.isPlaying()) {
            runMusic.play();
        }
    }

    public static void pause() {
        if (runMusic != null && runMusic.isPlaying()) {
            runMusic.pause();
        }
    }

    public static void stop() {
        if (runMusic != null) {
            runMusic.stop();
        }
    }

    public static void syncVolume() {
        if (runMusic == null) {
            return;
        }

        GameSettings settings = settingsManager.load(SETTINGS_FILE);
        applySettings(settings);
    }

    public static void applySettings(GameSettings settings) {
        if (runMusic == null || settings == null) {
            return;
        }

        runMusic.setVolume(clamp(settings.getMasterVolume()) * clamp(settings.getMusicVolume()));
    }

    private static boolean ensureLoaded() {
        if (runMusic != null) {
            return true;
        }

        if (!Gdx.files.internal(RUN_MUSIC_PATH).exists()) {
            Gdx.app.error("RunMusicManager", "File audio run non trovato: " + RUN_MUSIC_PATH);
            return false;
        }

        runMusic = Gdx.audio.newMusic(Gdx.files.internal(RUN_MUSIC_PATH));
        return true;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
