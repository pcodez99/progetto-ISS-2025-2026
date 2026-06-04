package io.github.iss_2025_2026.model.combat;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.abilities.DataDrivenAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BattleModelAbilityCooldownTest {

    private Player createPlayerWithAbility(String name) {
        AbilityConfiguration abilityConfig = new AbilityConfiguration();
        abilityConfig.setId("attack_ability");
        abilityConfig.setName("Special Attack");
        abilityConfig.setDescription("A powerful attack");
        abilityConfig.setStrategy("DAMAGE");
        abilityConfig.setCooldown(1);
        abilityConfig.setBaseDamage(15);
        abilityConfig.setAoe(false);

        SpecialAbility ability = new DataDrivenAbility(abilityConfig);

        Player player = new Player(name, 100, 10, ability, 10, 2, 1);
        return player;
    }

    private Enemy createEnemy(String name) {
        Enemy enemy = new Enemy(name, "test_enemy", 50, 5, 10, false);
        return enemy;
    }

    @Test
    public void testAbilitySingleUsePerBattle() {
        Player player = createPlayerWithAbility("Guerriero");
        Enemy enemy = createEnemy("Goblin");
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);

        BattleModel model = new BattleModel(player, null, enemies);

        // Prima esecuzione: l'abilità deve funzionare
        int hpEnemyBefore = enemy.getHp();
        model.executePlayerSpecialAbility(player);
        int hpEnemyAfter = enemy.getHp();

        // Verifico che il danno sia stato inflitto
        assertTrue(hpEnemyBefore > hpEnemyAfter, "Il primo uso deve causare danno");

        // Verifico che il giocatore sia segnato come avente usato l'abilità
        assertTrue(player.hasUsedSpecialAbilityThisBattle(), "Il giocatore deve essere segnato come avente usato l'abilità");

        // Secondo tentativo: l'abilità deve essere bloccata
        hpEnemyBefore = enemy.getHp();
        model.executePlayerSpecialAbility(player);
        hpEnemyAfter = enemy.getHp();

        // Verifico che il danno NON sia stato inflitto (HP rimane uguale)
        assertEquals(hpEnemyBefore, hpEnemyAfter, "Il secondo uso non deve causare danno");

        // Verifico il messaggio nel log
        boolean messageFound = model.getBattleLog().stream()
            .anyMatch(s -> s.contains("L'abilità non può più essere usata per questo combattimento"));
        assertTrue(messageFound, "Il log deve contenere il messaggio di abilità non disponibile");
    }

    @Test
    public void testAbilityResetBetweenBattles() {
        Player player = createPlayerWithAbility("Guerriero");
        Enemy enemy1 = createEnemy("Goblin");
        List<Enemy> enemies1 = new ArrayList<>();
        enemies1.add(enemy1);

        // Prima battaglia
        BattleModel model1 = new BattleModel(player, null, enemies1);
        model1.executePlayerSpecialAbility(player);
        assertTrue(player.hasUsedSpecialAbilityThisBattle(), "Dopo l'uso in battaglia 1, l'abilità deve essere esaurita");

        // Seconda battaglia (nuovo BattleModel)
        Enemy enemy2 = createEnemy("Orco");
        List<Enemy> enemies2 = new ArrayList<>();
        enemies2.add(enemy2);

        BattleModel model2 = new BattleModel(player, null, enemies2);

        // L'abilità deve essere resettata
        assertFalse(player.hasUsedSpecialAbilityThisBattle(), "Dopo l'inizio di una nuova battaglia, l'abilità deve essere resettata");

        // E deve poter essere usata di nuovo
        int hpEnemyBefore = enemy2.getHp();
        model2.executePlayerSpecialAbility(player);
        int hpEnemyAfter = enemy2.getHp();
        assertTrue(hpEnemyBefore > hpEnemyAfter, "L'abilità deve poter essere usata una volta nella nuova battaglia");
    }

    @Test
    public void testTwoPlayersCooldownIndependent() {
        Player player1 = createPlayerWithAbility("Guerriero");
        Player player2 = createPlayerWithAbility("Mago");
        Enemy enemy = createEnemy("Goblin");
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);

        BattleModel model = new BattleModel(player1, player2, enemies);

        // Player 1 usa l'abilità
        int hpBefore = enemy.getHp();
        model.executePlayerSpecialAbility(player1);
        int hpAfter = enemy.getHp();
        assertTrue(hpBefore > hpAfter, "Player 1 deve infliggere danno");
        assertTrue(player1.hasUsedSpecialAbilityThisBattle(), "Player 1 deve essere segnato come avente usato l'abilità");

        // Player 2 deve ancora poter usare l'abilità (cooldown indipendente)
        assertFalse(player2.hasUsedSpecialAbilityThisBattle(), "Player 2 non deve essere segnato come avente usato l'abilità");
        hpBefore = enemy.getHp();
        model.executePlayerSpecialAbility(player2);
        hpAfter = enemy.getHp();
        assertTrue(hpBefore > hpAfter, "Player 2 deve infliggere danno");
        assertTrue(player2.hasUsedSpecialAbilityThisBattle(), "Player 2 deve essere segnato come avente usato l'abilità");
    }

    @Test
    public void testAbilityLogMessagesCorrect() {
        Player player = createPlayerWithAbility("Guerriero");
        Enemy enemy = createEnemy("Goblin");
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);

        BattleModel model = new BattleModel(player, null, enemies);

        // Primo uso: successo
        model.executePlayerSpecialAbility(player);
        List<String> log = model.getBattleLog().stream().collect(Collectors.toList());
        boolean successMessageFound = log.stream()
            .anyMatch(s -> s.contains("usa") && s.contains("Special Attack") && s.contains("Danni"));
        assertTrue(successMessageFound, "Il log deve contenere il messaggio di successo dell'abilità");

        // Secondo uso: fallimento
        model.executePlayerSpecialAbility(player);
        log = model.getBattleLog().stream().collect(Collectors.toList());
        boolean failMessageFound = log.stream()
            .anyMatch(s -> s.contains("L'abilità non può più essere usata per questo combattimento"));
        assertTrue(failMessageFound, "Il log deve contenere il messaggio di impossibilità di usare l'abilità");
    }

    @Test
    public void testPlayerAbilityUsageState() {
        Player player = createPlayerWithAbility("Guerriero");

        // Inizialmente non deve aver usato l'abilità
        assertFalse(player.hasUsedSpecialAbilityThisBattle(), "Inizialmente l'abilità deve essere disponibile");

        // Marchiamo come usata
        player.markSpecialAbilityUsed();
        assertTrue(player.hasUsedSpecialAbilityThisBattle(), "Dopo markSpecialAbilityUsed, deve risultare come usata");

        // Resettiamo
        player.resetSpecialAbilityUsageForBattle();
        assertFalse(player.hasUsedSpecialAbilityThisBattle(), "Dopo reset, l'abilità deve essere disponibile di nuovo");
    }

    @Test
    public void testCooldownConfigurationDefault() {
        AbilityConfiguration config = new AbilityConfiguration();
        // Nessun cooldown impostato manualmente, dovrebbe essere 1 di default
        assertEquals(1, config.getCooldown(), "Il cooldown dovrebbe essere 1 di default");
    }
}





