package io.github.iss_2025_2026.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model per la schermata delle impostazioni.
 * <p>
 * Gestisce:
 * <ul>
 *   <li>Volume master (musica + effetti) — range [0.0, 1.0]</li>
 *   <li>Volume SFX — range [0.0, 1.0]</li>
 *   <li>Mappatura comandi di gioco (read-only dall'esterno)</li>
 * </ul>
 * Può essere esportato/importato tramite {@link GameSettings}.
 */
public class SettingsMenuModel {

    private static final float MIN_VOLUME = 0.0f;
    private static final float MAX_VOLUME = 1.0f;
    private static final float DEFAULT_VOLUME = 0.5f;

    private float masterVolume;
    private float sfxVolume;
    private final Map<String, String> keyBindings;

    /** Costruisce il model con i valori di default e i comandi standard. */
    public SettingsMenuModel() {
        this.masterVolume = DEFAULT_VOLUME;
        this.sfxVolume    = DEFAULT_VOLUME;
        this.keyBindings  = buildDefaultKeyBindings();
    }

    // ---------------------------------------------------------------
    // Volume
    // ---------------------------------------------------------------

    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Imposta il volume master, clampandolo nell'intervallo [0.0, 1.0].
     *
     * @param volume il valore desiderato (può essere fuori range — verrà corretto)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = clamp(volume);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Imposta il volume SFX, clampandolo nell'intervallo [0.0, 1.0].
     *
     * @param volume il valore desiderato (può essere fuori range — verrà corretto)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = clamp(volume);
    }

    // ---------------------------------------------------------------
    // Mappatura comandi
    // ---------------------------------------------------------------

    /**
     * Restituisce una vista non modificabile dei comandi di gioco.
     *
     * @return mappa {@code nome-azione → tasto}, read-only
     */
    public Map<String, String> getKeyBindings() {
        return Collections.unmodifiableMap(keyBindings);
    }

    // ---------------------------------------------------------------
    // Interoperabilità con GameSettings
    // ---------------------------------------------------------------

    /**
     * Esporta lo stato corrente del model come {@link GameSettings} serializzabile.
     *
     * @return un nuovo {@code GameSettings} con i valori correnti
     */
    public GameSettings toGameSettings() {
        GameSettings gs = new GameSettings();
        gs.setMasterVolume(masterVolume);
        gs.setSfxVolume(sfxVolume);
        gs.setKeyBindings(new HashMap<>(keyBindings));
        return gs;
    }

    /**
     * Importa i dati da un {@link GameSettings} (es. letto da file).
     * I volumi vengono clampati per sicurezza.
     *
     * @param gs l'oggetto impostazioni da caricare
     */
    public void loadFrom(GameSettings gs) {
        if (gs == null) return;
        setMasterVolume(gs.getMasterVolume());
        setSfxVolume(gs.getSfxVolume());
        if (gs.getKeyBindings() != null) {
            keyBindings.clear();
            keyBindings.putAll(gs.getKeyBindings());
        }
    }

    // ---------------------------------------------------------------
    // Utility privata
    // ---------------------------------------------------------------

    private static float clamp(float value) {
        return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, value));
    }

    private static Map<String, String> buildDefaultKeyBindings() {
        Map<String, String> map = new HashMap<>();
        map.put("Move Left",  "A");
        map.put("Move Right", "D");
        map.put("Jump",       "Space");
        map.put("Attack",     "Z");
        map.put("Pause",      "Escape");
        return map;
    }
}
