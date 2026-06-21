package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.AlienoGuardiano;
import io.github.iss_2025_2026.model.AlienoInvasore;
import io.github.iss_2025_2026.model.AlienoSciame;
import io.github.iss_2025_2026.model.BossLivello1;
import io.github.iss_2025_2026.model.BossLivello2;
import io.github.iss_2025_2026.model.BossLivello3;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

public class EnemyBuilder {
    private static final String TAG = "EnemyBuilder";

    private String id;
    private String name = "Unknown Enemy";
    private int maxHp = 50;
    private int baseDamage = 5;
    private int xpReward = 15;
    private boolean boss;
    private String enemyClass;
    private SpecialAbility specialAbility;

    static EnemyBuilder fromConfig(Map<String, Object> data, AbilityRegistry abilityRegistry) {
        EnemyBuilder builder = new EnemyBuilder()
                .id(getString(data, "id", null))
                .name(getString(data, "name", "Unknown Enemy"))
                .maxHp(getInt(data, "maxHp", 50))
                .baseDamage(getInt(data, "baseDamage", 5))
                .xpReward(getInt(data, "xpReward", 15))
                .boss(getBoolean(data, "isBoss", false))
                .enemyClass(getString(data, "enemyClass", null));

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

    public EnemyBuilder enemyClass(String enemyClass) {
        this.enemyClass = enemyClass;
        return this;
    }

    public EnemyBuilder specialAbility(SpecialAbility specialAbility) {
        this.specialAbility = specialAbility;
        return this;
    }

    public Enemy build() {
        Enemy enemy = createEnemy();
        enemy.setSpecialAbility(specialAbility);
        return enemy;
    }

    private Enemy createEnemy() {
        if (boss) {
            if ("boss_livello_1".equals(id)) {
                return new BossLivello1(name, id, maxHp, baseDamage, xpReward);
            }
            if ("boss_livello_2".equals(id)) {
                return new BossLivello2(name, id, maxHp, baseDamage, xpReward);
            }
            if ("boss_livello_3".equals(id)) {
                return new BossLivello3(name, id, maxHp, baseDamage, xpReward);
            }
            return new Enemy(name, id, maxHp, baseDamage, xpReward, true);
        }

        if ("SCIAME".equalsIgnoreCase(enemyClass)) {
            return new AlienoSciame(name, id, maxHp, baseDamage, xpReward);
        }
        if ("INVASORE".equalsIgnoreCase(enemyClass)) {
            return new AlienoInvasore(name, id, maxHp, baseDamage, xpReward);
        }
        if ("GUARDIANO".equalsIgnoreCase(enemyClass)) {
            return new AlienoGuardiano(name, id, maxHp, baseDamage, xpReward);
        }
        return new Enemy(name, id, maxHp, baseDamage, xpReward, false);
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
