package io.github.iss_2025_2026.map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelCatalog {
    private final List<LevelDefinition> levels;
    private final Map<Integer, LevelDefinition> byId;

    public LevelCatalog(List<LevelDefinition> levels) {
        List<LevelDefinition> sortedLevels = new ArrayList<>(levels != null ? levels : Collections.<LevelDefinition>emptyList());
        Collections.sort(sortedLevels, new Comparator<LevelDefinition>() {
            @Override
            public int compare(LevelDefinition first, LevelDefinition second) {
                return Integer.compare(first.getId(), second.getId());
            }
        });

        this.levels = Collections.unmodifiableList(sortedLevels);
        this.byId = new HashMap<>();
        for (LevelDefinition level : sortedLevels) {
            byId.put(level.getId(), level);
        }
    }

    public static LevelCatalog load(LevelAssetResolver resolver) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (InputStream inputStream = resolver.open(TmxMapContract.LEVELS_MANIFEST_PATH)) {
            LevelManifest manifest = mapper.readValue(inputStream, LevelManifest.class);
            return new LevelCatalog(manifest.getLevels());
        }
    }

    public List<LevelDefinition> getLevels() {
        return levels;
    }

    public LevelDefinition requireLevel(int id) {
        LevelDefinition level = byId.get(id);
        if (level == null) {
            throw new IllegalArgumentException("Livello non dichiarato nel manifest: " + id);
        }
        return level;
    }

    public boolean hasLevel(int id) {
        return byId.containsKey(id);
    }
}
