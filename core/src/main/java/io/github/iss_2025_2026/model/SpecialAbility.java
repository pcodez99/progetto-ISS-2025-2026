package io.github.iss_2025_2026.model;

/**
 * Interfaccia che definisce il comportamento di una mossa speciale
 */
public interface SpecialAbility {
    String getName();
    String getDescription();
    /** Esegue l'abilità. Il parametro livello permette di calcolare effetti
     * in base al livello dell'user
     * @param userLevel
     */
    void use(Character user, Character target, int userLevel);
}
