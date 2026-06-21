package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

/** Ordina i passi con cui i builder traducono una configurazione YAML in un Character. */
final class CharacterBuildDirector {
    private static final String TAG = "CharacterBuildDirector";

    Player constructPlayer(PlayerBuilder builder, Map<String, Object> data, AbilityRegistry abilityRegistry) {
        configureCommon(builder, data, "Unknown", 100, 10);
        builder.maxHpGrowth(getInt(data, "maxHpGrowth", 10))
                .damageGrowth(getInt(data, "damageGrowth", 2));

        String characterId = getString(data, "id", null);
        String legacyAbilityId = getString(data, "abilityId", null);
        Map<String, Object> abilities = getMap(data, "abilities");
        builder.abilitySlot(AbilitySlot.BASE,
                resolveAbility(abilityRegistry, getString(abilities, "base", legacyAbilityId), characterId, false));
        builder.abilitySlot(AbilitySlot.ALTRUISTIC,
                resolveAbility(abilityRegistry, getString(abilities, "altruistic", null), characterId, false));
        builder.abilitySlot(AbilitySlot.EGOISTIC,
                resolveAbility(abilityRegistry, getString(abilities, "egoistic", null), characterId, false));
        return builder.build();
    }

    Enemy constructEnemy(EnemyBuilder builder, Map<String, Object> data, AbilityRegistry abilityRegistry) {
        configureCommon(builder, data, "Unknown Enemy", 50, 5);
        builder.xpReward(getInt(data, "xpReward", 15))
                .boss(getBoolean(data, "isBoss", false));

        String enemyId = getString(data, "id", null);
        String abilityId = getString(data, "abilityId", null);
        builder.specialAbility(resolveAbility(abilityRegistry, abilityId, enemyId, true));
        return builder.build();
    }

    private void configureCommon(CharacterBuilder builder, Map<String, Object> data, String defaultName,
            int defaultMaxHp, int defaultBaseDamage) {
        builder.reset()
                .id(getString(data, "id", null))
                .name(getString(data, "name", defaultName))
                .maxHp(getInt(data, "maxHp", defaultMaxHp))
                .baseDamage(getInt(data, "baseDamage", defaultBaseDamage));
    }

    private SpecialAbility resolveAbility(AbilityRegistry abilityRegistry, String abilityId, String characterId,
            boolean allowNone) {
        if (abilityId == null || abilityId.trim().isEmpty() || (allowNone && "NONE".equalsIgnoreCase(abilityId))) {
            return null;
        }
        SpecialAbility ability = abilityRegistry.get(abilityId);
        if (ability == null) {
            Gdx.app.error(TAG, "WARNING: Ability '" + abilityId + "' not found for character '"
                    + characterId + "'. Slot will be empty.");
        }
        return ability;
    }

    private static String getString(Map<String, Object> data, String key, String fallback) {
        if (data == null) {
            return fallback;
        }
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private static int getInt(Map<String, Object> data, String key, int fallback) {
        if (data == null) {
            return fallback;
        }
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private static boolean getBoolean(Map<String, Object> data, String key, boolean fallback) {
        if (data == null) {
            return fallback;
        }
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMap(Map<String, Object> data, String key) {
        if (data == null) {
            return null;
        }
        Object value = data.get(key);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }
}
