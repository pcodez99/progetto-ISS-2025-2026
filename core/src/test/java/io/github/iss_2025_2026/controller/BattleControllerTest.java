package io.github.iss_2025_2026.controller;

import static org.junit.jupiter.api.Assertions.*;

import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Characters;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.model.combat.BattlePhase;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test TDD per BattleController.
 * Questi test sono scritti PRIMA dell'implementazione (fase RED).
 */
public class BattleControllerTest {

    private Player playerOne;
    private Player playerTwo;
    private Enemy enemyOne;
    private BattleModel model;
    private BattleController controller;

    @BeforeEach
    public void setUp() {
        SpecialAbility dummyAbility = new SpecialAbility() {
            @Override public String getName() { return "Test"; }
            @Override public String getDescription() { return "Abilita test"; }
            @Override public void perform(Characters user, Characters target, int level) {
                target.takeDamage(20);
            }
        };

        playerOne = new Player("Viddano", 100, 10, dummyAbility);
        playerTwo = new Player("Mamma", 80, 8, null);
        enemyOne  = new Enemy("Alieno", "alieno_base", 45, 8, 15, false);

        model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        controller = new BattleController(model);
    }

    // -------------------------------------------------------------------------
    // Azione: Attacco
    // -------------------------------------------------------------------------

    @Test
    public void testOnAttackSelectedTransitionToTargetSelectionState() {
        // Quando il giocatore seleziona "Attacco", il controller deve
        // transitare allo stato di selezione del bersaglio
        controller.onAttackSelected();

        assertEquals(BattleController.MenuState.TARGET_SELECTION, controller.getMenuState());
    }

    @Test
    public void testOnTargetSelectedExecutesAttackAndTransitionsToEnemyTurn() {
        controller.onAttackSelected();
        assertEquals(BattleController.MenuState.TARGET_SELECTION, controller.getMenuState());

        int hpBefore = enemyOne.getHp();
        controller.onTargetSelected(enemyOne);

        assertTrue(enemyOne.getHp() < hpBefore, "L'attacco deve ridurre gli HP del nemico");
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());
        assertEquals(BattleController.MenuState.MAIN_MENU, controller.getMenuState());
    }

    // -------------------------------------------------------------------------
    // Azione: Abilità Speciale
    // -------------------------------------------------------------------------

    @Test
    public void testOnSpecialAbilitySelectedExecutesAbilityAndTransitionsToEnemyTurn() {
        int hpBefore = enemyOne.getHp();

        controller.onSpecialAbilitySelected();

        assertTrue(enemyOne.getHp() < hpBefore);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());
    }

    // -------------------------------------------------------------------------
    // Azione: Inventario
    // -------------------------------------------------------------------------

    @Test
    public void testOnInventorySelectedTransitionToInventorySelectionState() {
        controller.onInventorySelected();

        assertEquals(BattleController.MenuState.INVENTORY_SELECTION, controller.getMenuState());
    }

    @Test
    public void testOnItemSelectedUsesItemAndTransitionsToEnemyTurn() {
        Collectible pozione = new Collectible("pozione", "Pozione", "Cura 20 HP", "HEAL", false, 20);
        playerOne.getBackpack().addItem(pozione);
        playerOne.takeDamage(30); // HP = 70
        int hpBefore = playerOne.getHp();

        controller.onInventorySelected();
        controller.onItemSelected(pozione, enemyOne);

        assertTrue(playerOne.getHp() > hpBefore, "L'oggetto di cura deve aumentare gli HP");
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());
        assertEquals(BattleController.MenuState.MAIN_MENU, controller.getMenuState());
    }

    // -------------------------------------------------------------------------
    // Fuga con tasto Q
    // -------------------------------------------------------------------------

    @Test
    public void testOnFleeAttemptWithinTimerSucceeds() {
        // Timer è a 10s, la fuga deve riuscire
        boolean fled = controller.onFleeAttempt();

        assertTrue(fled);
        assertEquals(BattlePhase.FLED, model.getPhase());
    }

    @Test
    public void testOnFleeAttemptAfterTimerExpiredFails() {
        // Simula timer scaduto
        model.updateFleeTimer(11.0f);

        boolean fled = controller.onFleeAttempt();

        assertFalse(fled);
        assertNotEquals(BattlePhase.FLED, model.getPhase());
    }

    // -------------------------------------------------------------------------
    // Turno nemico (chiamato da update)
    // -------------------------------------------------------------------------

    @Test
    public void testUpdateExecutesEnemyTurnWhenPhaseIsEnemyTurn() {
        // Porta il model a ENEMY_TURN
        controller.onAttackSelected();
        controller.onTargetSelected(enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        int hpBefore = playerOne.getHp();
        controller.update(0.016f); // simula un frame

        // Dopo update, il turno nemico deve essere eseguito e si torna a PLAYER_ONE_TURN
        assertTrue(playerOne.getHp() < hpBefore || model.getPhase() == BattlePhase.PLAYER_ONE_TURN,
            "Il turno nemico deve essere eseguito durante update");
    }

    @Test
    public void testBattleStateTransitionFullCycle() {
        // Ciclo completo: P1 attacca → ENEMY_TURN → update → P1_TURN
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());

        controller.onAttackSelected();
        controller.onTargetSelected(enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        controller.update(0.016f);
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());
    }

    // -------------------------------------------------------------------------
    // Multiplayer: entrambi i player hanno il loro turno
    // -------------------------------------------------------------------------

    @Test
    public void testMultiplayerBothPlayersGetTheirTurnBeforeEnemy() {
        BattleModel mpModel = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));
        BattleController mpController = new BattleController(mpModel);

        assertEquals(BattlePhase.PLAYER_ONE_TURN, mpModel.getPhase());

        mpController.onAttackSelected();
        mpController.onTargetSelected(enemyOne);
        assertEquals(BattlePhase.PLAYER_TWO_TURN, mpModel.getPhase());

        mpController.onAttackSelected();
        mpController.onTargetSelected(enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, mpModel.getPhase());
    }
}
