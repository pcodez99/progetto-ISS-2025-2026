package io.github.iss_2025_2026.model.abilities.strategies;

import io.github.iss_2025_2026.model.abilities.AbilityStrategy;
import io.github.iss_2025_2026.model.abilities.AbilityContext;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.Character;

public class DamageStrategy implements AbilityStrategy {

    @Override
    public void execute(AbilityContext context, AbilityConfiguration config) {
        Character caster = context.getCaster();

        // Damage scaling formula
        int totalDamage = config.getBaseDamage() + (config.getDamageForLevel() * caster.getLevel());

        System.out.println(caster.getName() + " uses " + config.getName() + "!");

        for (Character target : context.getTargets()) {
            target.takeDamage(totalDamage);
            System.out.println("Dealt " + totalDamage + " damage to " + target.getName());
        }
    }
}
