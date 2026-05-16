package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.persistence.SaveManager;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    private static final String TEST_FILENAME = "test_salvataggio_temporaneo";
    private static final String SAVE_DIRECTORY = "saves/";

    /**
     * Questo metodo viene eseguito DOPO ogni test.
     * Serve a cancellare il file JSON di prova per mantenere pulito il progetto.
     */
    @AfterEach
    void tearDown() {
        File file = new File(SAVE_DIRECTORY + TEST_FILENAME + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testSaveAndLoadConsistency() throws IOException {
        // 1. ARRANGE: Prepariamo i dati "finti" completi di tutte le novità
        Backpack zainoTest = new Backpack();

        GameState statoOriginale = new GameState(
            "SuperMario",
            "MAMMA",       // Il characterType per la factory
            100,
            25,
            10,
            zainoTest,
            "Castello_Livello_3",
            "FIREBALL"     // L'ID per la AbilityFactory
        );

        // 2. ACT: Salviamo il file e poi lo ricarichiamo subito dopo
        SaveManager.saveGame(statoOriginale, TEST_FILENAME);
        GameState statoCaricato = SaveManager.loadGame(TEST_FILENAME);

        // 3. ASSERT: Controlliamo che Jackson abbia ricostruito tutto alla perfezione
        assertNotNull(statoCaricato, "Il file ricaricato non deve essere nullo");
        assertEquals(statoOriginale.getPlayerName(), statoCaricato.getPlayerName());
        assertEquals(statoOriginale.getCharacterType(), statoCaricato.getCharacterType());
        assertEquals(statoOriginale.getHp(), statoCaricato.getHp());
        assertEquals(statoOriginale.getBaseDamage(), statoCaricato.getBaseDamage());
        assertEquals(statoOriginale.getLevel(), statoCaricato.getLevel());
        assertEquals(statoOriginale.getLastCheckpoint(), statoCaricato.getLastCheckpoint());
        assertEquals(statoOriginale.getSpecialAbilityId(), statoCaricato.getSpecialAbilityId());
        assertNotNull(statoCaricato.getBackpack(), "Lo zaino deve essere stato serializzato correttamente");
    }

    @Test
    void testLoadNonExistentFileThrowsException() {
        // Verifichiamo che il sistema lanci un'eccezione se cerchiamo di caricare un salvataggio inesistente
        Exception exception = assertThrows(IOException.class, () -> {
            SaveManager.loadGame("file_che_non_esiste_assolutamente");
        });

        assertTrue(exception.getMessage().contains("non esiste"), "L'eccezione deve avvisare che il file manca");
    }
}
