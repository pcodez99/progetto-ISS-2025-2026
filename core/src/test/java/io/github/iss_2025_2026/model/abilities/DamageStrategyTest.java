package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.abilities.strategies.DamageStrategy;
// Assicurati che l'import del Player coincida con il tuo package reale
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.Character;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class DamageStrategyTest {

    @Test
    public void testDamageCalculation() {
        // 1. ARRANGE (Preparazione)
        // Creiamo il caster e il target come "manichini" (classi anonime)
        Character caster = new Player("Mago", 100, 0, null) {};
        Character target = new Player("Orco", 100, 0, null) {}; // L'orco parte con 100 HP

        AbilityConfiguration configDanno = new AbilityConfiguration();
        configDanno.setBaseDamage(30);
        configDanno.setDamageForLevel(5);
        // Nota: se il Player appena creato parte dal livello 1 di default,
        // il danno totale atteso è 30 + (5 * 1) = 35.

        // Ricorda di usare Arrays.asList per la compatibilità con Java 8!
        AbilityContext context = new AbilityContext(caster, Arrays.asList(target));
        DamageStrategy attacco = new DamageStrategy();

        // 2. ACT (Esecuzione)
        attacco.execute(context, configDanno);

        // 3. ASSERT (Verifica)
        // 100 HP iniziali - 35 danni = 65 HP rimanenti
        assertEquals(65, target.getHp(), "Il bersaglio dovrebbe aver subito 35 danni (30 base + 5 di scaling)");
    }

    @Test
    public void testDamageDoesNotDropHpBelowZero() {
        // 1. ARRANGE (Preparazione)
        Character caster = new Player("Cavaliere", 100, 0, null) {};
        Character target = new Player("Goblin debole", 20, 0, null) {}; // Parte solo con 20 HP

        AbilityConfiguration configDannoLetale = new AbilityConfiguration();
        configDannoLetale.setBaseDamage(1000); // Danno esagerato
        configDannoLetale.setDamageForLevel(0);

        AbilityContext context = new AbilityContext(caster, Arrays.asList(target));
        DamageStrategy attacco = new DamageStrategy();

        // 2. ACT (Esecuzione)
        attacco.execute(context, configDannoLetale);

        // 3. ASSERT (Verifica)
        assertEquals(0, target.getHp(), "Gli HP del bersaglio non devono mai scendere sotto lo 0!");
    }
}
