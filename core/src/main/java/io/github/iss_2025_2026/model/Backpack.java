package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.List;

/** Represents the player's inventory */
public class Backpack {
    private int capacity;
    private List<Collectible> items;

    public Backpack(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }

    public boolean addItem(Collectible item) {
        if (items.size() < capacity) {
            items.add(item);
            return true;
        }
        return false;
    }

    public boolean removeItem(Collectible item) {
        return items.remove(item);
    }

    public List<Collectible> getItems() {
        return new ArrayList<>(items);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return items.size();
    }
}
