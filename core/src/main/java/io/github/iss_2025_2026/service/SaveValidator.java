package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.PlayerSaveState;
import java.io.IOException;
import java.util.List;

/**
 * Validates save mementos before they are written or restored into runtime objects.
 */
public final class SaveValidator {
    private SaveValidator() {
    }

    public static void validateForSave(GameState state) throws IOException {
        validate(state);
    }

    public static void validateForLoad(GameState state) throws IOException {
        validate(state);
    }

    private static void validate(GameState state) throws IOException {
        if (state == null) {
            throw new IOException("Salvataggio non valido: dati assenti.");
        }
        if (state.getCurrentLevelId() <= 0) {
            throw new IOException("Salvataggio non valido: livello corrente assente.");
        }
        validateCompletedLevels(state.getCompletedLevelIds());

        if (state.getPlayerOne() != null) {
            validatePlayer(state.getPlayerOne(), "Giocatore 1");
            if (state.getPlayerTwo() != null) {
                validatePlayer(state.getPlayerTwo(), "Giocatore 2");
            }
            validateCheckpoint(state);
            return;
        }

        if (state.hasLegacySinglePlayerData()) {
            if (isBlank(state.getCharacterType())) {
                throw new IOException("Salvataggio legacy non valido: personaggio mancante.");
            }
            return;
        }

        throw new IOException("Salvataggio non valido: nessun giocatore presente.");
    }

    private static void validateCompletedLevels(List<Integer> completedLevelIds) throws IOException {
        if (completedLevelIds == null) {
            return;
        }
        for (Integer levelId : completedLevelIds) {
            if (levelId == null || levelId.intValue() <= 0) {
                throw new IOException("Salvataggio non valido: progressione livelli corrotta.");
            }
        }
    }

    private static void validatePlayer(PlayerSaveState player, String label) throws IOException {
        if (isBlank(player.getCharacterId())) {
            throw new IOException("Salvataggio non valido: " + label + " senza characterId.");
        }
        if (isBlank(player.getName())) {
            throw new IOException("Salvataggio non valido: " + label + " senza nome.");
        }
        if (player.getMaxHp() <= 0 || player.getHp() < 0 || player.getHp() > player.getMaxHp()) {
            throw new IOException("Salvataggio non valido: HP corrotti per " + label + ".");
        }
        if (player.getBaseDamage() <= 0) {
            throw new IOException("Salvataggio non valido: statistiche danno corrotte per " + label + ".");
        }
        if (player.getLevel() <= 0) {
            throw new IOException("Salvataggio non valido: livello player corrotto per " + label + ".");
        }
        validateBackpack(player.getBackpack(), label);
    }

    private static void validateBackpack(Backpack backpack, String label) throws IOException {
        if (backpack == null) {
            throw new IOException("Salvataggio non valido: inventario mancante per " + label + ".");
        }
        if (backpack.getCapacity() <= 0) {
            throw new IOException("Salvataggio non valido: capienza inventario corrotta per " + label + ".");
        }

        List<Collectible> items = backpack.getItems();
        if (items.size() > backpack.getCapacity()) {
            throw new IOException("Salvataggio non valido: inventario oltre capienza per " + label + ".");
        }
        for (Collectible item : items) {
            if (item == null || isBlank(item.getId()) || isBlank(item.getEffectType())) {
                throw new IOException("Salvataggio non valido: oggetto inventario corrotto per " + label + ".");
            }
        }
    }

    private static void validateCheckpoint(GameState state) throws IOException {
        if (!isBlank(state.getLastCheckpointId()) && state.getLastCheckpointLevelId() <= 0) {
            throw new IOException("Salvataggio non valido: checkpoint senza livello associato.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
