package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.Character;
import java.util.*;

/**
 * Racchiude tutte le info variabili di un singolo turno:
 * chi lancia l'abilità(caster)
 * chi sono i bersagli(target)
 * permette alle strategie di essere senza stato(stateless)
 */

public class AbilityContext {
    private final Character caster;
    private final List<Character> targets;

    public AbilityContext(Character caster, List<Character> targets) {
        this.caster = caster;
        this.targets = targets;
    }

    public Character getCaster() {
        return caster;
    }

    public List<Character> getTargets() {
        return targets;
    }
}
