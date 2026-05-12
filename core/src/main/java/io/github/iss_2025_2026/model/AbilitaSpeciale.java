package io.github.iss_2025_2026.model;

/**
 * Interfaccia che definisce il comportamento di una mossa speciale
 */
public interface AbilitaSpeciale {
    String getNome();
    String getDescrizione();
    /** Esegue l'abilità. Il parametro livello permette di calcolare effetti
     * in base al livello dell'utilizzatore
     * @param livelloUtilizzatore
     */
    void esegui(Personaggio utilizzatore,Personaggio bersaglio,int livelloUtilizzatore);
}
