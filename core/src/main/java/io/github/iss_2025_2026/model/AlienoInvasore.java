package io.github.iss_2025_2026.model;

public final class AlienoInvasore extends Enemy {
    public AlienoInvasore(String name, String enemyId, int maxHp, int baseDamage, int xpReward) {
        super(name, enemyId, maxHp, baseDamage, xpReward, false);
    }

    @Override
    protected Enemy createCopy(String name, String enemyId, int maxHp, int baseDamage, int xpReward, boolean isBoss) {
        return new AlienoInvasore(name, enemyId, maxHp, baseDamage, xpReward);
    }
}
