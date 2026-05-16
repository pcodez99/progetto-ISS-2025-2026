package io.github.iss_2025_2026.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.File;
import java.io.IOException;

/**
 * Gestisce il salvataggio e il caricamento delle partite in formato JSON.
 */
public class SaveManager {
    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false); // Rende il JSON leggibile (pretty print)

    private static final String SAVE_DIRECTORY = "saves/";

    /**
     * Salva lo stato della partita in un file JSON con il nome scelto dall'utente.
     */
    public static void saveGame(GameState state, String fileName) throws IOException {
        // Crea la cartella saves se non esiste
        File dir = new File(SAVE_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Assicura che il file abbia l'estensione .json
        if (!fileName.toLowerCase().endsWith(".json")) {
            fileName += ".json";
        }

        File file = new File(SAVE_DIRECTORY + fileName);
        mapper.writeValue(file, state);
        System.out.println("Partita salvata con successo in: " + file.getAbsolutePath());
    }

    /**
     * Carica uno stato della partita da un file JSON specificato.
     */
    public static GameState loadGame(String fileName) throws IOException {
        if (!fileName.toLowerCase().endsWith(".json")) {
            fileName += ".json";
        }

        File file = new File(SAVE_DIRECTORY + fileName);
        if (!file.exists()) {
            throw new IOException("Errore: Il file di salvataggio '" + fileName + "' non esiste.");
        }

        return mapper.readValue(file, GameState.class);
    }
}
