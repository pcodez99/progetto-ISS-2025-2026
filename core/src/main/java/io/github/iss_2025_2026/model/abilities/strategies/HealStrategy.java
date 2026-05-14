package io.github.iss_2025_2026.model.abilities.strategies;

import io.github.iss_2025_2026.model.abilities.AbilityStrategy;
import io.github.iss_2025_2026.model.abilities.AbilityContext;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.Character;

public class HealStrategy implements AbilityStrategy {

    @Override
    public void execute(AbilityContext context, AbilityConfiguration config) {
        Character caster = context.getCaster();

        // Healing scaling formula
        int totalHeal = config.getBaseHealing() + (config.getHealingForLevel() * caster.getLevel());

        System.out.println(caster.getName() + " uses " + config.getName() + "!");

        for (Character target : context.getTargets()) {
            target.heal(totalHeal);
            System.out.println(target.getName() + " recovers " + totalHeal + " HP!");
        }
    }
}
