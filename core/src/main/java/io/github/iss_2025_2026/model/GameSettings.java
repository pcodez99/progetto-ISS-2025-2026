package io.github.iss_2025_2026.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Viene serializzato/deserializzato in JSON da {@link io.github.iss_2025_2026.service.SettingsManager}.
 */
public class GameSettings {

    /** Volume generale (musica + effetti). Range: [0.0, 1.0]. Default: 0.5 */
    private float masterVolume;

    /** Volume degli effetti sonori. Range: [0.0, 1.0]. Default: 0.5 */
    private float sfxVolume;

    /** Mappatura tasti: nome-azione → tasto (es. "Jump" → "Space"). */
    private Map<String, String> keyBindings;

    /** Costruttore che inizializza i valori di default. */
    public GameSettings() {
        this.masterVolume = 0.5f;
        this.sfxVolume    = 0.5f;
        this.keyBindings  = new HashMap<>();
    }

    // ---------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = masterVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public Map<String, String> getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(Map<String, String> keyBindings) {
        this.keyBindings = keyBindings;
    }
}
