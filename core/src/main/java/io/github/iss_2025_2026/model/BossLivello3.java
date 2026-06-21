package io.github.iss_2025_2026.model;

public final class BossLivello3 extends BossEnemy {
    public BossLivello3(String name, String enemyId, int maxHp, int baseDamage, int xpReward) {
        super(name, enemyId, maxHp, baseDamage, xpReward);
    }

    @Override
    protected Enemy createCopy(String name, String enemyId, int maxHp, int baseDamage, int xpReward, boolean isBoss) {
        return new BossLivello3(name, enemyId, maxHp, baseDamage, xpReward);
    }
}
