package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettingsMenuModelTest {

    private SettingsMenuModel model;

    @BeforeEach
    void setUp() {
        model = new SettingsMenuModel();
    }

    // ---------------------------------------------------------------
    // Volume di default
    // ---------------------------------------------------------------

    @Test
    void testDefaultMasterVolume() {
        assertEquals(0.5f, model.getMasterVolume(), 0.001f,
                "Il volume master di default deve essere 0.5");
    }

    @Test
    void testDefaultSfxVolume() {
        assertEquals(0.5f, model.getSfxVolume(), 0.001f,
                "Il volume SFX di default deve essere 0.5");
    }

    // ---------------------------------------------------------------
    // Impostazione volume valido
    // ---------------------------------------------------------------

    @Test
    void testSetValidMasterVolume() {
        model.setMasterVolume(0.8f);
        assertEquals(0.8f, model.getMasterVolume(), 0.001f,
                "Il volume master deve essere aggiornato a 0.8");
    }

    @Test
    void testSetValidSfxVolume() {
        model.setSfxVolume(0.3f);
        assertEquals(0.3f, model.getSfxVolume(), 0.001f,
                "Il volume SFX deve essere aggiornato a 0.3");
    }

    @Test
    void testSetVolumeToZero() {
        model.setMasterVolume(0.0f);
        assertEquals(0.0f, model.getMasterVolume(), 0.001f,
                "Il volume master può essere 0 (muto)");
    }

    @Test
    void testSetVolumeToOne() {
        model.setMasterVolume(1.0f);
        assertEquals(1.0f, model.getMasterVolume(), 0.001f,
                "Il volume master può essere 1.0 (massimo)");
    }

    // ---------------------------------------------------------------
    // Clamping dei valori fuori range
    // ---------------------------------------------------------------

    @Test
    void testMasterVolumeClampedAboveMax() {
        model.setMasterVolume(1.5f);
        assertEquals(1.0f, model.getMasterVolume(), 0.001f,
                "Il volume master deve essere clampato a 1.0 se supera il massimo");
    }

    @Test
    void testMasterVolumeClampedBelowMin() {
        model.setMasterVolume(-0.3f);
        assertEquals(0.0f, model.getMasterVolume(), 0.001f,
                "Il volume master deve essere clampato a 0.0 se scende sotto il minimo");
    }

    @Test
    void testSfxVolumeClampedAboveMax() {
        model.setSfxVolume(2.0f);
        assertEquals(1.0f, model.getSfxVolume(), 0.001f,
                "Il volume SFX deve essere clampato a 1.0");
    }

    // ---------------------------------------------------------------
    // Mappatura comandi
    // ---------------------------------------------------------------

    @Test
    void testKeyBindingsNotNull() {
        assertNotNull(model.getKeyBindings(),
                "La mappa dei comandi non deve essere null");
    }

    @Test
    void testKeyBindingsNotEmpty() {
        assertFalse(model.getKeyBindings().isEmpty(),
                "La mappa dei comandi deve contenere almeno un'associazione");
    }

    @Test
    void testKeyBindingsContainsExpectedKeys() {
        Map<String, String> bindings = model.getKeyBindings();
        assertTrue(bindings.containsKey("Move Left"),  "Deve esistere il tasto 'Move Left'");
        assertTrue(bindings.containsKey("Move Right"), "Deve esistere il tasto 'Move Right'");
        assertTrue(bindings.containsKey("Jump"),        "Deve esistere il tasto 'Jump'");
        assertTrue(bindings.containsKey("Attack"),      "Deve esistere il tasto 'Attack'");
    }

    // ---------------------------------------------------------------
    // Esportazione → GameSettings
    // ---------------------------------------------------------------

    @Test
    void testExportToGameSettingsPreservesVolumes() {
        model.setMasterVolume(0.7f);
        model.setSfxVolume(0.4f);

        GameSettings gs = model.toGameSettings();

        assertNotNull(gs, "toGameSettings() non deve restituire null");
        assertEquals(0.7f, gs.getMasterVolume(), 0.001f,
                "Il volume master esportato deve corrispondere");
        assertEquals(0.4f, gs.getSfxVolume(), 0.001f,
                "Il volume SFX esportato deve corrispondere");
    }

    @Test
    void testExportToGameSettingsPreservesKeyBindings() {
        GameSettings gs = model.toGameSettings();
        assertNotNull(gs.getKeyBindings(), "I key bindings esportati non devono essere null");
        assertFalse(gs.getKeyBindings().isEmpty(), "I key bindings esportati non devono essere vuoti");
    }

    // ---------------------------------------------------------------
    // Importazione da GameSettings
    // ---------------------------------------------------------------

    @Test
    void testLoadFromGameSettings() {
        GameSettings gs = new GameSettings();
        gs.setMasterVolume(0.9f);
        gs.setSfxVolume(0.2f);
        gs.getKeyBindings().put("Jump", "W");

        model.loadFrom(gs);

        assertEquals(0.9f, model.getMasterVolume(), 0.001f,
                "Dopo loadFrom, il volume master deve essere 0.9");
        assertEquals(0.2f, model.getSfxVolume(), 0.001f,
                "Dopo loadFrom, il volume SFX deve essere 0.2");
        assertEquals("W", model.getKeyBindings().get("Jump"),
                "Dopo loadFrom, il tasto Jump deve essere 'W'");
    }
}
