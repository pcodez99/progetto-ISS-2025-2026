package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD — Fase RED.
 * Test per CharacterSheetModel: logica pura, nessuna dipendenza LibGDX.
 * Tutti questi test devono fallire prima dell'implementazione della classe.
 */
public class CharacterSheetModelTest {

    private Player player;

    private static final SpecialAbility dummyAbility = new SpecialAbility() {
        @Override
        public String getName() { return "TestAbility"; }
        @Override
        public String getDescription() { return "Test"; }
        @Override
        public void perform(Character user, Character target, int userLevel) {}
    };

    @BeforeEach
    void setUp() {
        player = new Player("Viddano", 100, 15, dummyAbility, 10, 2, 1);
    }

    // -------------------------------------------------------------------------
    // Valori iniziali
    // -------------------------------------------------------------------------

    @Test
    void testInitialValues() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals("Viddano", sheet.getPlayerName());
        assertEquals(100, sheet.getHp());
        assertEquals(100, sheet.getMaxHp());
        assertEquals(15, sheet.getBaseDamage());
        assertEquals(1, sheet.getLevel());
        assertEquals(0, sheet.getKarma());
    }

    // -------------------------------------------------------------------------
    // Vita corrente e massima
    // -------------------------------------------------------------------------

    @Test
    void testHpCurrentAndMaxShown() {
        player.takeDamage(30);
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(70, sheet.getHp());
        assertEquals(100, sheet.getMaxHp());
    }

    @Test
    void testHpPercent() {
        player.takeDamage(50);
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(0.5f, sheet.getHpPercent(), 0.01f);
    }

    @Test
    void testHpPercentFullHealth() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(1.0f, sheet.getHpPercent(), 0.001f);
    }

    // -------------------------------------------------------------------------
    // Statistiche
    // -------------------------------------------------------------------------

    @Test
    void testStatisticsDisplayedCorrectly() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(15, sheet.getBaseDamage());
        assertEquals(1, sheet.getLevel());
        assertEquals(0, sheet.getKarma());
    }

    @Test
    void testUpdatedStatsReflectedInSheet() {
        player.increaseBaseDamageByPercent(100); // raddoppia il danno base
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertTrue(sheet.getBaseDamage() > 15, "Il danno base deve essere aumentato");
    }

    @Test
    void testDataRefreshOnModelChange() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);
        player.takeDamage(40);

        // Il model legge direttamente dal player (riferimento live), quindi deve riflettere la modifica
        assertEquals(60, sheet.getHp());
    }

    // -------------------------------------------------------------------------
    // Inventario
    // -------------------------------------------------------------------------

    @Test
    void testInventoryIsEmptyAtStart() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertTrue(sheet.isInventoryEmpty());
        assertEquals(0, sheet.getInventorySize());
    }

    @Test
    void testInventoryCapacity() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertTrue(sheet.getInventoryCapacity() > 0, "La capienza dell'inventario deve essere > 0");
    }

    @Test
    void testNewItemAppearsInSheet() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);
        Collectible potion = new Collectible("level_1_potion", "Cavuliceddi", "Verdura", "HEAL", false, 20);

        player.getBackpack().addItem(potion);

        assertFalse(sheet.isInventoryEmpty(), "L'inventario non deve essere vuoto dopo aver aggiunto un oggetto");
        assertEquals(1, sheet.getInventorySize());
        List<Collectible> inventory = sheet.getInventory();
        assertTrue(inventory.contains(potion), "L'oggetto aggiunto deve essere visibile nell'inventario");
    }

    @Test
    void testInventorySizeAndCapacity() {
        player.getBackpack().addItem(new Collectible("level_1_potion", "Cavuliceddi", "Verdura", "HEAL", false, 20));
        player.getBackpack().addItem(new Collectible("level_1_bomb", "Molotov", "Bomba", "DAMAGE", true, 15));

        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(2, sheet.getInventorySize());
        assertEquals(player.getBackpack().getCapacity(), sheet.getInventoryCapacity());
    }

    @Test
    void testInventoryIsUnmodifiable() {
        CharacterSheetModel sheet = new CharacterSheetModel(player);
        List<Collectible> inventory = sheet.getInventory();

        assertThrows(UnsupportedOperationException.class, () ->
                inventory.add(new Collectible("x", "X", "X", "HEAL", false, 1)),
                "La lista restituita deve essere non modificabile"
        );
    }

    // -------------------------------------------------------------------------
    // Restrizione: non apribile durante il combattimento
    // -------------------------------------------------------------------------

    @Test
    void testCannotOpenDuringCombat() {
        GameModel gameModel = new GameModel();
        gameModel.startSinglePlayerGame("TestGame", player);
        gameModel.getGameState().setPhase(GameState.Phase.COMBAT);

        // Durante il combattimento, il CharacterSheetModel non deve essere presentabile
        CharacterSheetModel sheet = new CharacterSheetModel(player);
        assertFalse(sheet.canOpenDuringPhase(gameModel.getGameState().getPhase()),
                "La schermata personaggio non deve potersi aprire durante il combattimento");
    }

    @Test
    void testCanOpenDuringPlaying() {
        GameModel gameModel = new GameModel();
        gameModel.startSinglePlayerGame("TestGame", player);
        gameModel.getGameState().setPhase(GameState.Phase.PLAYING);

        CharacterSheetModel sheet = new CharacterSheetModel(player);
        assertTrue(sheet.canOpenDuringPhase(gameModel.getGameState().getPhase()),
                "La schermata personaggio deve potersi aprire durante la fase PLAYING");
    }

    // -------------------------------------------------------------------------
    // XP e livello
    // -------------------------------------------------------------------------

    @Test
    void testXpDataReflectedInSheet() {
        player.addXp(50);
        CharacterSheetModel sheet = new CharacterSheetModel(player);

        assertEquals(50, sheet.getXp());
        assertTrue(sheet.getXpToNext() > 0);
    }
}
