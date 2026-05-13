package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BackpackTest {

    @Test
    public void testBackpackCapacity() {
        Backpack b = new Backpack(2);
        assertTrue(b.addItem(new Collectible("Item1", "Desc1")));
        assertTrue(b.addItem(new Collectible("Item2", "Desc2")));
        assertFalse(b.addItem(new Collectible("Item3", "Desc3")));
    }

    @Test
    public void testRemoveItem() {
        Backpack b = new Backpack(10);
        Collectible item = new Collectible("Key", "Opens doors");
        b.addItem(item);
        assertEquals(1, b.getSize());
        assertTrue(b.removeItem(item));
        assertEquals(0, b.getSize());
    }
}
