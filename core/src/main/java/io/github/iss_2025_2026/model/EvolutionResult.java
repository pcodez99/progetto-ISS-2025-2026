package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvolutionResult {
    private final EvolutionPath previousPath;
    private final EvolutionPath currentPath;
    private final AbilitySlot selectedAbilitySlot;
    private final List<AbilitySlot> unlockedSlots;

    public EvolutionResult(EvolutionPath previousPath, EvolutionPath currentPath,
            AbilitySlot selectedAbilitySlot, List<AbilitySlot> unlockedSlots) {
        this.previousPath = previousPath;
        this.currentPath = currentPath;
        this.selectedAbilitySlot = selectedAbilitySlot;
        this.unlockedSlots = new ArrayList<>(
                unlockedSlots != null ? unlockedSlots : Collections.<AbilitySlot>emptyList());
    }

    public EvolutionPath getPreviousPath() {
        return previousPath;
    }

    public EvolutionPath getCurrentPath() {
        return currentPath;
    }

    public AbilitySlot getSelectedAbilitySlot() {
        return selectedAbilitySlot;
    }

    public List<AbilitySlot> getUnlockedSlots() {
        return Collections.unmodifiableList(unlockedSlots);
    }

    public boolean hasNewUnlocks() {
        return !unlockedSlots.isEmpty();
    }
}
