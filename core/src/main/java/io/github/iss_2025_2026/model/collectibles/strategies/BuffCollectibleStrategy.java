package io.github.iss_2025_2026.model.collectibles.strategies;

import io.github.iss_2025_2026.model.Character;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.collectibles.CollectibleEffectStrategy;
import io.github.iss_2025_2026.model.collectibles.CollectibleUseContext;

public class BuffCollectibleStrategy implements CollectibleEffectStrategy {
    @Override
    public void apply(CollectibleUseContext context, Collectible collectible) {
        for (Character target : context.getTargets(collectible.isAoe())) {
            target.increaseBaseDamageByPercent(collectible.getEffectValue());
        }
    }
}
