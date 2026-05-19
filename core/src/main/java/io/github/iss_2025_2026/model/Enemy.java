package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.model.SpecialAbility;

/**
 * Represents an enemy in the game (can be a standard alien or a boss).
 * Extends the abstract Character class.
 */
public class Enemy extends Character {

    private String enemyId;         // L'ID dello YAML (es. "alieno_base")
    private int xpReward;          // I punti esperienza che dona al giocatore quando muore
    private boolean isBoss;        // true se è un boss (utile per cambiare musica o barra della vita)
    private SpecialAbility specialAbility; // L'abilità speciale (sarà null per gli alieni normali)

    /**
     * Constructor for Enemy.
     * Note: Enforces level = 1 as base, but can be scaled up later if needed.
     */
    public Enemy(String name, String enemyId, int maxHp, int baseDamage, int xpReward, boolean isBoss) {
        // Chiamiamo il costruttore del padre (Character) passando il livello 1 di default
        super(name, maxHp, baseDamage, 1);

        this.enemyId = enemyId;
        this.xpReward = Math.max(0, xpReward); // Sicurezza: niente XP negativi
        this.isBoss = isBoss;
        this.specialAbility = null; // Di default parte senza abilità, la assegna la Factory se c'è
    }

    /**
     * Checks if this enemy has a usable special ability.
     * @return true if the enemy has an ability assigned, false otherwise.
     */
    public boolean hasSpecialAbility() {
        return this.specialAbility != null;
    }

    // GETTERS
    public String getEnemyId() {
        return enemyId;
    }

    public int getXpReward() {
        return xpReward;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public SpecialAbility getSpecialAbility() {
        return specialAbility;
    }

    // SETTERS
    public void setSpecialAbility(SpecialAbility specialAbility) {
        this.specialAbility = specialAbility;
    }
}
