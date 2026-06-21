package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import io.github.iss_2025_2026.config.CollectibleVisualConfig;
import io.github.iss_2025_2026.service.CollectibleService;
import java.util.LinkedHashMap;
import java.util.Map;

/** Orchestra caricamento, aggiornamento e disegno dei collectible sulla mappa. */
public final class CollectibleRenderer implements Disposable {
    private static final float DEFAULT_SIZE = 64f;

    private final CollectibleVisualFactory visualFactory;
    private final Map<String, CollectibleVisual> visuals = new LinkedHashMap<>();
    private float stateTime;

    public CollectibleRenderer(Map<String, CollectibleVisualConfig> configs) {
        this.visualFactory = new CollectibleVisualFactory();
        try {
            for (Map.Entry<String, CollectibleVisualConfig> entry : configs.entrySet()) {
                visuals.put(entry.getKey(), visualFactory.create(entry.getKey(), entry.getValue()));
            }
        } catch (RuntimeException exception) {
            visualFactory.dispose();
            throw exception;
        }
    }

    public void update(float delta) {
        stateTime += Math.max(0f, delta);
    }

    public void render(
            Batch batch,
            Matrix4 projectionMatrix,
            Iterable<CollectibleService.CollectibleOnMap> collectibles) {
        batch.setProjectionMatrix(projectionMatrix);
        batch.begin();
        for (CollectibleService.CollectibleOnMap item : collectibles) {
            CollectibleVisual visual = visuals.get(item.getCollectible().getId());
            if (visual == null) {
                continue;
            }
            TextureRegion frame = visual.getFrame(stateTime);
            batch.draw(
                    frame,
                    item.getX() - DEFAULT_SIZE / 2f,
                    item.getY() - DEFAULT_SIZE / 2f,
                    DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        visuals.clear();
        visualFactory.dispose();
    }
}
