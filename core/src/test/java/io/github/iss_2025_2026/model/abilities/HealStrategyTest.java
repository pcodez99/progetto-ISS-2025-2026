package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.abilities.strategies.HealStrategy;
import io.github.iss_2025_2026.model.Character;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HealStrategyTest {

    @Test
    public void testHealDoesNotExceedMaxHp() {
        // 1. Preparazione (Arrange)
        Character bersaglio = new Player("Alleato", 100, 0, null); // Max HP 100
        bersaglio.takeDamage(10); // Lo portiamo a 90 HP

        AbilityConfiguration configCura = new AbilityConfiguration();
        configCura.setBaseHealing(50); // Cura enorme che supererebbe il massimo

        AbilityContext context = new AbilityContext(bersaglio, java.util.Arrays.asList(bersaglio));
        HealStrategy cura = new HealStrategy();

        // 2. Esecuzione (Act)
        cura.execute(context, configCura);

        // 3. Verifica (Assert)
        assertEquals(100, bersaglio.getHp(), "Gli HP non devono superare il limite massimo!");
    }
}
