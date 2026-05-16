package io.github.iss_2025_2026.model;

/** Represents an item that can be collected and put into the backpack */
public abstract class Collectible {
    private final String name;
    private final String description;

    /** Constructor with safety checks for null values */
    public Collectible(String name, String description) {
        this.name = (name != null) ? name : "Unknown Item";
        this.description = (description != null) ? description : "";
    }

    /** 
     * Defines the effect of the item when used.
     * @param target The character on which the item is used.
     */
    public abstract void use(Character target);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
