package io.github.iss_2025_2026.view.collectibles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.iss_2025_2026.config.CollectibleVisualConfig;
import io.github.iss_2025_2026.config.CollectibleVisualType;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Factory che costruisce la Strategy visuale dichiarata nello YAML. */
public final class CollectibleVisualFactory implements Disposable {
    private final Map<String, Texture> textures = new LinkedHashMap<>();

    public CollectibleVisual create(String collectibleId, CollectibleVisualConfig config) {
        if (config == null || config.getType() == null) {
            throw new IllegalArgumentException("Visual non configurato per " + collectibleId);
        }
        if (config.getAsset() == null || config.getAsset().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset visuale non configurato per " + collectibleId);
        }

        Texture texture = loadTexture(collectibleId, config.getAsset());
        if (config.getType() == CollectibleVisualType.STATIC) {
            return new StaticCollectibleVisual(new TextureRegion(texture));
        }
        if (config.getType() == CollectibleVisualType.ANIMATED) {
            return createAnimated(collectibleId, texture, config);
        }
        throw new IllegalArgumentException("Tipo visual non supportato per " + collectibleId);
    }

    private CollectibleVisual createAnimated(
            String collectibleId, Texture texture, CollectibleVisualConfig config) {
        int frameWidth = config.getFrameWidth();
        int frameHeight = config.getFrameHeight();
        if (frameWidth <= 0 || frameHeight <= 0 || config.getFrameDuration() <= 0f) {
            throw new IllegalArgumentException("Metadati di animazione non validi per " + collectibleId);
        }
        if (texture.getWidth() % frameWidth != 0 || texture.getHeight() % frameHeight != 0) {
            throw new IllegalArgumentException("Spritesheet di " + collectibleId
                    + " non divisibile in frame " + frameWidth + "x" + frameHeight);
        }

        TextureRegion[][] grid = TextureRegion.split(texture, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>(grid.length * grid[0].length);
        for (TextureRegion[] row : grid) {
            for (TextureRegion frame : row) {
                frames.add(frame);
            }
        }

        Animation<TextureRegion> animation = new Animation<>(config.getFrameDuration(), frames);
        try {
            if (config.getPlayMode() == null) {
                throw new IllegalArgumentException("Play mode mancante");
            }
            animation.setPlayMode(Animation.PlayMode.valueOf(
                    config.getPlayMode().trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Play mode non valido per " + collectibleId + ": " + config.getPlayMode(), exception);
        }
        return new AnimatedCollectibleVisual(animation);
    }

    private Texture loadTexture(String collectibleId, String path) {
        Texture cached = textures.get(path);
        if (cached != null) {
            return cached;
        }
        if (!Gdx.files.internal(path).exists()) {
            throw new IllegalArgumentException(
                    "Asset visuale non trovato per " + collectibleId + ": " + path);
        }
        Texture texture = new Texture(Gdx.files.internal(path));
        textures.put(path, texture);
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }
}
