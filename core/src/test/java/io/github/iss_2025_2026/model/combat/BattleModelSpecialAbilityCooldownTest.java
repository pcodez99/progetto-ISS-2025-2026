package io.github.iss_2025_2026.model.combat;

import static org.junit.jupiter.api.Assertions.*;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.abilities.DataDrivenAbility;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify special ability single-use-per-battle behavior.
 */
public class BattleModelSpecialAbilityCooldownTest {

    @Test
    public void testSpecialAbilityCanBeUsedOnlyOncePerBattle() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setName("Test Strike");
        config.setStrategy("DAMAGE");
        config.setBaseDamage(10);

        Player player = new Player("Hero", 100, 5, new DataDrivenAbility(config));
        Enemy enemy = new Enemy("Orco", "orco", 50, 0, 10, false);

        BattleModel model = new BattleModel(player, null, Arrays.asList(enemy));

        int hpBefore = enemy.getHp();
        model.executePlayerSpecialAbility(player);
        int hpAfterFirst = enemy.getHp();
        assertTrue(hpAfterFirst < hpBefore, "L'abilità dovrebbe infliggere danni al primo utilizzo");

        // Second use in same battle should be blocked
        model.executePlayerSpecialAbility(player);
        int hpAfterSecond = enemy.getHp();
        assertEquals(hpAfterFirst, hpAfterSecond, "Il secondo utilizzo non deve causare danni");

        // The battle log must contain the blocked-message
        boolean found = model.getBattleLog().stream().anyMatch(s -> s.contains("L'abilità non può più essere usata per questo combattimento"));
        assertTrue(found, "Il log deve contenere il messaggio di blocco dell'abilità");
    }

    @Test
    public void testSpecialAbilityResetBetweenBattles() {
        AbilityConfiguration config = new AbilityConfiguration();
        config.setName("Test Strike");
        config.setStrategy("DAMAGE");
        config.setBaseDamage(8);

        Player player = new Player("Hero", 100, 5, new DataDrivenAbility(config));
        Enemy enemy1 = new Enemy("Orco1", "orco1", 50, 0, 10, false);
        BattleModel firstBattle = new BattleModel(player, null, Arrays.asList(enemy1));

        firstBattle.executePlayerSpecialAbility(player);
        assertTrue(player.hasUsedSpecialAbilityThisBattle(), "Dopo l'uso, il giocatore deve essere marcato come usato");

        // New battle with same Player instance should reset the used flag in constructor
        Enemy enemy2 = new Enemy("Orco2", "orco2", 50, 0, 10, false);
        BattleModel secondBattle = new BattleModel(player, null, Arrays.asList(enemy2));
        assertFalse(player.hasUsedSpecialAbilityThisBattle(), "All'inizio della nuova battaglia lo stato deve essere resettato");

        // Now the ability should be usable again
        int hpBefore = enemy2.getHp();
        secondBattle.executePlayerSpecialAbility(player);
        assertTrue(enemy2.getHp() < hpBefore, "L'abilità deve poter essere usata nella nuova battaglia");
    }
}

