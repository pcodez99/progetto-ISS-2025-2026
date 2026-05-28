package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameModelTest {

    @Test
    public void testStartSinglePlayerGameInitializesOnlyPlayerOne() {
        GameModel model = new GameModel();
        Player playerOne = new Player("Nonno", 120, 20, null, 15, 3, 1);

        model.startSinglePlayerGame("Campagna", playerOne);

        assertTrue(model.hasGameStarted());
        assertEquals("Campagna", model.getGameName());
        assertEquals(NewGameConfigModel.GameMode.SINGLE_PLAYER, model.getGameMode());
        assertSame(playerOne, model.getPlayerOne());
        assertNull(model.getPlayerTwo());
        assertFalse(model.isMultiplayerGame());
        assertEquals(1, model.getGameState().getCurrentLevelId());
        assertEquals(GameState.Phase.PLAYING, model.getGameState().getPhase());
    }

    @Test
    public void testStartMultiplayerGameInitializesBothPlayers() {
        GameModel model = new GameModel();
        Player playerOne = new Player("Mamma", 110, 10, null, 12, 4, 1);
        Player playerTwo = new Player("Papa", 150, 17, null, 10, 5, 1);

        model.startMultiplayerGame("Difesa", playerOne, playerTwo);

        assertTrue(model.hasGameStarted());
        assertEquals("Difesa", model.getGameName());
        assertEquals(NewGameConfigModel.GameMode.MULTIPLAYER, model.getGameMode());
        assertSame(playerOne, model.getPlayerOne());
        assertSame(playerTwo, model.getPlayerTwo());
        assertTrue(model.isMultiplayerGame());
        assertEquals(1, model.getGameState().getCurrentLevelId());
        assertEquals(GameState.Phase.PLAYING, model.getGameState().getPhase());
    }

    @Test
    public void testGameStateDefaultsToMenuBeforeRunStarts() {
        GameModel model = new GameModel();

        assertEquals(1, model.getGameState().getCurrentLevelId());
        assertEquals(GameState.Phase.MENU, model.getGameState().getPhase());
        assertTrue(model.getGameState().getCompletedLevelIds().isEmpty());
    }
}
