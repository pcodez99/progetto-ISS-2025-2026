package io.github.iss_2025_2026.model;

/** Represents an item that can be collected and put into the backpack */
public class Collectible {
    private String name;
    private String description;

    public Collectible(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
