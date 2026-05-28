package io.github.iss_2025_2026.map;

import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public final class TmxLevelLoader {
    private TmxLevelLoader() {
    }

    public static TmxLevel load(LevelDefinition level) {
        return load(level.getMapPath());
    }

    public static TmxLevel load(String mapPath) {
        return new TmxLevel(new TmxMapLoader().load(mapPath));
    }
}
