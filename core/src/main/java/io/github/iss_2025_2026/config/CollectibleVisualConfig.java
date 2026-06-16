package io.github.iss_2025_2026.config;

/** Configurazione grafica di un collectible, indipendente dal suo comportamento di gioco. */
public class CollectibleVisualConfig {
    private CollectibleVisualType type;
    private String asset;
    private int frameWidth;
    private int frameHeight;
    private float frameDuration;
    private String playMode = "LOOP";

    public CollectibleVisualConfig() {
    }

    public CollectibleVisualConfig copy() {
        CollectibleVisualConfig copy = new CollectibleVisualConfig();
        copy.type = type;
        copy.asset = asset;
        copy.frameWidth = frameWidth;
        copy.frameHeight = frameHeight;
        copy.frameDuration = frameDuration;
        copy.playMode = playMode;
        return copy;
    }

    public CollectibleVisualType getType() {
        return type;
    }

    public void setType(CollectibleVisualType type) {
        this.type = type;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public float getFrameDuration() {
        return frameDuration;
    }

    public void setFrameDuration(float frameDuration) {
        this.frameDuration = frameDuration;
    }

    public String getPlayMode() {
        return playMode;
    }

    public void setPlayMode(String playMode) {
        this.playMode = playMode;
    }
}
