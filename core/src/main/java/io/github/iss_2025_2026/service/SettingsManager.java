package io.github.iss_2025_2026.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.iss_2025_2026.model.GameSettings;

import java.io.IOException;

/**
 * Servizio di persistenza per le impostazioni di gioco.
 * <p>
 * Salva e carica un {@link GameSettings} in formato JSON
 */
public class SettingsManager {

    private final ObjectMapper mapper;

    public SettingsManager() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializza le impostazioni correnti nel file JSON indicato.
     *
     * @param settings l'oggetto da salvare
     * @param filePath percorso assoluto o relativo del file di destinazione
     * @throws RuntimeException se la scrittura fallisce
     */
    public void save(GameSettings settings, String filePath) {
        try {
            mapper.writeValue(new java.io.File(filePath), settings);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile salvare le impostazioni in: " + filePath, e);
        }
    }

    /**
     * Deserializza le impostazioni dal file JSON indicato.
     * <p>
     * Se il file non esiste o la lettura fallisce, restituisce un
     * {@link GameSettings} con i valori di default (non lancia eccezioni).
     *
     * @param filePath percorso del file da leggere
     * @return le impostazioni caricate, oppure i valori di default
     */
    public GameSettings load(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                return new GameSettings();
            }
            GameSettings loaded = mapper.readValue(file, GameSettings.class);
            return loaded != null ? loaded : new GameSettings();
        } catch (IOException e) {
            // Errore durante la lettura → restituiamo i default
            return new GameSettings();
        }
    }
}
