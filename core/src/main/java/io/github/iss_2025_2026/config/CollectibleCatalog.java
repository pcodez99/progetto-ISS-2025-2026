package io.github.iss_2025_2026.config;

import io.github.iss_2025_2026.model.Collectible;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catalogo validato che separa i prototipi di gioco dalla configurazione grafica. */
public final class CollectibleCatalog {
    private final Map<String, Collectible> prototypes;
    private final Map<String, CollectibleVisualConfig> visualConfigs;

    public CollectibleCatalog(List<CollectibleDefinition> definitions) {
        Map<String, Collectible> loadedPrototypes = new LinkedHashMap<>();
        Map<String, CollectibleVisualConfig> loadedVisuals = new LinkedHashMap<>();
        for (CollectibleDefinition definition : definitions) {
            loadedPrototypes.put(definition.getId(), definition.toCollectible());
            loadedVisuals.put(definition.getId(), definition.getVisual().copy());
        }
        this.prototypes = Collections.unmodifiableMap(loadedPrototypes);
        this.visualConfigs = Collections.unmodifiableMap(loadedVisuals);
    }

    public List<Collectible> getCollectibles() {
        List<Collectible> collectibles = new ArrayList<>();
        for (Collectible prototype : prototypes.values()) {
            collectibles.add(prototype.copy());
        }
        return collectibles;
    }

    public Map<String, CollectibleVisualConfig> getVisualConfigs() {
        Map<String, CollectibleVisualConfig> result = new LinkedHashMap<>();
        for (Map.Entry<String, CollectibleVisualConfig> entry : visualConfigs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(result);
    }
}
