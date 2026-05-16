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
        Character caster = new Player("Mago", 100, 0, null);
        Character target = new Player("Orco", 100, 0, null); // L'orco parte con 100 HP

        AbilityConfiguration configDanno = new AbilityConfiguration();
        configDanno.setBaseDamage(30);
        // Scaling logic: baseDamage + caster.getLevel()
        // Level default is 1, so damage = 30 + 1 = 31

        AbilityContext context = new AbilityContext(caster, Arrays.asList(target));
        DamageStrategy attacco = new DamageStrategy();

        // 2. ACT (Esecuzione)
        attacco.execute(context, configDanno);

        // 3. ASSERT (Verifica)
        // 100 HP iniziali - 31 danni = 69 HP rimanenti
        assertEquals(69, target.getHp(), "Il bersaglio dovrebbe aver subito 31 danni (30 base + 1 di scaling del livello 1)");
    }

    @Test
    public void testDamageDoesNotDropHpBelowZero() {
        // 1. ARRANGE (Preparazione)
        Character caster = new Player("Cavaliere", 100, 0, null);
        Character target = new Player("Goblin debole", 20, 0, null); // Parte solo con 20 HP

        AbilityConfiguration configDannoLetale = new AbilityConfiguration();
        configDannoLetale.setBaseDamage(1000); // Danno esagerato

        AbilityContext context = new AbilityContext(caster, Arrays.asList(target));
        DamageStrategy attacco = new DamageStrategy();

        // 2. ACT (Esecuzione)
        attacco.execute(context, configDannoLetale);

        // 3. ASSERT (Verifica)
        assertEquals(0, target.getHp(), "Gli HP del bersaglio non devono mai scendere sotto lo 0!");
    }
}
