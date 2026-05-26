package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.map.TmxLevelLoader;
import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.physics.PhysicsFacade;
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.RunMusicManager;

/**
 * Game View (Parte del pattern MVC).
 * Implementazione di {@link Screen} di LibGDX.
 * Si occupa esclusivamente del rendering dello stato del Model.
 * Delega la logica di aggiornamento al Controller, la fisica a PhysicsFacade e gli asset a PlayerAssets.
 */
public class TestScreen implements Screen {
    private final GameModel model;
    private final GameController controller;
    private final InputAdapter inputListener;

    private Stage stage;
    private Skin skin;
    private Table root;
    private TmxLevel level;
    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;
    
    // Design Patterns delegation
    private PhysicsFacade physicsFacade;
    private PlayerAssets playerAssets;
    
    private CharacterState lastState = CharacterState.IDLE;
    private Rectangle mapBounds;
    private float cameraEdgePadding;

    private static final String MAP_PATH = TmxMapContract.CAMPAIGN_MAP_PATH;
    private static final float PLAYER_SIZE = 160f;
    private static final float PLAYER_Y_OFFSET = PLAYER_SIZE * 0.48f;
    private static final float CAMERA_ZOOM = 0.72f;
    private static final float DEFAULT_CAMERA_EDGE_PADDING = 320f;
    private boolean drawObstacleDebug = true;

    public TestScreen(Main game, GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.skin = GameUiTheme.loadSkin();
        
        loadMap();

        Player player = model.getPlayerOne();
        this.playerAssets = new PlayerAssets(player);
        
        // Sincronizza la durata dell'attacco del modello con la durata dell'animazione caricata
        if (player != null && playerAssets.getAttackAnim() != null) {
            player.setAttackDuration(playerAssets.getAttackAnim().getAnimationDuration());
        }

        buildUI();

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MainMenuScreen(game, model, controller, true, TestScreen.this));
                    return true;
                }
                if (keycode == Input.Keys.F3) {
                    drawObstacleDebug = !drawObstacleDebug;
                    return true;
                }

                return false;
            }
        };
    }

    private void loadMap() {
        level = TmxLevelLoader.load(MAP_PATH);
        map = level.getMap();
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
        mapBounds = level.getGeometry().getBounds();
        cameraEdgePadding = level.getGeometry().mapPropertyFloat("camera_edge_padding", DEFAULT_CAMERA_EDGE_PADDING);
        
        // Inizializza la facciata fisica Box2D
        physicsFacade = new PhysicsFacade(level, PLAYER_SIZE, PLAYER_Y_OFFSET);
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Actor personalizzato per il rendering delle animazioni del giocatore basato sullo State Pattern
        Image animatedSprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Player player = model.getPlayerOne();
                if (player == null)
                    return;

                Direction dir = player.getDirection();
                CharacterState state = player.getState();
                TextureRegion frame = null;

                float drawX = getX();
                float drawY = getY();
                float drawWidth = getWidth();
                float drawHeight = getHeight();

                if (state == CharacterState.ATTACKING && playerAssets.getAttackAnim() != null) {
                    frame = playerAssets.getAttackAnim().getKeyFrame(player.getStateTime(), false);
                    if (dir == Direction.LEFT) {
                        TextureRegion flippedFrame = new TextureRegion(frame);
                        flippedFrame.flip(true, false);
                        frame = flippedFrame;
                    }
                    
                    // Centra e ridimensiona l'animazione di attacco per allinearla a quella di idle
                    if ("bambino".equals(player.getCharacterId())) {
                        drawWidth = getWidth() * 0.45f;
                        drawHeight = getHeight() * 0.45f;
                        drawX = getX() + (getWidth() - drawWidth) / 2f;
                        drawY = getY(); // Mantiene i piedi allineati al terreno
                    }
                } else {
                    Animation<TextureRegion> currentAnim = (state == CharacterState.WALKING) 
                            ? playerAssets.getWalkAnim(dir) 
                            : playerAssets.getIdleAnim(dir);

                    if (currentAnim != null) {
                        frame = currentAnim.getKeyFrame(player.getStateTime(), true);
                    }
                }

                if (frame != null) {
                    batch.draw(frame, drawX, drawY, getOriginX(), getOriginY(), drawWidth, drawHeight, getScaleX(),
                            getScaleY(), getRotation());
                }
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                // Sincronizza la posizione dell'attore con le coordinate del modello del giocatore
                if (model.getPlayerOne() != null) {
                    setX(model.getPlayerOne().getX());
                    setY(model.getPlayerOne().getY());
                }
            }
        };
        
        stage.addActor(animatedSprite);
        animatedSprite.setSize(PLAYER_SIZE, PLAYER_SIZE);

        if (model.getPlayerOne() != null && model.getPlayerOne().getX() == 0) {
            Vector2 spawn = level.playerSpawnWorldPosition();
            model.getPlayerOne().setX(spawn.x - PLAYER_SIZE / 2f);
            model.getPlayerOne().setY(spawn.y - PLAYER_Y_OFFSET);
        }
        if (model.getPlayerOne() != null) {
            physicsFacade.initPlayerBody(model.getPlayerOne());
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
        Player player = model.getPlayerOne();
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();

        // 1. Aggiorna lo stato del gioco e i controlli (Controller)
        controller.update(delta);

        // 2. Aggiorna la simulazione fisica (Fisica)
        if (player != null && physicsFacade != null) {
            physicsFacade.update(delta, player);

            // Riproduce il suono di attacco all'inizio dello stato ATTACKING
            if (player.getState() == CharacterState.ATTACKING && lastState != CharacterState.ATTACKING) {
                if (playerAssets.getAttackSound() != null) {
                    playerAssets.getAttackSound().play();
                }
            }
            lastState = player.getState();
        }

        // Gestione della telecamera
        if (player != null) {
            cam.zoom = CAMERA_ZOOM;

            float targetCamX = player.getX() + PLAYER_SIZE / 2f;
            float targetCamY = player.getY() + PLAYER_SIZE / 2f;

            float halfViewportWidth  = (stage.getViewport().getWorldWidth()  * cam.zoom) / 2f;
            float halfViewportHeight = (stage.getViewport().getWorldHeight() * cam.zoom) / 2f;

            float minCamX = mapBounds.x + halfViewportWidth  + cameraEdgePadding;
            float maxCamX = mapBounds.x + mapBounds.width - halfViewportWidth  - cameraEdgePadding;
            float minCamY = mapBounds.y + halfViewportHeight + cameraEdgePadding;
            float maxCamY = mapBounds.y + mapBounds.height - halfViewportHeight - cameraEdgePadding;

            targetCamX = clamp(targetCamX, minCamX, maxCamX);
            targetCamY = clamp(targetCamY, minCamY, maxCamY);

            cam.position.set(targetCamX, targetCamY, 0);
            cam.update();
        }

        // 3. Rendering (View)
        ScreenUtils.clear(76f / 255f, 126f / 255f, 62f / 255f, 1f);

        mapRenderer.setView((OrthographicCamera) stage.getCamera());
        mapRenderer.render();
        
        if (drawObstacleDebug && physicsFacade != null) {
            physicsFacade.drawDebug(cam);
        }
        
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

        if (playerAssets != null) {
            playerAssets.dispose();
        }
        if (physicsFacade != null) {
            physicsFacade.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (map != null) {
            map.dispose();
        }
    }

    private float clamp(float value, float min, float max) {
        if (min > max) {
            return (min + max) / 2f;
        }
        return Math.max(min, Math.min(max, value));
    }
}
