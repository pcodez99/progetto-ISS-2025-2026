package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.Player;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce il caricamento, la memorizzazione e lo smaltimento degli asset grafici e sonori
 * associati a un personaggio specifico del giocatore.
 */
public class PlayerAssets {
    private final Map<Direction, Animation<TextureRegion>> idleAnims;
    private final Map<Direction, Animation<TextureRegion>> walkAnims;
    private Animation<TextureRegion> attackAnim;
    private Sound attackSound;
    private final List<Texture> loadedTextures;

    public PlayerAssets(Player player) {
        this.idleAnims = new EnumMap<>(Direction.class);
        this.walkAnims = new EnumMap<>(Direction.class);
        this.loadedTextures = new ArrayList<>();
        
        loadAssets(player);
    }

    private void loadAssets(Player player) {
        String basePath = "characters/child-spritesheet";
        if (player != null) {
            String charId = player.getCharacterId();
            if ("papa".equals(charId)) {
                basePath = "characters/Father-spritesheet";
            } else if ("mamma".equals(charId)) {
                basePath = "characters/mom-spritesheet";
            } else if ("nonno".equals(charId)) {
                basePath = "characters/nonno-spritesheet";
            }
        }

        Animation<TextureRegion> walkUp = loadFirstAvailable(
                basePath, 0.06f, false, "iso_walk_northeast_right",
                "iso_walk_up_right", "iso_idle_up_right",
                "iso_walk_right_right", "iso_idle_right_right", "idle_right");
        Animation<TextureRegion> walkLeft = loadFirstAvailable(
                basePath, 0.06f, true, "iso_walk_northeast_right",
                "iso_walk_up_right", "iso_idle_up_right",
                "iso_walk_right_right", "iso_idle_right_right", "idle_right");
        Animation<TextureRegion> walkRight = loadFirstAvailable(
                basePath, 0.06f, false, "iso_walk_southeast_right",
                "iso_walk_down_right", "iso_idle_down_right", "walk_right", "iso_walk_right_right",
                "iso_idle_right_right", "idle_right");
        Animation<TextureRegion> walkDown = loadFirstAvailable(
                basePath, 0.06f, true, "iso_walk_southeast_right",
                "iso_walk_down_right", "iso_idle_down_right", "walk_right", "iso_walk_right_right",
                "iso_idle_right_right", "idle_right");

        if (walkRight != null) walkAnims.put(Direction.RIGHT, walkRight);
        if (walkLeft != null) walkAnims.put(Direction.LEFT, walkLeft);
        if (walkUp != null) walkAnims.put(Direction.UP, walkUp);
        if (walkDown != null) walkAnims.put(Direction.DOWN, walkDown);

        setupIdleAnimation(Direction.RIGHT, walkRight);
        setupIdleAnimation(Direction.LEFT, walkLeft);
        setupIdleAnimation(Direction.UP, walkUp);
        setupIdleAnimation(Direction.DOWN, walkDown);

        // Attack animation
        attackAnim = JsonAnimationLoader.load(basePath + "/attack_right", 0.04f, loadedTextures, false);

        // Sound loading
        String characterId = player != null ? player.getCharacterId() : "bambino";
        String soundSuffix = getSoundSuffix(characterId);
        String soundPath = basePath + "/attack_right/attack_" + soundSuffix + ".mp3";
        if (Gdx.files.internal(soundPath).exists()) {
            attackSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        } else {
            Gdx.app.log("PlayerAssets", "Suono di attacco non trovato al path: " + soundPath);
        }
    }

    private Animation<TextureRegion> loadFirstAvailable(
            String basePath, float frameDuration, boolean flipX, String... animationFolders) {
        for (String animationFolder : animationFolders) {
            String folderPath = basePath + "/" + animationFolder;
            if (Gdx.files.internal(folderPath + "/atlas.json").exists()) {
                return JsonAnimationLoader.load(folderPath, frameDuration, loadedTextures, flipX);
            }
        }
        return null;
    }

    private void setupIdleAnimation(Direction dir, Animation<TextureRegion> walkAnim) {
        if (walkAnim != null) {
            TextureRegion idleFrame = walkAnim.getKeyFrame(0f);
            if (idleFrame != null) {
                idleAnims.put(dir, new Animation<>(0.1f, idleFrame));
            }
        }
    }

    private String getSoundSuffix(String characterId) {
        if ("papa".equals(characterId)) return "father";
        if ("bambino".equals(characterId)) return "child";
        if ("mamma".equals(characterId)) return "mom";
        if ("nonno".equals(characterId)) return "nonno";
        return "child";
    }

    public Animation<TextureRegion> getIdleAnim(Direction dir) {
        return idleAnims.get(dir);
    }

    public Animation<TextureRegion> getWalkAnim(Direction dir) {
        return walkAnims.get(dir);
    }

    public Animation<TextureRegion> getAttackAnim() {
        return attackAnim;
    }

    public Sound getAttackSound() {
        return attackSound;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        loadedTextures.clear();

        if (attackSound != null) {
            attackSound.dispose();
        }
    }
}
