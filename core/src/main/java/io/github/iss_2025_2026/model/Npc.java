package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Npc {
    private String id;
    private String name;
    private String levelId;
    private NpcType type;
    private String role;
    private String setting;
    private String appearance;
    private String personality;
    private String gameplayFunction;
    private String mission;
    private String rewardId;
    private String rareRewardId;
    private int altruismReward;
    private int altruismPenalty;
    private boolean canBecomeHostile;
    private String negativeConsequence;
    private String sampleDialogue;
    private String dialoguePrompt;
    private NpcVoiceConfig voice;
    private NpcHelpRequest helpRequest;
    private List<String> interactionOptions = new ArrayList<>();
    private List<String> traits = new ArrayList<>();

    public Npc() {
    }

    private Npc(Npc source) {
        this.id = source.id;
        this.name = source.name;
        this.levelId = source.levelId;
        this.type = source.type;
        this.role = source.role;
        this.setting = source.setting;
        this.appearance = source.appearance;
        this.personality = source.personality;
        this.gameplayFunction = source.gameplayFunction;
        this.mission = source.mission;
        this.rewardId = source.rewardId;
        this.rareRewardId = source.rareRewardId;
        this.altruismReward = source.altruismReward;
        this.altruismPenalty = source.altruismPenalty;
        this.canBecomeHostile = source.canBecomeHostile;
        this.negativeConsequence = source.negativeConsequence;
        this.sampleDialogue = source.sampleDialogue;
        this.dialoguePrompt = source.dialoguePrompt;
        this.voice = source.voice != null ? source.voice.copy() : null;
        this.helpRequest = source.helpRequest != null ? source.helpRequest.copy() : null;
        this.interactionOptions = new ArrayList<>(source.getInteractionOptions());
        this.traits = new ArrayList<>(source.getTraits());
    }

    public Npc copy() {
        return new Npc(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public NpcType getType() {
        return type;
    }

    public void setType(NpcType type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getGameplayFunction() {
        return gameplayFunction;
    }

    public void setGameplayFunction(String gameplayFunction) {
        this.gameplayFunction = gameplayFunction;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getRareRewardId() {
        return rareRewardId;
    }

    public void setRareRewardId(String rareRewardId) {
        this.rareRewardId = rareRewardId;
    }

    public int getAltruismReward() {
        return altruismReward;
    }

    public void setAltruismReward(int altruismReward) {
        this.altruismReward = altruismReward;
    }

    public int getAltruismPenalty() {
        return altruismPenalty;
    }

    public void setAltruismPenalty(int altruismPenalty) {
        this.altruismPenalty = altruismPenalty;
    }

    public boolean canBecomeHostile() {
        return canBecomeHostile;
    }

    public boolean isCanBecomeHostile() {
        return canBecomeHostile;
    }

    public void setCanBecomeHostile(boolean canBecomeHostile) {
        this.canBecomeHostile = canBecomeHostile;
    }

    public String getNegativeConsequence() {
        return negativeConsequence;
    }

    public void setNegativeConsequence(String negativeConsequence) {
        this.negativeConsequence = negativeConsequence;
    }

    public String getSampleDialogue() {
        return sampleDialogue;
    }

    public void setSampleDialogue(String sampleDialogue) {
        this.sampleDialogue = sampleDialogue;
    }

    public String getDialoguePrompt() {
        return dialoguePrompt;
    }

    public void setDialoguePrompt(String dialoguePrompt) {
        this.dialoguePrompt = dialoguePrompt;
    }

    public NpcVoiceConfig getVoice() {
        return voice != null ? voice.copy() : null;
    }

    public void setVoice(NpcVoiceConfig voice) {
        this.voice = voice != null ? voice.copy() : null;
    }

    public NpcHelpRequest getHelpRequest() {
        return helpRequest != null ? helpRequest.copy() : null;
    }

    public void setHelpRequest(NpcHelpRequest helpRequest) {
        this.helpRequest = helpRequest != null ? helpRequest.copy() : null;
    }

    public List<String> getInteractionOptions() {
        return Collections.unmodifiableList(interactionOptions);
    }

    public void setInteractionOptions(List<String> interactionOptions) {
        this.interactionOptions = interactionOptions != null
                ? new ArrayList<>(interactionOptions)
                : new ArrayList<String>();
    }

    public List<String> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public void setTraits(List<String> traits) {
        this.traits = traits != null ? new ArrayList<>(traits) : new ArrayList<String>();
    }
}
