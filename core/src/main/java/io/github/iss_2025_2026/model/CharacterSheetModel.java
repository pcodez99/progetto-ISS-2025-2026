package io.github.iss_2025_2026.model;

import java.util.List;

/**
 * Model della schermata personaggio.
 * Aggrega i dati visualizzabili di un Player (HP, statistiche, inventario)
 * senza dipendenze da LibGDX — completamente testabile con JUnit puro.
 *
 * Legge i dati direttamente dal riferimento live al Player, quindi riflette
 * sempre lo stato corrente senza necessità di refresh manuale.
 */
public class CharacterSheetModel {

    private final Player player;

    /**
     * @param player Il giocatore di cui mostrare i dati. Non deve essere null.
     */
    public CharacterSheetModel(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Il player non puo essere null.");
        }
        this.player = player;
    }

    // -------------------------------------------------------------------------
    // Identità
    // -------------------------------------------------------------------------

    /** @return Il nome del personaggio */
    public String getPlayerName() {
        return player.getName();
    }

    // -------------------------------------------------------------------------
    // Vita
    // -------------------------------------------------------------------------

    /** @return HP corrente */
    public int getHp() {
        return player.getHp();
    }

    /** @return HP massimo */
    public int getMaxHp() {
        return player.getMaxHp();
    }

    /**
     * @return Percentuale di vita (0.0 – 1.0)
     */
    public float getHpPercent() {
        if (player.getMaxHp() <= 0) return 0f;
        return Math.max(0f, Math.min(1f, (float) player.getHp() / (float) player.getMaxHp()));
    }

    // -------------------------------------------------------------------------
    // Statistiche
    // -------------------------------------------------------------------------

    /** @return Danno base */
    public int getBaseDamage() {
        return player.getBaseDamage();
    }

    /** @return Livello corrente */
    public int getLevel() {
        return player.getLevel();
    }

    /** @return Karma corrente (-50 / +50) */
    public int getKarma() {
        return player.getKarma();
    }

    /** @return XP corrente nel livello */
    public int getXp() {
        return player.getXp();
    }

    /** @return XP necessario per il prossimo livello */
    public int getXpToNext() {
        return player.getXpToNext();
    }

    // -------------------------------------------------------------------------
    // Inventario
    // -------------------------------------------------------------------------

    /**
     * @return Lista non modificabile degli oggetti nel backpack.
     */
    public List<Collectible> getInventory() {
        return player.getBackpack().getItems();
    }

    /** @return true se l'inventario è vuoto */
    public boolean isInventoryEmpty() {
        return player.getBackpack().getItems().isEmpty();
    }

    /** @return Numero di oggetti nell'inventario */
    public int getInventorySize() {
        return player.getBackpack().getSize();
    }

    /** @return Capienza massima del backpack */
    public int getInventoryCapacity() {
        return player.getBackpack().getCapacity();
    }

    // -------------------------------------------------------------------------
    // Restrizioni di fase
    // -------------------------------------------------------------------------

    /**
     * Indica se la schermata personaggio può essere aperta nella fase di gioco specificata.
     * Non è consentita durante il combattimento ({@link GameState.Phase#COMBAT}).
     *
     * @param phase La fase corrente del gioco
     * @return true se la schermata può essere aperta, false altrimenti
     */
    public boolean canOpenDuringPhase(GameState.Phase phase) {
        return phase != null && phase != GameState.Phase.COMBAT;
    }
}
