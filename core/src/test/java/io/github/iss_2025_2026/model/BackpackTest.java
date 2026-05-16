package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BackpackTest {

    private static class TestItem extends Collectible {
        public TestItem(String name, String description) {
            super(name, description);
        }
        @Override
        public void use(Character target) {
            // No-op for test
        }
    }

    @Test
    public void testBackpackCapacity() {
        Backpack b = new Backpack(2);
        assertTrue(b.addItem(new TestItem("Item1", "Desc1")));
        assertTrue(b.addItem(new TestItem("Item2", "Desc2")));
        assertFalse(b.addItem(new TestItem("Item3", "Desc3")));
    }

    @Test
    public void testRemoveItem() {
        Backpack b = new Backpack(10);
        Collectible item = new TestItem("Key", "Opens doors");
        b.addItem(item);
        assertEquals(1, b.getSize());
        assertTrue(b.removeItem(item));
        assertEquals(0, b.getSize());
    }

    @Test
    public void testImmutableList() {
        Backpack b = new Backpack(5);
        b.addItem(new TestItem("Potion", "Heals"));
        
        List<Collectible> items = b.getItems();
        assertThrows(UnsupportedOperationException.class, () -> {
            items.add(new TestItem("Illegal", "Should fail"));
        });
    }
}
