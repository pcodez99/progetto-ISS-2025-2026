package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.Character;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Collections;

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
        return config.isAoe() ? "Effetto ad area (AoE)" : "Bersaglio singolo";
    }

    @Override
    public void perform(Character user, Character target, int userLevel) {
        // Creiamo il contesto dell'azione. 
        // Nota: Qui passiamo una lista contenente solo il target per semplicità, 
        // ma se AoE = true, il controller dovrebbe passare tutti i target validi.
        AbilityContext context = new AbilityContext(user, Collections.singletonList(target));
        strategy.execute(context, config);
    }

    public AbilityConfiguration getConfig() {
        return config;
    }
}
