package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.abilities.strategies.DamageStrategy;
import io.github.iss_2025_2026.model.abilities.strategies.HealStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * distributore delle logiche,serve per separare le strategie e il loro utilizzo;
 * utilizza un registro(Map) statico di utte le possibili strategie(prendendole da YAML)
 */
public class AbilityFactory{
private static final Map<String,AbilityStrategy> registry=new HashMap<>();
      static{
        registry.put("DAMAGE", new DamageStrategy());
        registry.put("HEAL", new HealStrategy());
    }

    /**
     * Prende la strategia corretta dal file YAML
     * Usa toUpperCase() per evitare errori di input(es. dAmage)
     *
     * @param strategyKey Il nome della strategia da AbilityConfiguration
     * @return La corrispondente AbilityStrategy o null se non trova corrispondenza
     */
    public static AbilityStrategy getStrategy(String strategyKey) {
        if (strategyKey == null || strategyKey.trim().isEmpty()) {
        throw new IllegalArgumentException("La chiave della strategia non può essere nulla o vuota!");
        }
        //recupero abilità dal registro
        AbilityStrategy strategy=registry.get(strategyKey.toUpperCase());

        //se la strategia non esiste lancio l'eccezione
        if(strategy==null){
            throw new IllegalArgumentException("Nessuna strategia trovata per la chiave: "+strategyKey);
        }

        return strategy;
    }
}
