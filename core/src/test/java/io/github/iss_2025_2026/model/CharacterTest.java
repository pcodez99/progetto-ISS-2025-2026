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

    @Test
    public void testCharacterStateAndStateTime() {
        Character c = new CharacterImpl("Hero", 100, 10, 1);
        
        // Default state is IDLE, stateTime is 0
        assertEquals(CharacterState.IDLE, c.getState());
        assertEquals(0f, c.getStateTime(), 0.001f);

        // Setting a new state should keep stateTime at 0
        c.setState(CharacterState.WALKING);
        assertEquals(CharacterState.WALKING, c.getState());
        assertEquals(0f, c.getStateTime(), 0.001f);

        // Ticking time should update stateTime
        c.updateStateTime(0.5f);
        assertEquals(0.5f, c.getStateTime(), 0.001f);

        // Setting the same state should NOT reset stateTime
        c.setState(CharacterState.WALKING);
        assertEquals(0.5f, c.getStateTime(), 0.001f);

        // Setting a different state should reset stateTime
        c.setState(CharacterState.ATTACKING);
        assertEquals(CharacterState.ATTACKING, c.getState());
        assertEquals(0f, c.getStateTime(), 0.001f);

        // Test attack duration
        c.setAttackDuration(0.5f);
        assertEquals(0.5f, c.getAttackDuration(), 0.001f);
    }
}
