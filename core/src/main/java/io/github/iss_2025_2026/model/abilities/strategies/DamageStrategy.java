package io.github.iss_2025_2026.model.abilities.strategies;

import io.github.iss_2025_2026.model.abilities.AbilityStrategy;
import io.github.iss_2025_2026.model.abilities.AbilityContext;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.Characters;

/**
 * implementa l'interfaccia AbilityStrategy,contiene le formule matematiche per
 * calcolare i danni basandosi sulle statistiche e sul livello del personaggio
 */

public class DamageStrategy implements AbilityStrategy {

    @Override
    public void execute(AbilityContext context, AbilityConfiguration config) {
        Characters caster = context.getCaster();

        // Damage scaling formula
        int totalDamage = config.getBaseDamage() + caster.getLevel();

        for (Characters target : context.getTargets()) {
            target.takeDamage(totalDamage);
        }
    }
}
