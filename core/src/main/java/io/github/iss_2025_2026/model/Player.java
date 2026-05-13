package io.github.iss_2025_2026.model;

/** Represents a character selectable by the player */
public class Player extends Character {
    private int level;
    private int karma; // from -50 to +50 (egoism and altruism)
    private Backpack backpack;
    private SpecialAbility ability;
    
    // Growth parameters for leveling up
    private int maxHpGrowth;
    private int damageGrowth;

    public Player(String name, int maxHp, int baseDamage, SpecialAbility ability, int maxHpGrowth, int damageGrowth) {
        super(name, maxHp, baseDamage);
        this.level = 1;
        this.karma = 0;
        this.backpack = new Backpack(10);
        this.ability = ability;
        this.maxHpGrowth = maxHpGrowth;
        this.damageGrowth = damageGrowth;
    }

    /** Modifies karma safely within range
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

    /** Uses the character's special ability
     */
    public void useSpecialAbility(Character target) {
        if (this.ability != null) {
            this.ability.perform(this, target, this.level);
        }
    }

    /**
     * Increases level and updates stats based on growth parameters
     */
    public void levelUp() {
        this.level++;
        setMaxHp(getMaxHp() + maxHpGrowth);
        setBaseDamage(getBaseDamage() + damageGrowth);
        setHp(getMaxHp()); // Heal on level up
    }

    // GETTERS
    public int getLevel() {
        return level;
    }

    public int getKarma() {
        return karma;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public SpecialAbility getAbility() {
        return ability;
    }
}
