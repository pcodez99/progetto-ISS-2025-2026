package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.abilities.strategies.DamageStrategy;
import io.github.iss_2025_2026.model.abilities.strategies.HealStrategy;

import java.util.HashMap;
import java.util.Map;

public class AbilityFactory {

    private final Map<String, AbilityStrategy> registry;

    public AbilityFactory() {
        this.registry = new HashMap<>();

        // Registering the strategies with their corresponding YAML keys
        this.registry.put("DAMAGE", new DamageStrategy());
        this.registry.put("HEAL", new HealStrategy());
    }

    /**
     * Prende la strategia corretta dal file YAML
     * Usa toUpperCase() per evitare errori di input(es. dAmage)
     *
     * @param strategyKey Il nome della strategia da AbilityConfiguration
     * @return La corrispondente AbilityStrategy o null se non trova corrispondenza
     */
    public AbilityStrategy getStrategy(String strategyKey) {
        if (strategyKey == null || strategyKey.trim().isEmpty()) {
            System.err.println("Error: Strategy key is null or empty!");
            return null;
        }

        return registry.get(strategyKey.toUpperCase());
    }
}
