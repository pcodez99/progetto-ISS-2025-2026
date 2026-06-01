package io.github.iss_2025_2026.model.combat;

import static org.junit.jupiter.api.Assertions.*;

import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.abilities.DataDrivenAbility;
import io.github.iss_2025_2026.model.Character;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test TDD per BattleModel.
 * Questi test sono scritti PRIMA dell'implementazione (fase RED).
 * La classe BattleModel deve essere implementata per renderli GREEN.
 */
public class BattleModelTest {

    private Player playerOne;
    private Player playerTwo;
    private Enemy enemyOne;
    private Enemy enemyTwo;
    private SpecialAbility dummyAbility;

    @BeforeEach
    public void setUp() {
        dummyAbility = new SpecialAbility() {
            @Override public String getName() { return "Test Ability"; }
            @Override public String getDescription() { return "Abilita di test"; }
            @Override public void perform(Character user, Character target, int userLevel) {
                target.takeDamage(15);
            }
        };

        playerOne = new Player("Viddano", 100, 10, dummyAbility);
        playerTwo = new Player("Mamma", 80, 8, null);
        enemyOne  = new Enemy("Alieno Base", "alieno_base", 45, 8, 15, false);
        enemyTwo  = new Enemy("Alieno Guardiano", "alieno_guardiano", 90, 14, 30, false);
    }

    // -------------------------------------------------------------------------
    // Inizializzazione
    // -------------------------------------------------------------------------

