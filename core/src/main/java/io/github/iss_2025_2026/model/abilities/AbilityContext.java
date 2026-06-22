package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.Characters;
import java.util.*;

/**
 * Racchiude tutte le info variabili di un singolo turno:
 * chi lancia l'abilità(caster)
 * chi sono i bersagli(target)
 * permette alle strategie di essere senza stato(stateless)
 */

public class AbilityContext {
    private final Characters caster;
    private final List<Characters> targets;

    public AbilityContext(Characters caster, List<? extends Characters> targets) {
        this.caster = caster;
        this.targets = targets == null ? Collections.emptyList() : new ArrayList<>(targets);
    }

    public Characters getCaster() {
        return caster;
    }

    public List<Characters> getTargets() {
        return targets;
    }
}
