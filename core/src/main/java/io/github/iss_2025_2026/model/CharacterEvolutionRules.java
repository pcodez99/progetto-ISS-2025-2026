package io.github.iss_2025_2026.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CharacterEvolutionRules {
    private Map<String, String> abilities = new HashMap<>();
    private Map<String, EvolutionUnlockRule> unlockRules = new HashMap<>();

    public String getAbilityId(AbilitySlot slot) {
        if (slot == null) {
            return null;
        }
        return abilities.get(toConfigKey(slot));
    }

    public EvolutionUnlockRule getUnlockRule(AbilitySlot slot) {
        if (slot == null) {
            return null;
        }
        return unlockRules.get(toConfigKey(slot));
    }

    public Map<String, String> getAbilities() {
        return Collections.unmodifiableMap(abilities);
    }

    public void setAbilities(Map<String, String> abilities) {
        this.abilities = abilities != null ? new HashMap<>(abilities) : new HashMap<String, String>();
    }

    public Map<String, EvolutionUnlockRule> getUnlockRules() {
        return Collections.unmodifiableMap(unlockRules);
    }

    public void setUnlockRules(Map<String, EvolutionUnlockRule> unlockRules) {
        this.unlockRules = unlockRules != null
                ? new HashMap<>(unlockRules)
                : new HashMap<String, EvolutionUnlockRule>();
    }

    private String toConfigKey(AbilitySlot slot) {
        if (slot == AbilitySlot.ALTRUISTIC) {
            return "altruistic";
        }
        if (slot == AbilitySlot.EGOISTIC) {
            return "egoistic";
        }
        return "base";
    }
}
