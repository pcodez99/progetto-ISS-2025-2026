package io.github.iss_2025_2026.model.abilities;

import io.github.iss_2025_2026.model.Character;
import java.util.*;

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
