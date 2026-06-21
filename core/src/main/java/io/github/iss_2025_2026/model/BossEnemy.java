package io.github.iss_2025_2026.model;

public abstract class BossEnemy extends Enemy {
    protected BossEnemy(String name, String enemyId, int maxHp, int baseDamage, int xpReward) {
        super(name, enemyId, maxHp, baseDamage, xpReward, true);
    }
}
