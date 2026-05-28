package io.github.iss_2025_2026.model.collectibles;

import io.github.iss_2025_2026.model.collectibles.strategies.BuffCollectibleStrategy;
import io.github.iss_2025_2026.model.collectibles.strategies.DamageCollectibleStrategy;
import io.github.iss_2025_2026.model.collectibles.strategies.HealCollectibleStrategy;
import java.util.HashMap;
import java.util.Map;

public final class CollectibleEffectFactory {
    private static final Map<String, CollectibleEffectStrategy> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("HEAL", new HealCollectibleStrategy());
        REGISTRY.put("DAMAGE", new DamageCollectibleStrategy());
        REGISTRY.put("BUFF", new BuffCollectibleStrategy());
    }

    private CollectibleEffectFactory() {
    }

    public static CollectibleEffectStrategy getStrategy(String effectType) {
        if (effectType == null || effectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo di effetto del collectible non puo essere nullo o vuoto.");
        }

        CollectibleEffectStrategy strategy = REGISTRY.get(effectType.trim().toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Nessuna strategia collectible trovata per: " + effectType);
        }
        return strategy;
    }
}
