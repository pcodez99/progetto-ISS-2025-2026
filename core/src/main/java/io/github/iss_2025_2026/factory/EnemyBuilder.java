package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

public class EnemyBuilder {
    private static final String TAG = "CharacterFactory";

    private String id;
    private String name = "Unknown Enemy";
    private int maxHp = 50;
    private int baseDamage = 5;
    private int xpReward = 15;
    private boolean boss;
    private SpecialAbility specialAbility;

    static EnemyBuilder fromConfig(Map<String, Object> data, AbilityRegistry abilityRegistry) {
        EnemyBuilder builder = new EnemyBuilder()
                .id(getString(data, "id", null))
                .name(getString(data, "name", "Unknown Enemy"))
                .maxHp(getInt(data, "maxHp", 50))
                .baseDamage(getInt(data, "baseDamage", 5))
                .xpReward(getInt(data, "xpReward", 15))
                .boss(getBoolean(data, "isBoss", false));

        String abilityId = getString(data, "abilityId", null);
        if (abilityId != null && !"NONE".equalsIgnoreCase(abilityId)) {
            SpecialAbility ability = abilityRegistry.get(abilityId);
            if (ability == null) {
                Gdx.app.error(TAG, "WARNING: Ability '" + abilityId + "' not found for enemy '" + builder.id + "'");
            }
            builder.specialAbility(ability);
        }
        return builder;
    }

    public EnemyBuilder id(String id) {
        this.id = id;
        return this;
    }

    public EnemyBuilder name(String name) {
        this.name = name;
        return this;
    }

    public EnemyBuilder maxHp(int maxHp) {
        this.maxHp = maxHp;
        return this;
    }

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

    public Enemy build() {
        Enemy enemy = new Enemy(name, id, maxHp, baseDamage, xpReward, boss);
        enemy.setSpecialAbility(specialAbility);
        return enemy;
    }

    private static String getString(Map<String, Object> data, String key, String fallback) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private static int getInt(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private static boolean getBoolean(Map<String, Object> data, String key, boolean fallback) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }
}
