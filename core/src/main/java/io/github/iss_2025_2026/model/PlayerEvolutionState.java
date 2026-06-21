package io.github.iss_2025_2026.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerEvolutionState {
    private int altruismScore;
    private int egoismScore;
    private int helpedNpcs;
    private int stolenItems;
    private int liesTold;
    private int threatenedNpcs;
    private int ignoredRequests;
    private int sparedAliens;
    private int selfishRewards;
    private int redemptions;
    private AbilitySlot selectedAbilitySlot = AbilitySlot.BASE;
    private Set<AbilitySlot> unlockedAbilitySlots = EnumSet.of(AbilitySlot.BASE);
    private Map<String, Integer> npcTrust = new HashMap<>();
    private Map<String, NpcHelpRequestOutcome> npcHelpOutcomes = new HashMap<>();

    public PlayerEvolutionState() {
        unlockAbilitySlot(AbilitySlot.BASE);
    }

    public void addAltruism(int amount) {
        if (amount > 0) {
            altruismScore += amount;
        }
    }

    public void addEgoism(int amount) {
        if (amount > 0) {
            egoismScore += amount;
        }
    }

    public EvolutionPath getDominantPath() {
        int delta = altruismScore - egoismScore;
        if (delta >= 10) {
            return EvolutionPath.ALTRUISTIC;
        }
        if (delta <= -10) {
            return EvolutionPath.EGOISTIC;
        }
        return EvolutionPath.BALANCED;
    }

    public int getNegativeChoices() {
        return stolenItems + liesTold + threatenedNpcs + ignoredRequests + selfishRewards;
    }

    public void increaseNpcTrust(String npcId, int amount) {
        if (npcId == null || npcId.trim().isEmpty() || amount == 0) {
            return;
        }
        npcTrust.put(npcId, getNpcTrust(npcId) + amount);
    }

    public int getNpcTrust(String npcId) {
        if (npcId == null) {
            return 0;
        }
        Integer trust = npcTrust.get(npcId);
        return trust != null ? trust.intValue() : 0;
    }

    public boolean isAbilitySlotUnlocked(AbilitySlot slot) {
        return slot != null && unlockedAbilitySlots.contains(slot);
    }

    public void unlockAbilitySlot(AbilitySlot slot) {
        if (slot != null) {
            unlockedAbilitySlots.add(slot);
        }
    }

    public int getAltruismScore() {
        return altruismScore;
    }

    public void setAltruismScore(int altruismScore) {
        this.altruismScore = Math.max(0, altruismScore);
    }

    public int getEgoismScore() {
        return egoismScore;
    }

    public void setEgoismScore(int egoismScore) {
        this.egoismScore = Math.max(0, egoismScore);
    }

    public int getHelpedNpcs() {
        return helpedNpcs;
    }

    public void setHelpedNpcs(int helpedNpcs) {
        this.helpedNpcs = Math.max(0, helpedNpcs);
    }

    public void incrementHelpedNpcs() {
        helpedNpcs++;
    }

    public int getStolenItems() {
        return stolenItems;
    }

    public void setStolenItems(int stolenItems) {
        this.stolenItems = Math.max(0, stolenItems);
    }

    public void incrementStolenItems() {
        stolenItems++;
    }

    public int getLiesTold() {
        return liesTold;
    }

    public void setLiesTold(int liesTold) {
        this.liesTold = Math.max(0, liesTold);
    }

    public void incrementLiesTold() {
        liesTold++;
    }

    public int getThreatenedNpcs() {
        return threatenedNpcs;
    }

    public void setThreatenedNpcs(int threatenedNpcs) {
        this.threatenedNpcs = Math.max(0, threatenedNpcs);
    }

    public void incrementThreatenedNpcs() {
        threatenedNpcs++;
    }

    public int getIgnoredRequests() {
        return ignoredRequests;
    }

    public void setIgnoredRequests(int ignoredRequests) {
        this.ignoredRequests = Math.max(0, ignoredRequests);
    }

    public void incrementIgnoredRequests() {
        ignoredRequests++;
    }

    public int getSparedAliens() {
        return sparedAliens;
    }

    public void setSparedAliens(int sparedAliens) {
        this.sparedAliens = Math.max(0, sparedAliens);
    }

    public void incrementSparedAliens() {
        sparedAliens++;
    }

    public int getSelfishRewards() {
        return selfishRewards;
    }

    public void setSelfishRewards(int selfishRewards) {
        this.selfishRewards = Math.max(0, selfishRewards);
    }

    public void incrementSelfishRewards() {
        selfishRewards++;
    }

    public int getRedemptions() {
        return redemptions;
    }

    public void setRedemptions(int redemptions) {
        this.redemptions = Math.max(0, redemptions);
    }

    public void incrementRedemptions() {
        redemptions++;
    }

    public AbilitySlot getSelectedAbilitySlot() {
        return selectedAbilitySlot != null ? selectedAbilitySlot : AbilitySlot.BASE;
    }

    public void setSelectedAbilitySlot(AbilitySlot selectedAbilitySlot) {
        this.selectedAbilitySlot = selectedAbilitySlot != null ? selectedAbilitySlot : AbilitySlot.BASE;
    }

    public Set<AbilitySlot> getUnlockedAbilitySlots() {
        return Collections.unmodifiableSet(unlockedAbilitySlots);
    }

    public void setUnlockedAbilitySlots(Set<AbilitySlot> unlockedAbilitySlots) {
        this.unlockedAbilitySlots = unlockedAbilitySlots != null && !unlockedAbilitySlots.isEmpty()
                ? EnumSet.copyOf(unlockedAbilitySlots)
                : EnumSet.of(AbilitySlot.BASE);
        unlockAbilitySlot(AbilitySlot.BASE);
    }

    public Map<String, Integer> getNpcTrust() {
        return Collections.unmodifiableMap(npcTrust);
    }

    public void setNpcTrust(Map<String, Integer> npcTrust) {
        this.npcTrust = new HashMap<>();
        if (npcTrust != null) {
            this.npcTrust.putAll(npcTrust);
        }
    }

    public boolean hasNpcHelpOutcome(String npcId) {
        return npcId != null && npcHelpOutcomes.containsKey(npcId);
    }

    public NpcHelpRequestOutcome getNpcHelpOutcome(String npcId) {
        return npcId != null ? npcHelpOutcomes.get(npcId) : null;
    }

    public void recordNpcHelpOutcome(String npcId, NpcHelpRequestOutcome outcome) {
        if (npcId != null && !npcId.trim().isEmpty() && outcome != null) {
            npcHelpOutcomes.put(npcId, outcome);
        }
    }

    public Map<String, NpcHelpRequestOutcome> getNpcHelpOutcomes() {
        return Collections.unmodifiableMap(npcHelpOutcomes);
    }

    public void setNpcHelpOutcomes(Map<String, NpcHelpRequestOutcome> npcHelpOutcomes) {
        this.npcHelpOutcomes = new HashMap<>();
        if (npcHelpOutcomes != null) {
            this.npcHelpOutcomes.putAll(npcHelpOutcomes);
        }
    }
}
