package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dati pronti per costruire una card della schermata di selezione personaggi.
 */
public class CharacterSelectionOption {
    private final String id;
    private final String name;
    private final String description;
    private final int maxHp;
    private final int baseDamage;
    private final int maxHpGrowth;
    private final int damageGrowth;
    private final String abilityName;
    private final String abilityDescription;
    private final List<String> traits;

    public CharacterSelectionOption(String id, String name, String description, int maxHp, int baseDamage,
            int maxHpGrowth, int damageGrowth, String abilityName, String abilityDescription, List<String> traits) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxHp = maxHp;
        this.baseDamage = baseDamage;
        this.maxHpGrowth = maxHpGrowth;
        this.damageGrowth = damageGrowth;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.traits = Collections.unmodifiableList(new ArrayList<>(traits));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getMaxHpGrowth() {
        return maxHpGrowth;
    }

    public int getDamageGrowth() {
        return damageGrowth;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public List<String> getTraits() {
        return traits;
    }
}
