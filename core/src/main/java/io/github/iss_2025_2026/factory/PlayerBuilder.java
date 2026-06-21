package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.EnumMap;
import java.util.Map;

public final class PlayerBuilder implements CharacterBuilder {
    private String id;
    private String name;
    private int maxHp;
    private int baseDamage;
    private int maxHpGrowth;
    private int damageGrowth;
    private int level;
    private int karma;
    private Backpack backpack;
    private SpecialAbility ability;
    private final Map<AbilitySlot, SpecialAbility> abilitiesBySlot = new EnumMap<>(AbilitySlot.class);

    public PlayerBuilder() {
        reset();
    }

    @Override
    public PlayerBuilder reset() {
        id = null;
        name = "Unknown";
        maxHp = 100;
        baseDamage = 10;
        maxHpGrowth = 10;
        damageGrowth = 2;
        level = 1;
        karma = 0;
        backpack = new Backpack(10);
        ability = null;
        abilitiesBySlot.clear();
        return this;
    }

    @Override
    public PlayerBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public PlayerBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PlayerBuilder maxHp(int maxHp) {
        this.maxHp = maxHp;
        return this;
    }

    @Override
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

    @Override
    public Player build() {
        Player player = new Player(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, level);
        player.setAbilitiesBySlot(abilitiesBySlot);
        player.setCharacterId(id);
        player.setKarma(karma);
        player.setBackpack(backpack != null ? backpack : new Backpack(10));
        reset();
        return player;
    }
}
