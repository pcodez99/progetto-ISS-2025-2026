package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.GameSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsManagerTest {

    private SettingsManager settingsManager;
    private static final String TEST_FILE = "test_settings.json";

    @TempDir
    Path tempDir; // Crea una directory temporanea automatica per il test

    @BeforeEach
    void setUp() {
        settingsManager = new SettingsManager();
    }

    @Test
    void testSaveAndLoadConsistency() {
        // Percorso completo nel tempDir
        String fullPath = tempDir.resolve(TEST_FILE).toString();

        // 1. Prepariamo i dati
        GameSettings original = new GameSettings();
        original.setMasterVolume(0.85f);
        original.getKeyBindings().put("Jump", "Space");

        // 2. Salvataggio
        settingsManager.save(original, fullPath);

        // 3. Caricamento
        GameSettings loaded = settingsManager.load(fullPath);

        // 4. Verifiche
        assertNotNull(loaded, "Il file caricato non deve essere null");
        assertEquals(0.85f, loaded.getMasterVolume(), 0.001f, "Il volume non corrisponde!");
        assertEquals("Space", loaded.getKeyBindings().get("Jump"), "Il tasto Jump non corrisponde!");
    }

    @Test
    void testLoadNonExistentFileReturnsDefault() {
        // Se il file non esiste, deve tornare un oggetto con valori di default, non crashare
        GameSettings settings = settingsManager.load("file_fantasma.json");
        assertNotNull(settings);
        assertEquals(0.5f, settings.getMasterVolume(), "Dovrebbe avere il volume di default (0.5)");
    }
}
