package io.github.iss_2025_2026.model.collectibles;

import io.github.iss_2025_2026.model.Collectible;

public interface CollectibleEffectStrategy {
    void apply(CollectibleUseContext context, Collectible collectible);
}
