package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Array;
import java.util.List;

/**
 * Utility loader to parse atlas.json files and load individual frame-by-frame PNGs
 * from the 'frames' subdirectory.
 */
public class JsonAnimationLoader {

    /**
     * Loads an animation from a JSON + frame-by-frame directory.
     *
     * @param folderPath The directory path containing atlas.json and a frames/ subdirectory
     * @param frameDuration Time duration for each frame in seconds
     * @param textureRegistry A list where loaded Textures will be added for future disposal
     * @param flipX Whether to flip the animation frames horizontally
     * @return A LibGDX Animation of TextureRegions
     */
    public static Animation<TextureRegion> load(String folderPath, float frameDuration, List<Texture> textureRegistry, boolean flipX) {
        String jsonPath = folderPath + "/atlas.json";
        if (!Gdx.files.internal(jsonPath).exists()) {
            Gdx.app.error("JsonAnimationLoader", "atlas.json not found at: " + jsonPath);
            return null;
        }

        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));
            JsonValue framesObj = root.get("frames");

            if (framesObj == null) {
                Gdx.app.error("JsonAnimationLoader", "No 'frames' object found in: " + jsonPath);
                return null;
            }

            int frameCount = framesObj.size;
            Array<TextureRegion> frames = new Array<>(frameCount);

            for (int i = 0; i < frameCount; i++) {
                String frameName = String.format("%02d.png", i + 1);
                String framePath = folderPath + "/frames/" + frameName;

                if (!Gdx.files.internal(framePath).exists()) {
                    Gdx.app.error("JsonAnimationLoader", "Frame file not found: " + framePath);
                    continue;
                }

                Texture texture = new Texture(Gdx.files.internal(framePath));
                if (textureRegistry != null) {
                    textureRegistry.add(texture);
                }

                TextureRegion region = new TextureRegion(texture);
                if (flipX) {
                    region.flip(true, false);
                }
                frames.add(region);
            }

            if (frames.size == 0) {
                Gdx.app.error("JsonAnimationLoader", "No frames successfully loaded for: " + folderPath);
                return null;
            }

            return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
        } catch (Exception e) {
            Gdx.app.error("JsonAnimationLoader", "Error loading JSON animation from " + folderPath, e);
            return null;
        }
    }
}
