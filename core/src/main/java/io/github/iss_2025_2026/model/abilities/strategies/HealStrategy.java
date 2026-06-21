package io.github.iss_2025_2026.model.abilities.strategies;

import io.github.iss_2025_2026.model.abilities.AbilityStrategy;
import io.github.iss_2025_2026.model.abilities.AbilityContext;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.Characters;

/**
 * Implementa l'interfaccia AbilityStrategy e contiene le formule matematiche
 * per
 * calcolare la cura basandosi su statistiche e livello del personaggio
 */

public class HealStrategy implements AbilityStrategy {

    @Override
    public void execute(AbilityContext context, AbilityConfiguration config) {
        Characters caster = context.getCaster();

        // Healing scaling formula
        int totalHeal = config.getBaseHealing() + caster.getLevel();

        for (Characters target : context.getTargets()) {
            target.heal(totalHeal);
        }
    }
}
