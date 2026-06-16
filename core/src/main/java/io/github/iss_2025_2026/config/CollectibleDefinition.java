package io.github.iss_2025_2026.config;

import io.github.iss_2025_2026.model.Collectible;

/** DTO che rappresenta una voce completa di configs/collectibles.yaml. */
public class CollectibleDefinition {
    private String id;
    private String name;
    private String description;
    private String effectType;
    private boolean aoe;
    private int effectValue;
    private CollectibleVisualConfig visual;

    public CollectibleDefinition() {
    }

    public Collectible toCollectible() {
        return new Collectible(id, name, description, effectType, aoe, effectValue);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public boolean isAoe() {
        return aoe;
    }

    public void setAoe(boolean aoe) {
        this.aoe = aoe;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public void setEffectValue(int effectValue) {
        this.effectValue = effectValue;
    }

    public CollectibleVisualConfig getVisual() {
        return visual;
    }

    public void setVisual(CollectibleVisualConfig visual) {
        this.visual = visual;
    }
}
