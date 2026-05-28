package io.github.iss_2025_2026.map;

import java.util.ArrayList;
import java.util.List;

public class LevelManifest {
    private List<LevelDefinition> levels = new ArrayList<>();

    public List<LevelDefinition> getLevels() {
        return levels;
    }

    public void setLevels(List<LevelDefinition> levels) {
        this.levels = levels != null ? levels : new ArrayList<LevelDefinition>();
    }
}
