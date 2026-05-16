package io.github.iss_2025_2026.model.abilities;

/**
 * Il contenitore dei dati statici che vengono caricate dal file .yaml
 * contiene i dati per far si che la strategia esegua i calcoli corretti per danni o cura
 */

public class AbilityConfiguration {
    private String id;
    private String name;
    private String strategy;
    private int cooldown;

    private boolean aoe; // true = Area of Effect (all), false = Single target(chosen by player)

    private int baseDamage;
    private int damageForLevel;

    private int baseHealing;
    private int healingForLevel;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public boolean isAoe() { return aoe; }
    public void setAoe(boolean aoe) { this.aoe = aoe; }

    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }

    public int getDamageForLevel() { return damageForLevel; }
    public void setDamageForLevel(int damageForLevel) { this.damageForLevel = damageForLevel; }

    public int getBaseHealing() { return baseHealing; }
    public void setBaseHealing(int baseHealing) { this.baseHealing = baseHealing; }

    public int getHealingForLevel() { return healingForLevel; }
    public void setHealingForLevel(int healingForLevel) { this.healingForLevel = healingForLevel; }
}
