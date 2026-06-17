package io.github.iss_2025_2026.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.iss_2025_2026.model.Npc;
import java.util.ArrayList;
import java.util.List;

public class NpcAssets {
    private static final int IDLE_COLUMNS = 5;
    private static final int IDLE_ROWS = 5;
    private static final float IDLE_FRAME_DURATION = 0.18f;
    private static final String NPC_BASE_PATH = "characters/npcs";

    private final List<Texture> loadedTextures = new ArrayList<>();
    private Animation<TextureRegion> idleSoutheast;
    private Animation<TextureRegion> idleSouthwest;
    private Animation<TextureRegion> idleNortheast;

    public NpcAssets(Npc npc) {
        String folderName = resolveFolderName(npc);
        if (folderName == null) {
            return;
        }

        String basePath = NPC_BASE_PATH + "/" + folderName;
        idleSoutheast = GridSpriteSheetAnimationLoader.load(basePath + "/idle_southeast.png",
                IDLE_COLUMNS, IDLE_ROWS, IDLE_FRAME_DURATION, loadedTextures);
        idleSouthwest = GridSpriteSheetAnimationLoader.load(basePath + "/idle_southeast.png",
                IDLE_COLUMNS, IDLE_ROWS, IDLE_FRAME_DURATION, loadedTextures, true);
        idleNortheast = GridSpriteSheetAnimationLoader.load(basePath + "/idle_northeast.png",
                IDLE_COLUMNS, IDLE_ROWS, IDLE_FRAME_DURATION, loadedTextures);
    }

    public Animation<TextureRegion> getIdleSouthwestAnimation() {
        if (idleSouthwest != null) {
            return idleSouthwest;
        }
        if (idleSoutheast != null) {
            return idleSoutheast;
        }
        return idleNortheast;
    }

    public boolean isAvailable() {
        return idleSouthwest != null || idleSoutheast != null || idleNortheast != null;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        loadedTextures.clear();
    }

    private String resolveFolderName(Npc npc) {
        if (npc == null || npc.getId() == null) {
            return null;
        }
        return npc.getId().trim();
    }
}
