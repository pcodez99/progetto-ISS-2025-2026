package io.github.iss_2025_2026.model;

/**
 * Snapshot serializzabile di un player per i salvataggi.
 */
public class PlayerSaveState {
    private String characterId;
    private String name;
    private int hp;
    private int maxHp;
    private int baseDamage;
    private int level;
    private int xp;
    private int xpToNext;
    private int karma;
    private Backpack backpack;
    private PlayerEvolutionState evolutionState;
    private float x;
    private float y;
    private Direction direction;
    private CharacterState state;

    public PlayerSaveState() {
        this.xp = 0;
        this.xpToNext = 100;
    }

    public PlayerSaveState(String characterId, String name, int hp, int maxHp, int baseDamage, int level, int karma,
            Backpack backpack) {
        this.characterId = characterId;
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.baseDamage = baseDamage;
        this.level = level;
        this.karma = karma;
        this.backpack = backpack;
        this.evolutionState = new PlayerEvolutionState();
        this.direction = Direction.DOWN;
        this.state = CharacterState.IDLE;
        this.xp = 0;
        this.xpToNext = Math.max(1, 100 * Math.max(1, level));
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXpToNext() {
        return xpToNext;
    }

    public void setXpToNext(int xpToNext) {
        this.xpToNext = xpToNext;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public Backpack getBackpack() {
        return backpack;
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
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Direction getDirection() {
        return direction != null ? direction : Direction.DOWN;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CharacterState getState() {
        return state != null ? state : CharacterState.IDLE;
    }

    public void setState(CharacterState state) {
        this.state = state;
    }
}
