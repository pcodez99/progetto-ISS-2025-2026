package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Strategy per collectible rappresentati da uno spritesheet animato. */
public final class AnimatedCollectibleVisual implements CollectibleVisual {
    private final Animation<TextureRegion> animation;

    public AnimatedCollectibleVisual(Animation<TextureRegion> animation) {
        if (animation == null) {
            throw new IllegalArgumentException("L'animazione non puo essere nulla.");
        }
        this.animation = animation;
    }

    @Override
    public TextureRegion getFrame(float stateTime) {
        return animation.getKeyFrame(Math.max(0f, stateTime));
    }
}
