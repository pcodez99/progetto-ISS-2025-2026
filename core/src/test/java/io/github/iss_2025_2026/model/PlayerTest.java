package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private static class PlayerImpl extends Player {
        public PlayerImpl(String name, int maxHp, int baseDamage, SpecialAbility ability, int maxHpGrowth, int damageGrowth) {
            super(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth);
        }
    }

    private static class MockAbility implements SpecialAbility {
        public boolean used = false;
        @Override
        public String getName() { return "Mock"; }
        @Override
        public String getDescription() { return "Mock Desc"; }
        @Override
        public void perform(Character user, Character target, int userLevel) {
            used = true;
        }
    }

    @Test
    public void testPlayerInitialization() {
        Player p = new PlayerImpl("Hero", 100, 10, null, 10, 2);
        assertEquals(1, p.getLevel());
        assertEquals(0, p.getKarma());
        assertNotNull(p.getBackpack());
    }

    @Test
    public void testModifyKarma() {
        Player p = new PlayerImpl("Hero", 100, 10, null, 10, 2);
        p.modifyKarma(20);
        assertEquals(20, p.getKarma());
        p.modifyKarma(40);
        assertEquals(50, p.getKarma()); // Clamped
        p.modifyKarma(-110);
        assertEquals(-50, p.getKarma()); // Clamped
    }

    @Test
    public void testLevelUp() {
        Player p = new PlayerImpl("Hero", 100, 10, null, 10, 5);
        p.takeDamage(50);
        p.levelUp();
        assertEquals(2, p.getLevel());
        assertEquals(110, p.getMaxHp());
        assertEquals(110, p.getHp()); // Healed on level up
        assertEquals(15, p.getBaseDamage());
    }

    @Test
    public void testUseSpecialAbility() {
        MockAbility ability = new MockAbility();
        Player p = new PlayerImpl("Hero", 100, 10, ability, 10, 2);
        p.useSpecialAbility(null);
        assertTrue(ability.used);
    }
}
