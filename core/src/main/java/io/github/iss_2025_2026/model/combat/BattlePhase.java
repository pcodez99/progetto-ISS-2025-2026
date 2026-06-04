package io.github.iss_2025_2026.model.combat;

/**
 * Rappresenta le fasi del combattimento a turni.
 * Il turno del giocatore (o dei giocatori in multiplayer) precede sempre il turno del nemico.
 */
public enum BattlePhase {
    /** PlayerOne sceglie l'azione (sempre presente) */
    PLAYER_ONE_TURN,
    /** PlayerTwo sceglie l'azione (solo modalità multiplayer) */
    PLAYER_TWO_TURN,
    /** I nemici eseguono le loro azioni, attaccando un player casuale tra quelli vivi */
    ENEMY_TURN,
    /** Tutti i nemici sono stati sconfitti */
    VICTORY,
    /** Entrambi i giocatori sono a 0 HP */
    DEFEAT,
    /** I giocatori sono fuggiti entro il tempo limite */
    FLED
}
