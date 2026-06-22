package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private static class MockAbility implements SpecialAbility {
        public boolean used = false;
        @Override
        public String getName() { return "Mock"; }
        @Override
        public String getDescription() { return "Mock Desc"; }
        @Override
        public void perform(Characters user, Characters target, int userLevel) {
            used = true;
        }
    }

    @Test
    public void testPlayerInitialization() {
        Player p = new Player("Hero", 100, 10, null, 10, 2, 1);
        assertEquals(1, p.getLevel());
        assertEquals(0, p.getKarma());
        assertNotNull(p.getBackpack());
    }

    @Test
    public void testModifyKarma() {
        Player p = new Player("Hero", 100, 10, null, 10, 2, 1);
        p.modifyKarma(20);
        assertEquals(20, p.getKarma());
        p.modifyKarma(40);
        assertEquals(50, p.getKarma()); // Clamped
        p.modifyKarma(-110);
        assertEquals(-50, p.getKarma()); // Clamped
    }

    @Test
    public void testLevelUp() {
        Player p = new Player("Hero", 100, 10, null, 10, 5, 1);
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
        Player p = new Player("Hero", 100, 10, ability, 10, 2, 1);
        p.useSpecialAbility(null);
        assertTrue(ability.used);
    }

    @Test
    public void testAbilitySlotsRespectUnlocks() {
        MockAbility base = new MockAbility();
        MockAbility altruistic = new MockAbility();
        Player p = new Player("Hero", 100, 10, base, 10, 2, 1);
        p.setAbilitySlot(AbilitySlot.ALTRUISTIC, altruistic);

        assertEquals(base, p.getAbility());
        assertFalse(p.selectAbilitySlot(AbilitySlot.ALTRUISTIC));

        p.unlockAbilitySlot(AbilitySlot.ALTRUISTIC);
        assertTrue(p.selectAbilitySlot(AbilitySlot.ALTRUISTIC));
        assertEquals(altruistic, p.getAbility());
    }
}