    @Test
    public void testBattleInitializesCorrectlySinglePlayer() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        assertNotNull(model);
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());
        assertEquals(1, model.getAliveEnemies().size());
        assertFalse(model.isBattleOver());
    }

    @Test
    public void testBattleInitializesCorrectlyMultiplayer() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne, enemyTwo));

        assertNotNull(model);
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());
        assertEquals(2, model.getAliveEnemies().size());
        assertEquals(2, model.getAlivePlayers().size());
    }

    @Test
    public void testFleeTimerStartsAt10Seconds() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        assertEquals(10.0f, model.getFleeTimer(), 0.001f);
    }

    // -------------------------------------------------------------------------
    // Turni
    // -------------------------------------------------------------------------

    @Test
    public void testAfterPlayerOneActionInSinglePlayerGoesToEnemyTurn() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());

        model.executePlayerAttack(playerOne, enemyOne);

        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());
    }

    @Test
    public void testAfterPlayerOneActionInMultiplayerGoesToPlayerTwoTurn() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));
        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());

        model.executePlayerAttack(playerOne, enemyOne);

        assertEquals(BattlePhase.PLAYER_TWO_TURN, model.getPhase());
    }

    @Test
    public void testAfterPlayerTwoActionGoesToEnemyTurn() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));

        model.executePlayerAttack(playerOne, enemyOne);
        assertEquals(BattlePhase.PLAYER_TWO_TURN, model.getPhase());

        model.executePlayerAttack(playerTwo, enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());
    }

    // -------------------------------------------------------------------------
    // Attacco del giocatore
    // -------------------------------------------------------------------------

    @Test
    public void testAttackReducesEnemyHp() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        int hpBefore = enemyOne.getHp();

        model.executePlayerAttack(playerOne, enemyOne);

        assertTrue(enemyOne.getHp() < hpBefore,
            "L'HP del nemico deve diminuire dopo l'attacco del player");
    }

    @Test
    public void testAttackDamageEqualsPlayerBaseDamage() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        model.executePlayerAttack(playerOne, enemyOne);

        assertEquals(45 - playerOne.getBaseDamage(), enemyOne.getHp());
    }

    // -------------------------------------------------------------------------
    // Abilità speciale
    // -------------------------------------------------------------------------

    @Test
    public void testSpecialAbilityReducesEnemyHp() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        int hpBefore = enemyOne.getHp();

        model.executePlayerSpecialAbility(playerOne);

        assertTrue(enemyOne.getHp() < hpBefore);
    }

    @Test
    public void testSpecialAbilityWithNoAbilityDoesNothing() {
        BattleModel model = new BattleModel(playerTwo, null, Arrays.asList(enemyOne));
        int hpBefore = enemyOne.getHp();

        // playerTwo non ha abilità, non deve causare crash
        model.executePlayerSpecialAbility(playerTwo);

        // L'HP non deve cambiare se non c'è abilità
        assertEquals(hpBefore, enemyOne.getHp());
    }

    @Test
    public void testSpecialAbilityLogShowsDamageAmount() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setName("Pioggia di Pietre");
        config.setStrategy("DAMAGE");
        config.setBaseDamage(15);

        playerOne = new Player("Bambino", 100, 10, new DataDrivenAbility(config));
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        model.executePlayerSpecialAbility(playerOne);

        String logLine = model.getBattleLog().get(model.getBattleLog().size() - 1);
        assertTrue(logLine.contains("Danni: 16 HP"), "Il log deve riportare i danni dell'abilità (15 + livello 1)");
    }

    @Test
    public void testSpecialAbilityLogShowsHealAmount() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setName("Cura di Gruppo");
        config.setStrategy("HEAL");
        config.setBaseHealing(20);

        Player healer = new Player("Mamma", 80, 8, new DataDrivenAbility(config));
        BattleModel model = new BattleModel(playerOne, healer, Arrays.asList(enemyOne));

        model.executePlayerSpecialAbility(healer);

        String logLine = model.getBattleLog().get(model.getBattleLog().size() - 1);
        assertTrue(logLine.contains("Cura: 21 HP"), "Il log deve riportare la cura dell'abilità (20 + livello 1)");
    }

    @Test
    public void testAoeDamageAbilityHitsAllEnemies() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setId("STONE_RAIN");
        config.setName("Pioggia di Pietre");
        config.setStrategy("DAMAGE");
        config.setAoe(true);
        config.setBaseDamage(10);

        playerOne = new Player("Bambino", 100, 10, new DataDrivenAbility(config));
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne, enemyTwo));

        model.executePlayerSpecialAbility(playerOne);

        assertTrue(enemyOne.getHp() < 45);
        assertTrue(enemyTwo.getHp() < 90);
    }

    @Test
    public void testGroupHealAbilityHealsAllPlayers() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setId("GROUP_HEAL");
        config.setName("Cura di Gruppo");
        config.setStrategy("HEAL");
        config.setAoe(true);
        config.setBaseHealing(20);

        playerOne.takeDamage(30);
        Player healer = new Player("Mamma", 80, 8, new DataDrivenAbility(config));
        healer.takeDamage(40);

        BattleModel model = new BattleModel(playerOne, healer, Arrays.asList(enemyOne));
        int hp1Before = playerOne.getHp();
        int hp2Before = healer.getHp();
        int enemyHpBefore = enemyOne.getHp();

        model.executePlayerSpecialAbility(healer);

        assertTrue(playerOne.getHp() > hp1Before, "La cura deve riguardare gli alleati");
        assertTrue(healer.getHp() > hp2Before, "La cura deve riguardare gli alleati");
        assertEquals(enemyHpBefore, enemyOne.getHp(), "I nemici non devono essere curati");
    }

    // -------------------------------------------------------------------------
    // Uso oggetto dall'inventario
    // -------------------------------------------------------------------------

    @Test
    public void testUseHealItemIncreasesPlayerHp() {
        playerOne.takeDamage(30); // HP = 70
        Collectible healPotion = new Collectible("pozione", "Pozione", "Cura 20 HP", "HEAL", false, 20);
        playerOne.getBackpack().addItem(healPotion);

        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        model.executeUseItem(playerOne, healPotion, enemyOne);

        assertTrue(playerOne.getHp() > 70, "L'HP del player deve aumentare dopo aver usato la pozione");
    }

    @Test
    public void testUseItemRemovesItFromBackpack() {
        Collectible healPotion = new Collectible("pozione", "Pozione", "Cura 20 HP", "HEAL", false, 20);
        playerOne.getBackpack().addItem(healPotion);
        assertEquals(1, playerOne.getBackpack().getSize());

        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        model.executeUseItem(playerOne, healPotion, enemyOne);

        assertEquals(0, playerOne.getBackpack().getSize(), "L'oggetto usato deve essere rimosso dallo zaino");
    }

    // -------------------------------------------------------------------------
    // Turno del nemico
    // -------------------------------------------------------------------------

    @Test
    public void testEnemyAttackReducesTargetPlayerHp() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        int hpBefore = playerOne.getHp();

        // Porta il model a ENEMY_TURN
        model.executePlayerAttack(playerOne, enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        model.executeEnemyTurn();

        assertTrue(playerOne.getHp() < hpBefore,
            "L'HP del player deve diminuire dopo l'attacco del nemico");
    }

    @Test
    public void testEnemyTurnAttacksRandomPlayerAmongAlivePlayers() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));
        int hp1Before = playerOne.getHp();
        int hp2Before = playerTwo.getHp();

        // Porta a ENEMY_TURN: p1 attacca, p2 attacca
        model.executePlayerAttack(playerOne, enemyOne);
        model.executePlayerAttack(playerTwo, enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        model.executeEnemyTurn();

        // Almeno uno dei due player deve aver perso HP
        boolean attackedSomeone = playerOne.getHp() < hp1Before || playerTwo.getHp() < hp2Before;
        assertTrue(attackedSomeone, "Il nemico deve attaccare almeno uno dei player vivi");
    }

    @Test
    public void testEnemyTurnDoesNotAttackDeadPlayer() {
        // playerTwo è già a 0 HP
        playerTwo.takeDamage(playerTwo.getHp());
        assertEquals(0, playerTwo.getHp());

        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));
        int hp1Before = playerOne.getHp();

        // Porta a ENEMY_TURN: p1 attacca
        model.executePlayerAttack(playerOne, enemyOne);
        // p2 è morto, avanza automaticamente al turno nemico
        model.executeEnemyTurn();

        // Solo playerOne può essere stato attaccato
        assertTrue(playerOne.getHp() < hp1Before,
            "Con playerTwo a 0 HP, il nemico deve attaccare solo playerOne");
    }

    @Test
    public void testDeadPlayerCannotAttack() {
        playerOne.takeDamage(playerOne.getHp());
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        int enemyHpBefore = enemyOne.getHp();

        model.executePlayerAttack(playerOne, enemyOne);

        assertEquals(enemyHpBefore, enemyOne.getHp());
        assertFalse(model.canCurrentTurnPlayerAct());
        assertTrue(model.getBattleLog().stream().noneMatch(msg -> msg.contains("attacca")));
    }

    @Test
    public void testDeadPlayerCannotUseSpecialAbility() {
        playerOne.takeDamage(playerOne.getHp());
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        int enemyHpBefore = enemyOne.getHp();

        model.executePlayerSpecialAbility(playerOne);

        assertEquals(enemyHpBefore, enemyOne.getHp());
    }

    @Test
    public void testSkipDeadPlayerOneTurnAtBattleStart() {
        playerOne.takeDamage(playerOne.getHp());
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));

        assertEquals(BattlePhase.PLAYER_TWO_TURN, model.getPhase());
        assertTrue(model.canCurrentTurnPlayerAct());
    }

    @Test
    public void testAfterEnemyTurnSkipsDeadPlayerOneTurn() {
        playerOne.takeDamage(playerOne.getHp());
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));

        model.executePlayerAttack(playerTwo, enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        model.executeEnemyTurn();

        assertEquals(BattlePhase.PLAYER_TWO_TURN, model.getPhase());
        assertTrue(model.canCurrentTurnPlayerAct());
    }

    @Test
    public void testDeadEnemyDoesNotAttack() {
        enemyOne.takeDamage(enemyOne.getHp());
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne, enemyTwo));

        model.executePlayerAttack(playerOne, enemyTwo);
        model.executeEnemyTurn();

        long attacksFromDeadEnemy = model.getBattleLog().stream()
                .filter(msg -> msg.contains(enemyOne.getName()) && msg.contains("attacca"))
                .count();

        assertEquals(0, attacksFromDeadEnemy);
    }

    @Test
    public void testDeadPlayerCannotAttackDeadEnemy() {
        enemyOne.takeDamage(enemyOne.getHp());
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        model.executePlayerAttack(playerOne, enemyOne);

        assertTrue(model.getBattleLog().isEmpty());
    }

    @Test
    public void testSecondEnemyDoesNotAttackPlayerKilledEarlierInSameTurn() {
        playerTwo.takeDamage(79); // Mamma resta con 1 HP
        Enemy strongEnemyOne = new Enemy("Alieno 1", "alieno_base", 45, 10, 15, false);
        Enemy strongEnemyTwo = new Enemy("Alieno 2", "alieno_base", 45, 10, 15, false);

        Random alwaysTargetPlayerTwo = new Random() {
            @Override
            public int nextInt(int bound) {
                return bound > 1 ? 1 : 0;
            }
        };

        BattleModel model = new BattleModel(
                playerOne, playerTwo, Arrays.asList(strongEnemyOne, strongEnemyTwo), alwaysTargetPlayerTwo);

        model.executePlayerAttack(playerOne, strongEnemyOne);
        model.executePlayerAttack(playerTwo, strongEnemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        model.executeEnemyTurn();

        long attacksOnMamma = model.getBattleLog().stream()
                .filter(msg -> msg.contains("attacca Mamma"))
                .count();

        assertEquals(0, playerTwo.getHp());
        assertEquals(1, attacksOnMamma,
                "Il secondo nemico non deve attaccare Mamma se e gia stata eliminata dal primo");
    }

    @Test
    public void testAfterEnemyTurnGoesBackToPlayerOneTurn() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        model.executePlayerAttack(playerOne, enemyOne);
        assertEquals(BattlePhase.ENEMY_TURN, model.getPhase());

        model.executeEnemyTurn();

        assertEquals(BattlePhase.PLAYER_ONE_TURN, model.getPhase());
    }

    // -------------------------------------------------------------------------
    // Condizioni di fine battaglia
    // -------------------------------------------------------------------------

    @Test
    public void testBattleEndsWhenAllEnemiesDefeated() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        enemyOne.takeDamage(enemyOne.getHp()); // Uccidi il nemico

        assertTrue(model.isBattleOver());
        assertTrue(model.isVictory());
    }

    @Test
    public void testBattleEndsWhenBothPlayersDefeated() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));

        playerOne.takeDamage(playerOne.getHp());
        playerTwo.takeDamage(playerTwo.getHp());

        assertTrue(model.isBattleOver());
        assertTrue(model.isDefeat());
    }

    @Test
    public void testBattleNotOverWhenOnlyPlayerOneDefeated() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));

        playerOne.takeDamage(playerOne.getHp()); // Solo p1 a 0 HP

        assertFalse(model.isBattleOver(),
            "La battaglia non deve finire se solo uno dei due player è sconfitto");
        assertFalse(model.isDefeat());
    }

    @Test
    public void testSinglePlayerBattleEndsWhenPlayerDefeated() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));

        playerOne.takeDamage(playerOne.getHp()); // playerOne a 0 HP

        assertTrue(model.isBattleOver(),
            "In single player, la battaglia finisce se il solo player è sconfitto");
        assertTrue(model.isDefeat());
    }

    // -------------------------------------------------------------------------
    // Fuga
    // -------------------------------------------------------------------------

    @Test
    public void testFleeSucceedsWithinTimeLimit() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        // Timer inizia a 10s, non è ancora scaduto

        boolean fled = model.tryFlee();

        assertTrue(fled, "La fuga deve riuscire se il timer non è ancora scaduto");
        assertEquals(BattlePhase.FLED, model.getPhase());
    }

    @Test
    public void testFleeFailsAfterTimeLimit() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        // Simula la scadenza del timer
        model.updateFleeTimer(11.0f);

        boolean fled = model.tryFlee();

        assertFalse(fled, "La fuga deve fallire se il timer è scaduto");
        assertNotEquals(BattlePhase.FLED, model.getPhase());
    }

    @Test
    public void testFleeTimerDecreasesWithUpdate() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        float initialTimer = model.getFleeTimer();

        model.updateFleeTimer(3.0f);

        assertEquals(initialTimer - 3.0f, model.getFleeTimer(), 0.001f);
    }

    // -------------------------------------------------------------------------
    // XP e log
    // -------------------------------------------------------------------------

    @Test
    public void testXpAwardedToPlayersAfterVictory() {
        BattleModel model = new BattleModel(playerOne, playerTwo, Arrays.asList(enemyOne));
        int xpReward = enemyOne.getXpReward(); // 15

        enemyOne.takeDamage(enemyOne.getHp()); // Uccidi il nemico
        model.awardXpToPlayers();

        // Verifica che il log della battaglia contenga un messaggio di XP
        List<String> log = model.getBattleLog();
        assertTrue(log.stream().anyMatch(msg -> msg.contains("XP") || msg.contains("xp")),
            "Il log deve contenere un messaggio di assegnazione XP");
    }

    @Test
    public void testBattleLogIsUpdatedAfterPlayerAttack() {
        BattleModel model = new BattleModel(playerOne, null, Arrays.asList(enemyOne));
        assertTrue(model.getBattleLog().isEmpty());

        model.executePlayerAttack(playerOne, enemyOne);

        assertFalse(model.getBattleLog().isEmpty(), "Il log deve contenere almeno un messaggio dopo l'attacco");
    }
}
