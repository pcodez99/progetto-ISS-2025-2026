package io.github.iss_2025_2026.map;

/**
 * Checkpoint configured in the level manifest and resolved against a TMX object.
 */
public class CheckpointDefinition {
    private String id;
    private String layer = TmxMapContract.LAYER_CHECKPOINTS;
    private String objectName = TmxMapContract.CHECKPOINT_OBJECT_NAME;
    private float radius = 96f;
    private boolean autosave = true;

    public CheckpointDefinition() {
    }

    public CheckpointDefinition(String id, String layer, String objectName, float radius, boolean autosave) {
        this.id = id;
        this.layer = layer;
        this.objectName = objectName;
        this.radius = radius;
        this.autosave = autosave;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLayer() {
        return isBlank(layer) ? TmxMapContract.LAYER_CHECKPOINTS : layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getObjectName() {
        return isBlank(objectName) ? TmxMapContract.CHECKPOINT_OBJECT_NAME : objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public float getRadius() {
        return radius > 0f ? radius : 96f;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isAutosave() {
        return autosave;
    }

    public boolean getAutosave() {
        return autosave;
    }

    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
