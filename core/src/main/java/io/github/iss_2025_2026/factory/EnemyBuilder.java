package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.SpecialAbility;

public final class EnemyBuilder implements CharacterBuilder {
    private String id;
    private String name;
    private int maxHp;
    private int baseDamage;
    private int xpReward;
    private boolean boss;
    private SpecialAbility specialAbility;

    public EnemyBuilder() {
        reset();
    }

    @Override
    public EnemyBuilder reset() {
        id = null;
        name = "Unknown Enemy";
        maxHp = 50;
        baseDamage = 5;
        xpReward = 15;
        boss = false;
        specialAbility = null;
        return this;
    }

    @Override
    public EnemyBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public EnemyBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public EnemyBuilder maxHp(int maxHp) {
        this.maxHp = maxHp;
        return this;
    }

    @Override
    public EnemyBuilder baseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
        return this;
    }

    public EnemyBuilder xpReward(int xpReward) {
        this.xpReward = xpReward;
        return this;
    }

    public EnemyBuilder boss(boolean boss) {
        this.boss = boss;
        return this;
    }

    public EnemyBuilder specialAbility(SpecialAbility specialAbility) {
        this.specialAbility = specialAbility;
        return this;
    }

    @Override
    public Enemy build() {
        Enemy enemy = new Enemy(name, id, maxHp, baseDamage, xpReward, boss);
        enemy.setSpecialAbility(specialAbility);
        reset();
        return enemy;
    }
}
