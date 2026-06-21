package io.github.iss_2025_2026.model;

public final class AlienoSciame extends Enemy {
    public AlienoSciame(String name, String enemyId, int maxHp, int baseDamage, int xpReward) {
        super(name, enemyId, maxHp, baseDamage, xpReward, false);
    }

    @Override
    protected Enemy createCopy(String name, String enemyId, int maxHp, int baseDamage, int xpReward, boolean isBoss) {
        return new AlienoSciame(name, enemyId, maxHp, baseDamage, xpReward);
    }
}
