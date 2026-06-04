package io.github.iss_2025_2026.model;

/** Represents a character selectable by the player */
public class Player extends Character {
    private String characterId;
    private int karma; // from -50 to +50 (egoism and altruism)
    private Backpack backpack;
    private SpecialAbility ability;
    // Flag per indicare se il giocatore ha già usato la sua abilità speciale in questa battaglia
    private boolean specialAbilityUsedThisBattle = false;

    // Growth parameters for leveling up
    private int maxHpGrowth;
    private int damageGrowth;

    public Player(String name, int maxHp, int baseDamage, SpecialAbility ability, int maxHpGrowth, int damageGrowth,
            int level) {
        super(name, maxHp, baseDamage, level);
        this.karma = 0;
        this.backpack = new Backpack(10);
        this.ability = ability;
        this.maxHpGrowth = maxHpGrowth;
        this.damageGrowth = damageGrowth;
    }

    /** Convenience constructor with default growth values */
    public Player(String name, int maxHp, int baseDamage, SpecialAbility ability) {
        this(name, maxHp, baseDamage, ability, 10, 2, 1);
    }

    /**
     * Modifies karma safely within range
     *
     * @param variation Karma points to add/subtract
     */
    public void modifyKarma(int variation) {
        this.karma += variation;
        if (this.karma > 50) {
            this.karma = 50;
        } else if (this.karma < -50) {
            this.karma = -50;
        }
    }

    /**
     * Uses the character's special ability
     */
    public void useSpecialAbility(Character target) {
        if (this.ability != null) {
            this.ability.perform(this, target, getLevel());
        }
    }

    /**
     * Increases level and updates stats based on growth parameters
     */
    public void levelUp() {
        setLevel(getLevel() + 1);
        setMaxHp(getMaxHp() + maxHpGrowth);
        setBaseDamage(getBaseDamage() + damageGrowth);
        setHp(getMaxHp()); // Heal on level up
    }

    // GETTERS
    public int getKarma() {
        return karma;
    }

    public String getCharacterId() {
        return characterId;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public SpecialAbility getAbility() {
        return ability;
    }

    public boolean hasUsedSpecialAbilityThisBattle() {
        return specialAbilityUsedThisBattle;
    }

    /**
     * Marca che il giocatore ha usato la special in questa battaglia.
     */
    public void markSpecialAbilityUsed() {
        this.specialAbilityUsedThisBattle = true;
    }

    /**
     * Reset dello stato a inizio/nuova battaglia.
     */
    public void resetSpecialAbilityUsageForBattle() {
        this.specialAbilityUsedThisBattle = false;
    }

    public int getMaxHpGrowth() {
        return maxHpGrowth;
    }

    public int getDamageGrowth() {
        return damageGrowth;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
    }

    public void restoreState(String name, int hp, int maxHp, int baseDamage, int level, int karma, Backpack backpack) {
        if (name != null && !name.trim().isEmpty()) {
            setName(name);
        }
        setLevel(Math.max(1, level));
        setMaxHp(Math.max(1, maxHp));
        setBaseDamage(Math.max(1, baseDamage));
        setHp(hp);
        setKarma(karma);
        setBackpack(backpack != null ? backpack : new Backpack(10));
    }

}
