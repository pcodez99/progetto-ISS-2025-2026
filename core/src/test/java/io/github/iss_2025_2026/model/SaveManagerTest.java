package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.persistence.SaveManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveManagerTest {

    private static final String TEST_FILENAME = "test_salvataggio_temporaneo";
    private static final String TEST_FILENAME_WITH_SPACES = "save con spazi";
    private static final String SAVE_DIRECTORY = "saves";

    @AfterEach
    void tearDown() {
        deleteIfExists(TEST_FILENAME);
        deleteIfExists(TEST_FILENAME_WITH_SPACES);
    }

    @Test
    void testSaveAndLoadConsistency() throws IOException {
        Backpack zainoTest = new Backpack();
        PlayerSaveState playerOne = new PlayerSaveState("mamma", "Mamma", 95, 100, 25, 3, 4, zainoTest);
        playerOne.getEvolutionState().recordNpcHelpOutcome("zia_pina", NpcHelpRequestOutcome.ACCEPTED);
        PlayerSaveState playerTwo = new PlayerSaveState("papa", "Papa", 120, 120, 19, 2, -3, new Backpack());
        GameState statoOriginale = new GameState(
                "Run di Test",
                NewGameConfigModel.GameMode.MULTIPLAYER,
                playerOne,
                playerTwo,
                "2026-05-16T10:15:30Z");

        SaveManager.saveGame(statoOriginale, TEST_FILENAME);
        GameState statoCaricato = SaveManager.loadGame(TEST_FILENAME);

        assertNotNull(statoCaricato);
        assertEquals(statoOriginale.getGameName(), statoCaricato.getGameName());
        assertEquals(statoOriginale.getGameMode(), statoCaricato.getGameMode());
        assertNotNull(statoCaricato.getPlayerOne());
        assertNotNull(statoCaricato.getPlayerTwo());
        assertEquals("mamma", statoCaricato.getPlayerOne().getCharacterId());
        assertEquals("Mamma", statoCaricato.getPlayerOne().getName());
        assertEquals(95, statoCaricato.getPlayerOne().getHp());
        assertEquals(120, statoCaricato.getPlayerTwo().getHp());
        assertEquals(NpcHelpRequestOutcome.ACCEPTED,
                statoCaricato.getPlayerOne().getEvolutionState().getNpcHelpOutcome("zia_pina"));
        assertEquals("2026-05-16T10:15:30Z", statoCaricato.getSavedAt());
    }

    @Test
    void testLoadNonExistentFileThrowsException() {
        Exception exception = assertThrows(IOException.class, () -> SaveManager.loadGame("file_che_non_esiste"));
        assertTrue(exception.getMessage().contains("non esiste"));
    }

    @Test
    void testListSaveFilesReturnsNormalizedNames() throws IOException {
        GameState state = new GameState(
                "Save con spazi",
                NewGameConfigModel.GameMode.SINGLE_PLAYER,
                new PlayerSaveState("mamma", "Mamma", 100, 100, 18, 1, 0, new Backpack()),
                null,
                "2026-05-16T10:20:00Z");

        SaveManager.saveGame(state, TEST_FILENAME_WITH_SPACES);
        List<String> saveFiles = SaveManager.listSaveFiles();

        assertFalse(saveFiles.isEmpty());
        assertTrue(saveFiles.contains("save con spazi.json"));
    }

    private void deleteIfExists(String fileName) {
        File file = new File(SAVE_DIRECTORY, SaveManager.toSaveFileName(fileName));
        if (file.exists()) {
            file.delete();
        }
    }
}
