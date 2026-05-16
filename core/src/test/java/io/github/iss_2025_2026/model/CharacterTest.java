package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {

    private static class CharacterImpl extends Character {
        public CharacterImpl(String name, int maxHp, int baseDamage, int level) {
            super(name, maxHp, baseDamage, level);
        }
    }

    @Test
    public void testCharacterInitialization() {
        Character c = new CharacterImpl("Hero", 100, 10, 1);
        assertEquals("Hero", c.getName());
        assertEquals(100, c.getMaxHp());
        assertEquals(100, c.getHp());
        assertEquals(10, c.getBaseDamage());
        assertEquals(1, c.getLevel());
    }

    @Test
    public void testTakeDamage() {
        Character c = new CharacterImpl("Hero", 100, 10, 1);
        c.takeDamage(30);
        assertEquals(70, c.getHp());
        assertTrue(c.isAlive());
    }

    @Test
    public void testCharacterDeath() {
        Character c = new CharacterImpl("Hero", 100, 10, 1);
        c.takeDamage(100);
        assertEquals(0, c.getHp());
        assertFalse(c.isAlive());
    }

    @Test
    public void testSafeDamage() {
        Character c = new CharacterImpl("Hero", 100, 10, 1);
        c.takeDamage(150);
        assertEquals(0, c.getHp());
        assertFalse(c.isAlive());
    }
}
