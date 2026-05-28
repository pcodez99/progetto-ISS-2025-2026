package io.github.iss_2025_2026.model.collectibles;

import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Player;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectibleEffectStrategyTest {

    @Test
    public void testHealCollectibleRestoresTargetHp() {
        Player user = new Player("Curatore", 100, 10, null);
        Player ally = new Player("Alleato", 100, 10, null);
        ally.takeDamage(30);

        Collectible potion = new Collectible("TEST_POTION", "Pozione", "Cura", "HEAL", false, 20);
        potion.use(new CollectibleUseContext(user, Arrays.asList(ally)));

        assertEquals(90, ally.getHp());
    }

    @Test
    public void testDamageCollectibleAppliesToAllTargetsWhenAoe() {
        Player user = new Player("Lanciatore", 100, 10, null);
        Player firstEnemy = new Player("Nemico 1", 100, 10, null);
        Player secondEnemy = new Player("Nemico 2", 100, 10, null);

        Collectible bomb = new Collectible("TEST_BOMB", "Bomba", "Danno ad area", "DAMAGE", true, 15);
        bomb.use(new CollectibleUseContext(user, Arrays.asList(firstEnemy, secondEnemy)));

        assertEquals(85, firstEnemy.getHp());
        assertEquals(85, secondEnemy.getHp());
    }

    @Test
    public void testDamageCollectibleAppliesOnlyToFirstTargetWhenNotAoe() {
        Player user = new Player("Lanciatore", 100, 10, null);
        Player firstEnemy = new Player("Nemico 1", 100, 10, null);
        Player secondEnemy = new Player("Nemico 2", 100, 10, null);

        Collectible dart = new Collectible("TEST_DART", "Dardo", "Danno singolo", "DAMAGE", false, 15);
        dart.use(new CollectibleUseContext(user, Arrays.asList(firstEnemy, secondEnemy)));

        assertEquals(85, firstEnemy.getHp());
        assertEquals(100, secondEnemy.getHp());
    }

    @Test
    public void testBuffCollectibleIncreasesBaseDamageByPercentage() {
        Player user = new Player("Eroe", 100, 10, null);
        Collectible elixir = new Collectible("TEST_ELIXIR", "Elisir", "Buff danno", "BUFF", false, 20);

        elixir.use(new CollectibleUseContext(user, Arrays.asList(user)));

        assertEquals(12, user.getBaseDamage());
    }

    @Test
    public void testUnknownCollectibleEffectThrowsException() {
        Player user = new Player("Eroe", 100, 10, null);
        Collectible brokenItem = new Collectible("BROKEN", "Rotto", "Effetto sconosciuto", "UNKNOWN", false, 1);

        assertThrows(IllegalArgumentException.class,
                () -> brokenItem.use(new CollectibleUseContext(user, Arrays.asList(user))));
    }
}
