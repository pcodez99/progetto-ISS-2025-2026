package io.github.iss_2025_2026.model.collectibles.strategies;

import io.github.iss_2025_2026.model.Characters;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.collectibles.CollectibleEffectStrategy;
import io.github.iss_2025_2026.model.collectibles.CollectibleUseContext;

public class HealCollectibleStrategy implements CollectibleEffectStrategy {
    @Override
    public void apply(CollectibleUseContext context, Collectible collectible) {
        for (Characters target : context.getTargets(collectible.isAoe())) {
            target.heal(collectible.getEffectValue());
        }
    }
}
