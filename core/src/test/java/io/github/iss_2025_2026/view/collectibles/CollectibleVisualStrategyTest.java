package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class CollectibleVisualStrategyTest {

    @Test
    void visualStaticoRestituisceSempreLaStessaRegione() {
        TextureRegion region = new TextureRegion();
        StaticCollectibleVisual visual = new StaticCollectibleVisual(region);

        assertSame(region, visual.getFrame(0f));
        assertSame(region, visual.getFrame(42f));
    }

    @Test
    void visualAnimatoSelezionaIlFrameInBaseAlTempo() {
        TextureRegion first = new TextureRegion();
        TextureRegion second = new TextureRegion();
        Array<TextureRegion> frames = new Array<>();
        frames.add(first);
        frames.add(second);
        Animation<TextureRegion> animation = new Animation<>(0.1f, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        AnimatedCollectibleVisual visual = new AnimatedCollectibleVisual(animation);

        assertSame(first, visual.getFrame(0f));
        assertSame(second, visual.getFrame(0.11f));
        assertSame(first, visual.getFrame(0.21f));
    }
}
