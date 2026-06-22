package io.github.iss_2025_2026.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Base class for characters selectable by the player. */
public class Player extends Character {
    private String characterId;
    private int karma; // from -50 to +50 (egoism and altruism)
    private Backpack backpack;
    private SpecialAbility ability;
    private final Map<AbilitySlot, SpecialAbility> abilitiesBySlot = new EnumMap<>(AbilitySlot.class);
    private PlayerEvolutionState evolutionState;

    // Growth parameters for leveling up
    private int maxHpGrowth;
    private int damageGrowth;

    // Experience / leveling progression
    private int xp = 0;
    private int xpToNext = 100; // default required XP for next level

    // Flag per-player: traccia se l'abilità speciale è stata già usata in questa battaglia
    private boolean specialAbilityUsedThisBattle = false;

    public Player(String name, int maxHp, int baseDamage, SpecialAbility ability, int maxHpGrowth, int damageGrowth,
            int level) {
        super(name, maxHp, baseDamage, level);
        this.karma = 0;
        this.backpack = new Backpack(10);
        this.ability = ability;
        this.evolutionState = new PlayerEvolutionState();
        setAbilitySlot(AbilitySlot.BASE, ability);
        this.maxHpGrowth = maxHpGrowth;
        this.damageGrowth = damageGrowth;
        this.xp = 0;
        this.xpToNext = calculateXpForLevel(level);
    }

    /** Convenience constructor with default growth values */
    public Player(String name, int maxHp, int baseDamage, SpecialAbility ability) {
        this(name, maxHp, baseDamage, ability, 10, 2, 1);
    }

    /**
     * Modifies karma safely within range
     *
     * @param variation Karma points to add/subtract
     */
    public void modifyKarma(int variation) {
        this.karma += variation;
        if (this.karma > 50) {
            this.karma = 50;
        } else if (this.karma < -50) {
            this.karma = -50;
        }
    }

    /**
     * Uses the character's special ability
     */
    public void useSpecialAbility(Characters target) {
        SpecialAbility selectedAbility = getAbility();
        if (selectedAbility != null) {
            selectedAbility.perform(this, target, getLevel());
        }
    }

    /**
     * Controlla se il giocatore ha già usato l'abilità speciale in questa battaglia
     */
    public boolean hasUsedSpecialAbilityThisBattle() {
        return specialAbilityUsedThisBattle;
    }

    /**
     * Marca l'abilità speciale come usata in questa battaglia
     */
    public void markSpecialAbilityUsed() {
        this.specialAbilityUsedThisBattle = true;
    }

    /**
     * Resetta l'uso dell'abilità speciale all'inizio di una nuova battaglia
     */
    public void resetSpecialAbilityUsageForBattle() {
        this.specialAbilityUsedThisBattle = false;
    }

    /**
     * Increases level and updates stats based on growth parameters
     */
    public void levelUp() {
        setLevel(getLevel() + 1);
        setMaxHp(getMaxHp() + maxHpGrowth);
        setBaseDamage(getBaseDamage() + damageGrowth);
        setHp(getMaxHp()); // Heal on level up
        // Recalculate XP threshold for the new level
        this.xpToNext = calculateXpForLevel(getLevel());
    }

    /**
     * Adds experience and handles level-ups when thresholds are reached.
     * Any overflow XP carries over to the next level.
     *
     * @param amount XP to add (must be >= 0)
     */
    public void addXp(int amount) {
        if (amount <= 0) {
            return;
        }
        this.xp += amount;
        // Handle multiple level-ups if XP is large
        while (this.xp >= this.xpToNext) {
            this.xp -= this.xpToNext;
            levelUp();
        }
    }

    public int getXp() {
        return xp;
    }

    public int getXpToNext() {
        return xpToNext;
    }

