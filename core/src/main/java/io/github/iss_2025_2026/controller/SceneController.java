package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.map.LevelAssetResolvers;
import io.github.iss_2025_2026.map.LevelCatalog;
import io.github.iss_2025_2026.map.LevelDefinition;
import io.github.iss_2025_2026.map.LevelRuntime;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.map.TmxLevelLoader;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.view.LevelScreen;
import java.io.IOException;

/**
 * MVC controller responsible for level screen navigation.
 * It loads level runtime data and activates the view, while GameState stores progress.
 */
public final class SceneController {
    public enum LoadSceneMode {
        SINGLE,
        ADDITIVE
    }

    private final Main game;
    private final GameModel model;
    private final GameController gameController;
    private final LevelCatalog catalog;
    private LevelRuntime activeScene;

    public SceneController(Main game, GameModel model, GameController gameController) {
        this.game = game;
        this.model = model;
        this.gameController = gameController;
        this.catalog = loadCatalog();
    }

    public LevelRuntime loadLevel(int levelId) {
        return loadLevel(levelId, LoadSceneMode.SINGLE);
    }

    public LevelRuntime loadLevel(int levelId, LoadSceneMode mode) {
        if (mode == LoadSceneMode.ADDITIVE) {
            throw new UnsupportedOperationException(
                    "Il caricamento additivo sara supportato quando avremo piu scene attive.");
        }

        unloadCurrentLevel();
        LevelDefinition definition = catalog.requireLevel(levelId);
        TmxLevel level = TmxLevelLoader.load(definition);
        activeScene = new LevelRuntime(definition, level);
        game.setScreen(new LevelScreen(game, model, gameController, activeScene));
        return activeScene;
    }

    public void unloadCurrentLevel() {
        activeScene = null;
    }

    public boolean hasLevel(int levelId) {
        return catalog.hasLevel(levelId);
    }

    public LevelRuntime getActiveScene() {
        return activeScene;
    }

    public LevelCatalog getCatalog() {
        return catalog;
    }

    private LevelCatalog loadCatalog() {
        try {
            return LevelCatalog.load(LevelAssetResolvers.gdx());
        } catch (IOException exception) {
            throw new GdxRuntimeException("Impossibile caricare il catalogo livelli runtime.", exception);
        }
    }
}
