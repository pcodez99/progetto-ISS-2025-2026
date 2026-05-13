package io.github.iss_2025_2026.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.iss_2025_2026.model.GameSettings;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Servizio di persistenza per le impostazioni di gioco.
 * <p>
 * Salva e carica un {@link GameSettings} in formato JSON
 */
public class SettingsManager {

    private final Gson gson;

    public SettingsManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Serializza le impostazioni correnti nel file JSON indicato.
     *
     * @param settings l'oggetto da salvare
     * @param filePath percorso assoluto o relativo del file di destinazione
     * @throws RuntimeException se la scrittura fallisce
     */
    public void save(GameSettings settings, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(settings, writer);
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
        try (FileReader reader = new FileReader(filePath)) {
            GameSettings loaded = gson.fromJson(reader, GameSettings.class);
            return loaded != null ? loaded : new GameSettings();
        } catch (IOException e) {
            // File non trovato o non leggibile → restituiamo i default
            return new GameSettings();
        }
    }
}
