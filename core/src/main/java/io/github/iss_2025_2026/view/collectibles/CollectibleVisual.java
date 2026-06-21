package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Strategy che seleziona il frame da mostrare per un collectible. */
public interface CollectibleVisual {
    TextureRegion getFrame(float stateTime);
}
