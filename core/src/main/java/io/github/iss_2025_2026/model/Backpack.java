package io.github.iss_2025_2026.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Represents the player's inventory */
public class Backpack {
    private int capacity;
    private List<Collectible> items;

    /** Constructor with capacity */
    public Backpack(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.items = new ArrayList<>();
    }

    /** Default constructor for serialization */
    public Backpack() {
        this.capacity = 10;
        this.items = new ArrayList<>();
    }

    /**
     * Adds an item to the backpack if there is space.
     * @param item The item to add
     * @return true if added, false otherwise
     */
    public boolean addItem(Collectible item) {
        if (item != null && items.size() < capacity) {
            items.add(item);
            return true;
        }
        return false;
    }

    /**
     * Removes an item from the backpack.
     * @param item The item to remove
     * @return true if removed, false otherwise
     */
    public boolean removeItem(Collectible item) {
        return items.remove(item);
    }

    /**
     * Returns an unmodifiable view of the items.
     * @return List of items
     */
    public List<Collectible> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Sets the items list (required for deserialization).
     * @param items List of items
     */
    public void setItems(List<Collectible> items) {
        this.items = items != null ? items : new ArrayList<Collectible>();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    @JsonIgnore
    public int getSize() {
        return items.size();
    }
}
