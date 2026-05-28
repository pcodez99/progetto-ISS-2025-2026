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
import io.github.iss_2025_2026.map.LevelAssetResolvers;
import io.github.iss_2025_2026.map.LevelCatalog;
import io.github.iss_2025_2026.map.LevelDefinition;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.map.TmxLevelLoader;
import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.physics.PhysicsFacade;
import io.github.iss_2025_2026.service.GameProperties;
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.RunMusicManager;
import java.io.IOException;

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
    private PlayerAssets playerTwoAssets;
    
    private CharacterState lastStateP1 = CharacterState.IDLE;
    private CharacterState lastStateP2 = CharacterState.IDLE;
    private Rectangle mapBounds;
    private float cameraEdgePadding;

    private static final float DEFAULT_CAMERA_EDGE_PADDING = 320f;

    // Configurable game properties
    private final float playerSize;
    private final float playerYOffset;
    private final float cameraZoom;
    private boolean drawObstacleDebug;

    public TestScreen(Main game, GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.skin = GameUiTheme.loadSkin();
        
        // Carica le proprietà configurabili dal file properties
        this.playerSize = GameProperties.getFloat(GameProperties.KEY_PLAYER_SIZE, 160f);
        this.playerYOffset = this.playerSize * 0.48f;
        this.cameraZoom = GameProperties.getFloat(GameProperties.KEY_CAMERA_ZOOM, 0.72f);
        this.drawObstacleDebug = GameProperties.getBoolean(GameProperties.KEY_DRAW_PHYSICS_DEBUG, true);

        loadMap();

        Player player = model.getPlayerOne();
        this.playerAssets = new PlayerAssets(player);
        
        // Sincronizza la durata dell'attacco del modello con la durata dell'animazione caricata
        if (player != null && playerAssets.getAttackAnim() != null) {
            player.setAttackDuration(playerAssets.getAttackAnim().getAnimationDuration());
        }

        Player playerTwo = model.getPlayerTwo();
        if (model.isMultiplayerGame() && playerTwo != null) {
            this.playerTwoAssets = new PlayerAssets(playerTwo);
            if (playerTwoAssets.getAttackAnim() != null) {
                playerTwo.setAttackDuration(playerTwoAssets.getAttackAnim().getAnimationDuration());
            }
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
        LevelDefinition levelDefinition = loadDefaultLevelDefinition();
        level = TmxLevelLoader.load(levelDefinition);
        map = level.getMap();
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
        mapBounds = level.getGeometry().getBounds();
        cameraEdgePadding = level.getGeometry().mapPropertyFloat("camera_edge_padding", DEFAULT_CAMERA_EDGE_PADDING);
        
        // Inizializza la facciata fisica Box2D con le dimensioni configurate
        physicsFacade = new PhysicsFacade(level, playerSize, playerYOffset);
    }

    private LevelDefinition loadDefaultLevelDefinition() {
        try {
            return LevelCatalog.load(LevelAssetResolvers.gdx()).requireLevel(TmxMapContract.DEFAULT_LEVEL_ID);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossibile caricare il catalogo livelli runtime.", exception);
        }
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Aggiungi Giocatore 1
        Player p1 = model.getPlayerOne();
        if (p1 != null) {
            addPlayerActor(p1, playerAssets);
            if (p1.getX() == 0) {
                Vector2 spawn = level.playerSpawnWorldPosition();
                p1.setX(spawn.x - playerSize / 2f);
                p1.setY(spawn.y - playerYOffset);
            }
            physicsFacade.initPlayerBody(p1);
        }

        // Aggiungi Giocatore 2 se in modalità multiplayer
        if (model.isMultiplayerGame() && model.getPlayerTwo() != null) {
            Player p2 = model.getPlayerTwo();
            addPlayerActor(p2, playerTwoAssets);
            if (p2.getX() == 0) {
                Vector2 spawn = level.playerSpawnWorldPosition();
                // Spawn leggermente sfalsato per evitare sovrapposizione esatta
                p2.setX(spawn.x - playerSize / 2f + 50f);
                p2.setY(spawn.y - playerYOffset);
            }
            physicsFacade.initPlayerBody(p2);
        }
    }

    private void addPlayerActor(final Player player, final PlayerAssets assets) {
        // Actor personalizzato per il rendering delle animazioni del giocatore basato sullo State Pattern
        Image animatedSprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Direction dir = player.getDirection();
                CharacterState state = player.getState();
                TextureRegion frame = null;

                float drawX = getX();
                float drawY = getY();
                float drawWidth = getWidth();
                float drawHeight = getHeight();

                if (state == CharacterState.ATTACKING && assets.getAttackAnim() != null) {
                    frame = assets.getAttackAnim().getKeyFrame(player.getStateTime(), false);
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
                            ? assets.getWalkAnim(dir) 
                            : assets.getIdleAnim(dir);

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
                setX(player.getX());
                setY(player.getY());
            }
        };
        
        stage.addActor(animatedSprite);
        animatedSprite.setSize(playerSize, playerSize);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputListener);
        MenuMusicManager.pause();
        RunMusicManager.play();
    }

    @Override
    public void render(float delta) {
        Player p1 = model.getPlayerOne();
        Player p2 = model.getPlayerTwo();
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();

        // 1. Aggiorna lo stato del gioco e i controlli (Controller)
        controller.update(delta);

        // 2. Aggiorna la simulazione fisica (Fisica)
        if (physicsFacade != null) {
            if (p1 != null) {
                physicsFacade.setPlayerVelocity(p1, delta);
                
                // Suono attacco Player 1
                if (p1.getState() == CharacterState.ATTACKING && lastStateP1 != CharacterState.ATTACKING) {
                    if (playerAssets.getAttackSound() != null) {
                        playerAssets.getAttackSound().play();
                    }
                }
                lastStateP1 = p1.getState();
            }

            if (model.isMultiplayerGame() && p2 != null) {
                physicsFacade.setPlayerVelocity(p2, delta);

                // Suono attacco Player 2
                if (p2.getState() == CharacterState.ATTACKING && lastStateP2 != CharacterState.ATTACKING) {
                    if (playerTwoAssets != null && playerTwoAssets.getAttackSound() != null) {
                        playerTwoAssets.getAttackSound().play();
                    }
                }
                lastStateP2 = p2.getState();
            }

            physicsFacade.step(delta);
            physicsFacade.syncPlayerPositions();
        }

        // Gestione della telecamera (Midpoint dei due giocatori in Multiplayer)
        if (p1 != null) {
            cam.zoom = cameraZoom;

            float targetCamX, targetCamY;
            if (model.isMultiplayerGame() && p2 != null) {
                targetCamX = (p1.getX() + p2.getX()) / 2f + playerSize / 2f;
                targetCamY = (p1.getY() + p2.getY()) / 2f + playerSize / 2f;
            } else {
                targetCamX = p1.getX() + playerSize / 2f;
                targetCamY = p1.getY() + playerSize / 2f;
            }

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
        if (playerTwoAssets != null) {
            playerTwoAssets.dispose();
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
