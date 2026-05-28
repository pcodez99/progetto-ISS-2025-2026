package io.github.iss_2025_2026.map;

public class LevelDefinition {
    private int id;
    private String name;
    private String map;

    public LevelDefinition() {
    }

    public LevelDefinition(int id, String name, String map) {
        this.id = id;
        this.name = name;
        this.map = map;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getMapPath() {
        return map;
    }
}
