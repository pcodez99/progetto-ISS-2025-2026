package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;

public final class GridSpriteSheetAnimationLoader {
    private GridSpriteSheetAnimationLoader() {
    }

    public static Animation<TextureRegion> load(String sheetPath, int columns, int rows, float frameDuration,
            List<Texture> textureRegistry) {
        return load(sheetPath, columns, rows, frameDuration, textureRegistry, false);
    }

    public static Animation<TextureRegion> load(String sheetPath, int columns, int rows, float frameDuration,
            List<Texture> textureRegistry, boolean flipX) {
        if (!Gdx.files.internal(sheetPath).exists()) {
            Gdx.app.error("GridSpriteSheetAnimationLoader", "Sprite sheet not found at: " + sheetPath);
            return null;
        }

        Texture texture = new Texture(Gdx.files.internal(sheetPath));
        if (textureRegistry != null) {
            textureRegistry.add(texture);
        }

        int safeColumns = Math.max(1, columns);
        int safeRows = Math.max(1, rows);
        int frameWidth = texture.getWidth() / safeColumns;
        int frameHeight = texture.getHeight() / safeRows;
        Array<TextureRegion> frames = new Array<>(safeColumns * safeRows);

        for (int row = 0; row < safeRows; row++) {
            for (int column = 0; column < safeColumns; column++) {
                TextureRegion frame = new TextureRegion(texture, column * frameWidth, row * frameHeight,
                        frameWidth, frameHeight);
                if (flipX) {
                    frame.flip(true, false);
                }
                frames.add(frame);
            }
        }

        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }
}