    public float getXpPercent() {
        if (xpToNext <= 0) return 0f;
        return Math.max(0f, Math.min(1f, (float) xp / (float) xpToNext));
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public void setXpToNext(int xpToNext) {
        this.xpToNext = Math.max(1, xpToNext);
    }

    private int calculateXpForLevel(int level) {
        // Simple formula: base 100 * level (can be tuned later)
        return Math.max(1, 100 * Math.max(1, level));
    }

    // GETTERS
    public int getKarma() {
        return karma;
    }

    public String getCharacterId() {
        return characterId;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public SpecialAbility getAbility() {
        AbilitySlot selectedSlot = getSelectedAbilitySlot();
        SpecialAbility selectedAbility = getAbility(selectedSlot);
        if (selectedAbility != null && isAbilitySlotUnlocked(selectedSlot)) {
            return selectedAbility;
        }
        return getAbility(AbilitySlot.BASE);
    }

    public SpecialAbility getAbility(AbilitySlot slot) {
        AbilitySlot resolvedSlot = slot != null ? slot : AbilitySlot.BASE;
        SpecialAbility slotAbility = abilitiesBySlot.get(resolvedSlot);
        if (slotAbility != null) {
            return slotAbility;
        }
        return resolvedSlot == AbilitySlot.BASE ? ability : null;
    }

    public Map<AbilitySlot, SpecialAbility> getAbilitiesBySlot() {
        return Collections.unmodifiableMap(abilitiesBySlot);
    }

    public AbilitySlot getSelectedAbilitySlot() {
        return getEvolutionState().getSelectedAbilitySlot();
    }

    public boolean selectAbilitySlot(AbilitySlot slot) {
        if (slot == null || !isAbilitySlotUnlocked(slot) || getAbility(slot) == null) {
            return false;
        }
        getEvolutionState().setSelectedAbilitySlot(slot);
        return true;
    }

    public boolean isAbilitySlotUnlocked(AbilitySlot slot) {
        return getEvolutionState().isAbilitySlotUnlocked(slot);
    }

    public void unlockAbilitySlot(AbilitySlot slot) {
        getEvolutionState().unlockAbilitySlot(slot);
    }

    public void setAbilitySlot(AbilitySlot slot, SpecialAbility ability) {
        AbilitySlot resolvedSlot = slot != null ? slot : AbilitySlot.BASE;
        if (ability == null) {
            abilitiesBySlot.remove(resolvedSlot);
            if (resolvedSlot == AbilitySlot.BASE) {
                this.ability = null;
            }
            return;
        }
        abilitiesBySlot.put(resolvedSlot, ability);
        if (resolvedSlot == AbilitySlot.BASE) {
            this.ability = ability;
            unlockAbilitySlot(AbilitySlot.BASE);
        }
    }

    public void setAbilitiesBySlot(Map<AbilitySlot, SpecialAbility> abilities) {
        abilitiesBySlot.clear();
        if (abilities != null) {
            for (Map.Entry<AbilitySlot, SpecialAbility> entry : abilities.entrySet()) {
                setAbilitySlot(entry.getKey(), entry.getValue());
            }
        }
        if (!abilitiesBySlot.containsKey(AbilitySlot.BASE) && ability != null) {
            setAbilitySlot(AbilitySlot.BASE, ability);
        }
    }

    public int getMaxHpGrowth() {
        return maxHpGrowth;
    }

    public int getDamageGrowth() {
        return damageGrowth;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
    }

    public PlayerEvolutionState getEvolutionState() {
        if (evolutionState == null) {
            evolutionState = new PlayerEvolutionState();
        }
        return evolutionState;
    }

    public void setEvolutionState(PlayerEvolutionState evolutionState) {
        this.evolutionState = evolutionState != null ? evolutionState : new PlayerEvolutionState();
        this.evolutionState.unlockAbilitySlot(AbilitySlot.BASE);
        if (getAbility(this.evolutionState.getSelectedAbilitySlot()) == null
                || !this.evolutionState.isAbilitySlotUnlocked(this.evolutionState.getSelectedAbilitySlot())) {
            this.evolutionState.setSelectedAbilitySlot(AbilitySlot.BASE);
        }
    }

    public void restoreState(String name, int hp, int maxHp, int baseDamage, int level, int karma, Backpack backpack) {
        if (name != null && !name.trim().isEmpty()) {
            setName(name);
        }
        setLevel(Math.max(1, level));
        setMaxHp(Math.max(1, maxHp));
        setBaseDamage(Math.max(1, baseDamage));
        setHp(hp);
        setKarma(karma);
        setBackpack(backpack != null ? backpack : new Backpack(10));
    }

}
