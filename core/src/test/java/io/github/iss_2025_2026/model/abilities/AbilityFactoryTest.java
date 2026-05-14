package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.abilities.strategies.HealStrategy;
import io.github.iss_2025_2026.model.abilities.strategies.DamageStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per AbilityFactory, vedo se restituisce le istanze corrette o
 * le exception nel caso di strategie null
 */
public class AbilityFactoryTest {

    @Test
    public void testGetHealStrategy() {
        // Esecuzione
        AbilityStrategy strategy = AbilityFactory.getStrategy("HEAL");

        // Verifica
        assertNotNull(strategy, "La strategy non deve essere null");
        assertTrue(strategy instanceof HealStrategy, "L'ID 'HEAL' deve restituire esattamente una HealStrategy");
    }

    @Test
    public void testGetDamageStrategy() {
        // Esecuzione
        AbilityStrategy strategy = AbilityFactory.getStrategy("DAMAGE");

        // Verifica
        assertNotNull(strategy, "La strategy non deve essere null");
        assertTrue(strategy instanceof DamageStrategy, "L'ID 'DAMAGE' deve restituire esattamente una DamageStrategy");
    }

    @Test
    public void testUnknownStrategyThrowsException() {
        // Verifica che inserendo un ID inesistente il sistema lo segnali correttamente.
        // Se la tua Factory restituisce null invece di lanciare un'eccezione,
        // cancella questo blocco e scrivi: assertNull(AbilityFactory.getStrategy("PIPPO"));
        assertThrows(IllegalArgumentException.class, () -> {
            AbilityFactory.getStrategy("ABILITA_INESISTENTE");
        }, "Una stringa sconosciuta dovrebbe lanciare un'eccezione");
    }
}
