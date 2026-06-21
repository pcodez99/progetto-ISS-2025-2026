package io.github.iss_2025_2026.model;

/** Common contract for every playable character and enemy. */
public interface Characters {
    void takeDamage(int amount);

    void heal(int amount);

    void increaseBaseDamageByPercent(int percentage);

    boolean isAlive();

    String getName();

    int getHp();

    int getMaxHp();

    int getBaseDamage();

    int getLevel();

    void setName(String name);

    void setHp(int hp);

    void setLevel(int level);

    float getX();

    void setX(float x);

    float getY();

    void setY(float y);

    float getSpeed();

    void setSpeed(float speed);

    Direction getDirection();

    void setDirection(Direction direction);

    CharacterState getState();

    void setState(CharacterState state);

    float getStateTime();

    void setStateTime(float stateTime);

    void updateStateTime(float delta);

    float getAttackDuration();

    void setAttackDuration(float attackDuration);
}
