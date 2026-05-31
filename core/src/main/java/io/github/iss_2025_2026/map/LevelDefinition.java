package io.github.iss_2025_2026.map;

import java.util.ArrayList;
import java.util.List;

public class LevelDefinition {
    private int id;
    private String name;
    private String map;
    private List<CheckpointDefinition> checkpoints = new ArrayList<>();

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

    public List<CheckpointDefinition> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<CheckpointDefinition> checkpoints) {
        this.checkpoints = checkpoints != null ? checkpoints : new ArrayList<CheckpointDefinition>();
    }
}
