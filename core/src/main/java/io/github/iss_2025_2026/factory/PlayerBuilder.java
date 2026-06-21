package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.BambinoPlayer;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.MammaPlayer;
import io.github.iss_2025_2026.model.NonnoPlayer;
import io.github.iss_2025_2026.model.PapaPlayer;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.EnumMap;
import java.util.Map;

public class PlayerBuilder {
    private static final String TAG = "PlayerBuilder";

    private String id;
    private String name = "Unknown";
    private int maxHp = 100;
    private int baseDamage = 10;
    private int maxHpGrowth = 10;
    private int damageGrowth = 2;
    private int level = 1;
    private int karma = 0;
    private Backpack backpack = new Backpack(10);
    private SpecialAbility ability;
    private final Map<AbilitySlot, SpecialAbility> abilitiesBySlot = new EnumMap<>(AbilitySlot.class);

    static PlayerBuilder fromConfig(Map<String, Object> data, AbilityRegistry abilityRegistry) {
        PlayerBuilder builder = new PlayerBuilder()
                .id(getString(data, "id", null))
                .name(getString(data, "name", "Unknown"))
                .maxHp(getInt(data, "maxHp", 100))
                .baseDamage(getInt(data, "baseDamage", 10))
                .maxHpGrowth(getInt(data, "maxHpGrowth", 10))
                .damageGrowth(getInt(data, "damageGrowth", 2));

        String legacyAbilityId = getString(data, "abilityId", null);
        Map<String, Object> abilities = getMap(data, "abilities");
        String baseAbilityId = getString(abilities, "base", legacyAbilityId);
        String altruisticAbilityId = getString(abilities, "altruistic", null);
        String egoisticAbilityId = getString(abilities, "egoistic", null);

        builder.abilitySlot(AbilitySlot.BASE, resolveAbility(abilityRegistry, baseAbilityId, builder.id));
        builder.abilitySlot(AbilitySlot.ALTRUISTIC, resolveAbility(abilityRegistry, altruisticAbilityId, builder.id));
        builder.abilitySlot(AbilitySlot.EGOISTIC, resolveAbility(abilityRegistry, egoisticAbilityId, builder.id));
        return builder;
    }

    public PlayerBuilder id(String id) {
        this.id = id;
        return this;
    }

    public PlayerBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PlayerBuilder maxHp(int maxHp) {
        this.maxHp = maxHp;
        return this;
    }

    public PlayerBuilder baseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
        return this;
    }

    public PlayerBuilder maxHpGrowth(int maxHpGrowth) {
        this.maxHpGrowth = maxHpGrowth;
        return this;
    }

    public PlayerBuilder damageGrowth(int damageGrowth) {
        this.damageGrowth = damageGrowth;
        return this;
    }

    public PlayerBuilder level(int level) {
        this.level = level;
        return this;
    }

    public PlayerBuilder karma(int karma) {
        this.karma = karma;
        return this;
    }

    public PlayerBuilder backpack(Backpack backpack) {
        this.backpack = backpack != null ? backpack : new Backpack(10);
        return this;
    }

    public PlayerBuilder ability(SpecialAbility ability) {
        this.ability = ability;
        abilitySlot(AbilitySlot.BASE, ability);
        return this;
    }

    public PlayerBuilder abilitySlot(AbilitySlot slot, SpecialAbility ability) {
        AbilitySlot resolvedSlot = slot != null ? slot : AbilitySlot.BASE;
        if (ability == null) {
            abilitiesBySlot.remove(resolvedSlot);
            if (resolvedSlot == AbilitySlot.BASE) {
                this.ability = null;
            }
            return this;
        }
        abilitiesBySlot.put(resolvedSlot, ability);
        if (resolvedSlot == AbilitySlot.BASE) {
            this.ability = ability;
        }
        return this;
    }

    public Player build() {
        Player player = createPlayer();
        player.setAbilitiesBySlot(abilitiesBySlot);
        player.setCharacterId(id);
        player.setKarma(karma);
        player.setBackpack(backpack != null ? backpack : new Backpack(10));
        return player;
    }

    private Player createPlayer() {
        if ("nonno".equals(id)) {
            return new NonnoPlayer(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        }
        if ("papa".equals(id)) {
            return new PapaPlayer(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        }
        if ("mamma".equals(id)) {
            return new MammaPlayer(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        }
        if ("bambino".equals(id)) {
            return new BambinoPlayer(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        }
        return new Player(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
    }

    private static SpecialAbility resolveAbility(AbilityRegistry abilityRegistry, String abilityId, String characterId) {
        if (abilityId == null || abilityId.trim().isEmpty()) {
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
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }
}
