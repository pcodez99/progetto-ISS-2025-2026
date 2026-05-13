package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testPlayerInitialization() {
        Player p = new Player("Grandpa", 100, 10, null, 15, 3);
        assertEquals(1, p.getLevel());
        assertEquals(0, p.getKarma());
        assertNotNull(p.getBackpack());
    }

    @Test
    public void testKarmaModification() {
        Player p = new Player("Grandpa", 100, 10, null, 15, 3);
        p.modifyKarma(10);
        assertEquals(10, p.getKarma());
        p.modifyKarma(50);
        assertEquals(50, p.getKarma());
        p.modifyKarma(-110);
        assertEquals(-50, p.getKarma());
    }

    @Test
    public void testLevelUp() {
        Player p = new Player("Grandpa", 100, 10, null, 15, 3);
        p.levelUp();
        assertEquals(2, p.getLevel());
        assertEquals(115, p.getMaxHp());
        assertEquals(115, p.getHp());
        assertEquals(13, p.getBaseDamage());
    }
}
