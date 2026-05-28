package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.service.GameProperties;

/** Represents a generic character in the game (Hero, Enemy, or Boss) */
public abstract class Character {
    private String name;
    private int hp;
    private int maxHp;
    private int baseDamage;
    protected int level = 1;
    private float x;
    private float y;
    private float speed = GameProperties.getFloat(GameProperties.KEY_DEFAULT_PLAYER_SPEED, 200f);
    private Direction direction = Direction.DOWN;
    private CharacterState state = CharacterState.IDLE;
    private float stateTime = 0f;
    private float attackDuration = 0.32f;

    /** Constructor with safety checks for hp (min 1) and baseDamage (min 1) */
    public Character(String name, int maxHp, int baseDamage, int level) {
        this.name = name;
        this.maxHp = Math.max(1, maxHp);
        this.hp = this.maxHp;
        this.baseDamage = Math.max(1, baseDamage);
        this.level = level;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Safe method to receive damage (prevents HP from dropping below zero)
     * 
     * @param amount The amount of damage to take
     */
    public void takeDamage(int amount) {
        if (amount > 0) {
            this.hp -= amount;
            if (this.hp < 0) {
                this.hp = 0;
            }
        }
    }

    /**
     * Method to heal the character
     * 
     * @param amount The amount of HP to restore
     */
    public void heal(int amount) {
        if (amount > 0) {
            this.hp += amount;
            if (this.hp > maxHp) {
                this.hp = maxHp;
            }
        }
    }

    public void increaseBaseDamageByPercent(int percentage) {
        if (percentage > 0) {
            int increase = Math.max(1, Math.round(baseDamage * percentage / 100f));
            this.baseDamage += increase;
        }
    }

    /** Method to check if a character is still alive */
    public boolean isAlive() {
        return this.hp > 0;
    }

    // GETTERS
    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getLevel() {
        return level;
    }

    // SETTERS
    public void setName(String name) {
        this.name = name;
    }

    public void setHp(int hp) {
        this.hp = Math.min(maxHp, Math.max(0, hp));
    }

    public void setLevel(int level) {
        this.level = level;
    }

    protected void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    protected void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CharacterState getState() {
        return state;
    }

    public void setState(CharacterState state) {
        if (this.state != state) {
            this.state = state;
            this.stateTime = 0f;
        }
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public void updateStateTime(float delta) {
        this.stateTime += delta;
    }

    public float getAttackDuration() {
        return attackDuration;
    }

    public void setAttackDuration(float attackDuration) {
        this.attackDuration = attackDuration;
    }

    protected void copyRuntimeStateTo(Character copy) {
        copy.setHp(getHp());
        copy.setLevel(getLevel());
        copy.setX(getX());
        copy.setY(getY());
        copy.setSpeed(getSpeed());
        copy.setDirection(getDirection());
        copy.setState(getState());
        copy.setStateTime(getStateTime());
        copy.setAttackDuration(getAttackDuration());
    }
}
