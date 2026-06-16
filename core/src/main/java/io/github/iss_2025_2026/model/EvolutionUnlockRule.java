package io.github.iss_2025_2026.model;

public class EvolutionUnlockRule {
    private int altruismMin;
    private int egoismMin;
    private int helpedNpcsMin;
    private int stolenItemsMin;
    private int negativeChoicesMin;
    private int sparedAliensMin;
    private int redemptionsMin;

    public boolean isSatisfiedBy(PlayerEvolutionState state) {
        if (state == null) {
            return false;
        }
        return state.getAltruismScore() >= altruismMin
                && state.getEgoismScore() >= egoismMin
                && state.getHelpedNpcs() >= helpedNpcsMin
                && state.getStolenItems() >= stolenItemsMin
                && state.getNegativeChoices() >= negativeChoicesMin
                && state.getSparedAliens() >= sparedAliensMin
                && state.getRedemptions() >= redemptionsMin;
    }

    public int getAltruismMin() {
        return altruismMin;
    }

    public void setAltruismMin(int altruismMin) {
        this.altruismMin = Math.max(0, altruismMin);
    }

    public int getEgoismMin() {
        return egoismMin;
    }

    public void setEgoismMin(int egoismMin) {
        this.egoismMin = Math.max(0, egoismMin);
    }

    public int getHelpedNpcsMin() {
        return helpedNpcsMin;
    }

    public void setHelpedNpcsMin(int helpedNpcsMin) {
        this.helpedNpcsMin = Math.max(0, helpedNpcsMin);
    }

    public int getStolenItemsMin() {
        return stolenItemsMin;
    }

    public void setStolenItemsMin(int stolenItemsMin) {
        this.stolenItemsMin = Math.max(0, stolenItemsMin);
    }

    public int getNegativeChoicesMin() {
        return negativeChoicesMin;
    }

    public void setNegativeChoicesMin(int negativeChoicesMin) {
        this.negativeChoicesMin = Math.max(0, negativeChoicesMin);
    }

    public int getSparedAliensMin() {
        return sparedAliensMin;
    }

    public void setSparedAliensMin(int sparedAliensMin) {
        this.sparedAliensMin = Math.max(0, sparedAliensMin);
    }

    public int getRedemptionsMin() {
        return redemptionsMin;
    }

    public void setRedemptionsMin(int redemptionsMin) {
        this.redemptionsMin = Math.max(0, redemptionsMin);
    }
}
