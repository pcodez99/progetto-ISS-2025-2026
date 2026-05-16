package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import java.util.EnumMap;
import java.util.Map;
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.RunMusicManager;

/**
 * Game View (Parte del pattern MVC).
 * Implementazione di {@link Screen} di LibGDX.
 * Si occupa esclusivamente del rendering dello stato del Model.
 * Delega la logica di aggiornamento e input al Controller.
 */
public class TestScreen implements Screen {
    private final GameModel model;
    private final GameController controller;
    private final Main game;
    private final InputAdapter inputListener;
    private final AssetManager assetManager;

    private Stage stage;
    private Skin skin;
    private Table root;
    private Map<Direction, Animation<TextureRegion>> childIdleAnims;
    private Map<Direction, Animation<TextureRegion>> childWalkAnims;
    private float stateTime;

    private Label messageLabel;
    private Label timerLabel;
    private Label infoLabel;

    private static final String ATLAS_PATH = "sprites/characters/child/child_sprite.atlas";
    private static final String WALK_FRONT = "sprites/characters/child/walk/child_front.atlas";
    private static final String WALK_BACK = "sprites/characters/child/walk/child_back.atlas";
    private static final String WALK_LEFT = "sprites/characters/child/walk/child_left.atlas";
    private static final String WALK_RIGHT = "sprites/characters/child/walk/child_right.atlas";

    private static final String BG_PATH = "test_walk_background.png";
    private static final String BG_FALLBACK_PATH = "select-character-bg.png";

    public TestScreen(Main game, GameModel model, GameController controller) {
        this.game = game;
        this.model = model;
        this.controller = controller;
        this.assetManager = game.getAssetManager();
        this.skin = GameUiTheme.loadSkin();

        // Carichiamo gli asset necessari tramite AssetManager
        assetManager.load(ATLAS_PATH, TextureAtlas.class);
        assetManager.load(WALK_FRONT, TextureAtlas.class);
        assetManager.load(WALK_BACK, TextureAtlas.class);
        assetManager.load(WALK_LEFT, TextureAtlas.class);
        assetManager.load(WALK_RIGHT, TextureAtlas.class);

        if (Gdx.files.internal(BG_PATH).exists()) {
            assetManager.load(BG_PATH, Texture.class);
        } else {
            assetManager.load(BG_FALLBACK_PATH, Texture.class);
        }
        assetManager.finishLoading();

        // Inizializziamo le mappe delle animazioni
        childIdleAnims = new EnumMap<>(Direction.class);
        childWalkAnims = new EnumMap<>(Direction.class);

        // Inizializziamo l'animazione Idle
        TextureAtlas atlas = assetManager.get(ATLAS_PATH, TextureAtlas.class);
        TextureRegion fullSheet = atlas.findRegion("Child-idle");
        if (fullSheet != null) {
            TextureRegion[][] grid = fullSheet.split(256, 256);
            childIdleAnims.put(Direction.DOWN, new Animation<>(0.1f, grid[0]));
            childIdleAnims.put(Direction.LEFT, new Animation<>(0.1f, grid[1]));
            childIdleAnims.put(Direction.RIGHT, new Animation<>(0.1f, grid[2]));
            childIdleAnims.put(Direction.UP, new Animation<>(0.1f, grid[3]));
        }

        // Inizializziamo le animazioni Walk dai 4 atlas separati
        loadWalkAnimation(Direction.DOWN, WALK_FRONT, "front");
        loadWalkAnimation(Direction.UP, WALK_BACK, "back");
        loadWalkAnimation(Direction.LEFT, WALK_LEFT, "left");
        loadWalkAnimation(Direction.RIGHT, WALK_RIGHT, "right");

        stateTime = 0f;

        buildUI();

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MainMenuScreen(game, model, controller, true, TestScreen.this));
                    return true;
                }

                return false;
            }
        };
    }

    private void loadWalkAnimation(Direction dir, String atlasPath, String regionName) {
        if (assetManager.isLoaded(atlasPath)) {
            TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);
            TextureRegion sheet = atlas.findRegion(regionName);
            if (sheet != null) {
                // Slicing 5x5 (o comunque su base 5 frame)
                childWalkAnims.put(dir, new Animation<>(0.06f, slice(sheet, 5, 5)));
            }
        }
    }

    private TextureRegion[] slice(TextureRegion region, int rows, int cols) {
        int frameWidth = region.getRegionWidth() / cols;
        int frameHeight = region.getRegionHeight() / rows;
        TextureRegion[][] temp = region.split(frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = temp[i][j];
            }
        }
        return frames;
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        String currentBg = Gdx.files.internal(BG_PATH).exists() ? BG_PATH : BG_FALLBACK_PATH;
        Texture bgTexture = assetManager.get(currentBg, Texture.class);

        // Creiamo il root con background e wash identici alla selezione personaggio
        root = GameUiFactory.createScreenRoot(stage, bgTexture, skin);

        // Actor personalizzato per l'animazione
        Image animatedSprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Player player = model.getPlayerOne();
                if (player == null)
                    return;

                Direction dir = player.getDirection();
                boolean isMoving = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.S) ||
                        Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D);

                Animation<TextureRegion> currentAnim = isMoving ? childWalkAnims.get(dir) : childIdleAnims.get(dir);

                if (currentAnim != null) {
                    TextureRegion frame = currentAnim.getKeyFrame(stateTime, true);
                    batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),
                            getScaleY(), getRotation());
                }
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                // Sincronizziamo la posizione con il model
                if (model.getPlayerOne() != null) {
                    setX(model.getPlayerOne().getX());
                    setY(model.getPlayerOne().getY());
                }
            }
        };
        root.addActor(animatedSprite);
        animatedSprite.setSize(160f, 160f); // Diminuita la dimensione
        // Centriamo la posizione iniziale nel model se necessario
        if (model.getPlayerOne() != null && model.getPlayerOne().getX() == 0) {
            model.getPlayerOne().setX(Gdx.graphics.getWidth() / 2f - 80f);
            model.getPlayerOne().setY(Gdx.graphics.getHeight() / 2f - 80f);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputListener);
        MenuMusicManager.pause();
        RunMusicManager.play();
    }

    @Override
    public void render(float delta) {
        // 1. Update logic (via Controller)
        controller.update(delta);
        stateTime += delta;

        // 2. Render (View)
        ScreenUtils.clear(0, 0, 0, 1);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        RunMusicManager.pause();
    }

    @Override
    public void dispose() {
        RunMusicManager.stop();
        stage.dispose();
        skin.dispose();

        // Facciamo l'unload degli asset
        if (assetManager.isLoaded(ATLAS_PATH)) {
            assetManager.unload(ATLAS_PATH);
        }
        unloadIfLoaded(WALK_FRONT);
        unloadIfLoaded(WALK_BACK);
        unloadIfLoaded(WALK_LEFT);
        unloadIfLoaded(WALK_RIGHT);

        String currentBg = Gdx.files.internal(BG_PATH).exists() ? BG_PATH : BG_FALLBACK_PATH;
        if (assetManager.isLoaded(currentBg)) {
            assetManager.unload(currentBg);
        }
    }

    private void unloadIfLoaded(String path) {
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }
}
