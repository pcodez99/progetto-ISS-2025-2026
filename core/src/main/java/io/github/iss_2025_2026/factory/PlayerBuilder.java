package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.Map;

public class PlayerBuilder {
    private static final String TAG = "CharacterFactory";

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

    static PlayerBuilder fromConfig(Map<String, Object> data, AbilityRegistry abilityRegistry) {
        PlayerBuilder builder = new PlayerBuilder()
                .id(getString(data, "id", null))
                .name(getString(data, "name", "Unknown"))
                .maxHp(getInt(data, "maxHp", 100))
                .baseDamage(getInt(data, "baseDamage", 10))
                .maxHpGrowth(getInt(data, "maxHpGrowth", 10))
                .damageGrowth(getInt(data, "damageGrowth", 2));

        String abilityId = getString(data, "abilityId", null);
        SpecialAbility ability = abilityRegistry.get(abilityId);
        if (ability == null && abilityId != null) {
            Gdx.app.error(TAG, "WARNING: Ability '" + abilityId + "' not found for character '"
                    + builder.id + "'. Player will have NO special ability!");
        }
        return builder.ability(ability);
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
        return this;
    }

    public Player build() {
        Player player = new Player(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        player.setCharacterId(id);
        player.setKarma(karma);
        player.setBackpack(backpack != null ? backpack : new Backpack(10));
        return player;
    }

    private static String getString(Map<String, Object> data, String key, String fallback) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private static int getInt(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }
}
