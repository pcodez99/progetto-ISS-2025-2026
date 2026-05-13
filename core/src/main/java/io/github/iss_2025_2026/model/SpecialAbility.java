package io.github.iss_2025_2026.model;

/**
 * Interface defining the behavior of a special move
 */
public interface SpecialAbility {
    String getName();
    String getDescription();
    /** Executes the ability.
     * @param user The character using the ability
     * @param target The target character
     * @param userLevel The level of the user to calculate effects
     */
    void perform(Character user, Character target, int userLevel);
}
