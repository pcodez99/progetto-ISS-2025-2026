package io.github.iss_2025_2026.map;

/**
 * Runtime scene data for one loaded level.
 * It is the libGDX equivalent of the currently active Unity Scene.
 */
public final class LevelRuntime {
    private final LevelDefinition definition;
    private final TmxLevel level;

    public LevelRuntime(LevelDefinition definition, TmxLevel level) {
        if (definition == null) {
            throw new IllegalArgumentException("La definizione del livello non puo essere nulla.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Il runtime TMX del livello non puo essere nullo.");
        }
        this.definition = definition;
        this.level = level;
    }

    public LevelDefinition getDefinition() {
        return definition;
    }

    public TmxLevel getLevel() {
        return level;
    }

    public int getId() {
        return definition.getId();
    }

    public String getName() {
        return definition.getName();
    }
}
