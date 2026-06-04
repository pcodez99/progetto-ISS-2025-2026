package io.github.iss_2025_2026.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.iss_2025_2026.model.GameState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Gestisce salvataggio, caricamento e lista dei file in formato JSON.
 */
public final class SaveManager {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String SAVE_DIRECTORY = "saves";
    private static final String JSON_EXTENSION = ".json";

    private SaveManager() {
    }

    public static void saveGame(GameState state, String fileName) throws IOException {
        File file = resolveSaveFile(fileName, true);
        MAPPER.writeValue(file, state);
    }

    public static GameState loadGame(String fileName) throws IOException {
        File file = resolveSaveFile(fileName, false);
        if (!file.exists()) {
            throw new IOException("Errore: Il file di salvataggio '" + toSaveFileName(fileName) + "' non esiste.");
        }
        return MAPPER.readValue(file, GameState.class);
    }

    public static List<String> listSaveFiles() {
        File dir = ensureSaveDirectory();
        File[] files = dir.listFiles((directory, name) -> name.toLowerCase().endsWith(JSON_EXTENSION));
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed().thenComparing(File::getName));

        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }

    public static String toSaveFileName(String rawName) {
        String normalized = rawName == null ? "" : rawName.trim();
        if (normalized.isEmpty()) {
            normalized = "salvataggio";
        }

        normalized = normalized.replaceAll("[\\\\/:*?\"<>|]+", "_");
        if (!normalized.toLowerCase().endsWith(JSON_EXTENSION)) {
            normalized += JSON_EXTENSION;
        }
        return normalized;
    }

    public static String toAutoSaveFileName(String rawName) {
        return stripJsonExtension(toSaveFileName(rawName)) + "_autosave" + JSON_EXTENSION;
    }

    public static String stripJsonExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (fileName.toLowerCase().endsWith(JSON_EXTENSION)) {
            return fileName.substring(0, fileName.length() - JSON_EXTENSION.length());
        }
        return fileName;
    }

    private static File resolveSaveFile(String fileName, boolean ensureDirectory) {
        File dir = ensureDirectory ? ensureSaveDirectory() : new File(SAVE_DIRECTORY);
        return new File(dir, toSaveFileName(fileName));
    }

    private static File ensureSaveDirectory() {
        File dir = new File(SAVE_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
