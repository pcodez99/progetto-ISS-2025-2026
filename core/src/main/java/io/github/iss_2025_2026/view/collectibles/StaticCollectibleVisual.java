package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Strategy per immagini prive di animazione. */
public final class StaticCollectibleVisual implements CollectibleVisual {
    private final TextureRegion region;

    public StaticCollectibleVisual(TextureRegion region) {
        if (region == null) {
            throw new IllegalArgumentException("La regione statica non puo essere nulla.");
        }
        this.region = region;
    }

    @Override
    public TextureRegion getFrame(float stateTime) {
        return region;
    }
}
