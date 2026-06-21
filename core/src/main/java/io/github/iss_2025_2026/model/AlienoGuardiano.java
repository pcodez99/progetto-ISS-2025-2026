package io.github.iss_2025_2026.model;

public final class AlienoGuardiano extends Enemy {
    public AlienoGuardiano(String name, String enemyId, int maxHp, int baseDamage, int xpReward) {
        super(name, enemyId, maxHp, baseDamage, xpReward, false);
    }

    @Override
    protected Enemy createCopy(String name, String enemyId, int maxHp, int baseDamage, int xpReward, boolean isBoss) {
        return new AlienoGuardiano(name, enemyId, maxHp, baseDamage, xpReward);
    }
}
