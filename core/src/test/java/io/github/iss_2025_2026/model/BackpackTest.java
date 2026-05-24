package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BackpackTest {


    @Test
    public void testBackpackCapacity() {
        Backpack b = new Backpack(2);
        //creiamo collectible passando i 6 parametri
        Collectible item1=new Collectible("ID_1", "Item1","Desc","Test",false,0);
        Collectible item2=new Collectible("ID_2", "Item2","Desc","Test",false,0);
        Collectible item3=new Collectible("ID_3", "Item3","Desc","Test",false,0);
        assertTrue(b.addItem(item1));
        assertTrue(b.addItem(item2));
        assertFalse(b.addItem(item3));
    }

    @Test
    public void testRemoveItem() {
        Backpack b = new Backpack(10);
        Collectible item1=new Collectible("ID_1", "Item1","Desc","Test",false,0);
        b.addItem(item1);
        assertEquals(1, b.getSize());
        assertTrue(b.removeItem(item1));
        assertEquals(0, b.getSize());
    }

    @Test
    public void testImmutableList() {
        Collectible item1=new Collectible("ID_1", "Item1","Desc","Test",false,0);
        Backpack b = new Backpack(5);
        b.addItem(item1);

        List<Collectible> items = b.getItems();
        assertThrows(UnsupportedOperationException.class, () -> {
            items.add(item1);
        });
    }
}
