package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.Characters;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Collections;
import java.util.List;

/**
 * Implementazione di SpecialAbility che utilizza una AbilityConfiguration caricata da YAML
 * e una AbilityStrategy corrispondente.
 */
public class DataDrivenAbility implements SpecialAbility {
    private final AbilityConfiguration config;
    private final AbilityStrategy strategy;

    public DataDrivenAbility(AbilityConfiguration config) {
        this.config = config;
        this.strategy = AbilityFactory.getStrategy(config.getStrategy());
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getDescription() {
        String description = config.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }

        if (config.getBaseHealing() > 0) {
            return config.isAoe()
                    ? "Cura tutti gli alleati con un effetto ad area."
                    : "Cura un singolo alleato.";
        }

        if (config.getBaseDamage() > 0) {
            return config.isAoe()
                    ? "Infligge danni ad area a tutti i bersagli."
                    : "Infligge danni a un singolo bersaglio.";
        }

        return config.isAoe() ? "Effetto ad area" : "Bersaglio singolo";
    }

    @Override
    public void perform(Characters user, Characters target, int userLevel) {
        AbilityContext context = new AbilityContext(user, Collections.singletonList(target));
        strategy.execute(context, config);
    }

    public void performOnTargets(Characters user, List<? extends Characters> targets, int userLevel) {
        if (targets == null || targets.isEmpty()) {
            return;
        }
        AbilityContext context = new AbilityContext(user, targets);
        strategy.execute(context, config);
    }

    public AbilityConfiguration getConfig() {
        return config;
    }

    public int getEffectAmount(int userLevel) {
        if ("HEAL".equalsIgnoreCase(config.getStrategy())) {
            return config.getBaseHealing() + userLevel;
        }
        return config.getBaseDamage() + userLevel;
    }
}
