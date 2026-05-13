package io.github.iss_2025_2026.model;

/** Represents a generic character in the game (Hero, Enemy, or Boss) */
public abstract class Character {
    private String name;
    private int hp;
    private int maxHp;
    private int baseDamage;

    /** Constructor with safety checks for hp (min 1) and baseDamage (min 1) */
    public Character(String name, int maxHp, int baseDamage) {
        this.name = name;
        this.maxHp = Math.max(1, maxHp);
        this.hp = this.maxHp;
        this.baseDamage = Math.max(1, baseDamage);
    }

    /** Safe method to receive damage (prevents HP from dropping below zero)
     * @param amount The amount of damage to take
     */
    public void takeDamage(int amount) {
        if (amount > 0) {
            this.hp -= amount;
            if (this.hp < 0) {
                this.hp = 0;
            }
        }
    }

    /** Method to check if a character is still alive */
    public boolean isAlive() {
        return this.hp > 0;
    }

    // GETTERS
    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public void setHp(int hp) {
        this.hp = Math.min(maxHp, Math.max(0, hp));
    }

    protected void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    protected void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }
}
