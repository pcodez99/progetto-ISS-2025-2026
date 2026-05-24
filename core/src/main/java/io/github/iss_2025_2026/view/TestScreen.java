package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
    private final InputAdapter inputListener;

    private Stage stage;
    private Skin skin;
    private Table root;
    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;
    private World physicsWorld;
    private Body playerBody;
    private float mapMinX;
    private float mapMaxX;
    private float mapMinY;
    private float mapMaxY;
    private int mapWidth;
    private int mapHeight;
    private int tileWidth;
    private int tileHeight;
    private Map<Direction, Animation<TextureRegion>> childIdleAnims;
    private Map<Direction, Animation<TextureRegion>> childWalkAnims;
    private float stateTime;

    private Animation<TextureRegion> attackAnim;
    private com.badlogic.gdx.audio.Sound attackSound;
    private float attackStateTime = -1f;
    private float attackDuration = 0f;

    private final java.util.List<Texture> loadedTextures = new java.util.ArrayList<>();

    private static final String MAP_PATH = "map/generated/level_01_countryside_crash.tmx";
    private static final float PLAYER_SIZE = 160f;
    private static final float CAMERA_ZOOM = 0.85f;
    private static final float PPM = 32f;

    public TestScreen(Main game, GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.skin = GameUiTheme.loadSkin();
        loadMap();

        // Inizializziamo le mappe delle animazioni
        childIdleAnims = new EnumMap<>(Direction.class);
        childWalkAnims = new EnumMap<>(Direction.class);

        // Determiniamo il path base a seconda del personaggio selezionato
        Player player = model.getPlayerOne();
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

        // Carichiamo le animazioni di movimento per le 4 direzioni (con fallback ad
        // Idle per chi non cammina)
        Animation<TextureRegion> walkRight = JsonAnimationLoader.load(basePath + "/iso_walk_right_right", 0.06f,
                loadedTextures, false);
        if (walkRight == null) {
            walkRight = JsonAnimationLoader.load(basePath + "/iso_idle_right_right", 0.06f, loadedTextures, false);
            if (walkRight == null) {
                walkRight = JsonAnimationLoader.load(basePath + "/idle_right", 0.06f, loadedTextures, false);
            }
        }

        Animation<TextureRegion> walkLeft = null;
        if (walkRight != null) {
            walkLeft = JsonAnimationLoader.load(basePath + "/iso_walk_right_right", 0.06f, loadedTextures, true);
            if (walkLeft == null) {
                walkLeft = JsonAnimationLoader.load(basePath + "/iso_idle_right_right", 0.06f, loadedTextures, true);
                if (walkLeft == null) {
                    walkLeft = JsonAnimationLoader.load(basePath + "/idle_right", 0.06f, loadedTextures, true);
                }
            }
        }

        Animation<TextureRegion> walkUp = JsonAnimationLoader.load(basePath + "/iso_walk_up_right", 0.06f,
                loadedTextures, false);
        if (walkUp == null) {
            walkUp = JsonAnimationLoader.load(basePath + "/iso_idle_up_right", 0.06f, loadedTextures, false);
            if (walkUp == null) {
                walkUp = walkRight;
            }
        }

        Animation<TextureRegion> walkDown = JsonAnimationLoader.load(basePath + "/iso_walk_down_right", 0.06f,
                loadedTextures, false);
        if (walkDown == null) {
            walkDown = JsonAnimationLoader.load(basePath + "/iso_idle_down_right", 0.06f, loadedTextures, false);
            if (walkDown == null) {
                walkDown = walkRight;
            }
        }

        if (walkRight != null)
            childWalkAnims.put(Direction.RIGHT, walkRight);
        if (walkLeft != null)
            childWalkAnims.put(Direction.LEFT, walkLeft);
        if (walkUp != null)
            childWalkAnims.put(Direction.UP, walkUp);
        if (walkDown != null)
            childWalkAnims.put(Direction.DOWN, walkDown);

        // Carichiamo le animazioni dedicate di Idle, se esistono, altrimenti usiamo il
        // primo frame del walk
        Animation<TextureRegion> idleRight = JsonAnimationLoader.load(basePath + "/iso_idle_right_right", 0.1f,
                loadedTextures, false);
        if (idleRight == null) {
            idleRight = JsonAnimationLoader.load(basePath + "/idle_right", 0.1f, loadedTextures, false);
        }

        Animation<TextureRegion> idleLeft = JsonAnimationLoader.load(basePath + "/iso_idle_right_right", 0.1f,
                loadedTextures, true);
        if (idleLeft == null) {
            idleLeft = JsonAnimationLoader.load(basePath + "/idle_right", 0.1f, loadedTextures, true);
        }

        Animation<TextureRegion> idleUp = JsonAnimationLoader.load(basePath + "/iso_idle_up_right", 0.1f,
                loadedTextures, false);
        Animation<TextureRegion> idleDown = JsonAnimationLoader.load(basePath + "/iso_idle_down_right", 0.1f,
                loadedTextures, false);

        if (idleRight != null)
            childIdleAnims.put(Direction.RIGHT, idleRight);
        else
            setupIdleAnimation(Direction.RIGHT, walkRight);

        if (idleLeft != null)
            childIdleAnims.put(Direction.LEFT, idleLeft);
        else
            setupIdleAnimation(Direction.LEFT, walkLeft);

        if (idleUp != null)
            childIdleAnims.put(Direction.UP, idleUp);
        else
            setupIdleAnimation(Direction.UP, walkUp);

        if (idleDown != null)
            childIdleAnims.put(Direction.DOWN, idleDown);
        else
            setupIdleAnimation(Direction.DOWN, walkDown);

        // Carichiamo l'animazione di attacco
        attackAnim = JsonAnimationLoader.load(basePath + "/attack_right", 0.04f, loadedTextures, false);
        if (attackAnim != null) {
            attackDuration = attackAnim.getAnimationDuration();
        }

        // Carichiamo il suono di attacco specifico
        String characterId = player != null ? player.getCharacterId() : "bambino";
        String soundSuffix = getSoundSuffix(characterId);
        String soundPath = basePath + "/attack_right/attack_" + soundSuffix + ".mp3";
        if (Gdx.files.internal(soundPath).exists()) {
            attackSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        } else {
            Gdx.app.log("TestScreen", "Suono di attacco non trovato al path: " + soundPath);
        }

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

    private String getSoundSuffix(String characterId) {
        if ("papa".equals(characterId))
            return "father";
        if ("bambino".equals(characterId))
            return "child";
        if ("mamma".equals(characterId))
            return "mom";
        if ("nonno".equals(characterId))
            return "nonno";
        return "child";
    }

    private void setupIdleAnimation(Direction dir, Animation<TextureRegion> walkAnim) {
        if (walkAnim != null) {
            TextureRegion idleFrame = walkAnim.getKeyFrame(0f);
            if (idleFrame != null) {
                childIdleAnims.put(dir, new Animation<>(0.1f, idleFrame));
            }
        }
    }

    private void loadMap() {
        Box2D.init();
        map = new TmxMapLoader().load(MAP_PATH);
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
        physicsWorld = new World(new Vector2(0f, 0f), true);

        MapProperties properties = map.getProperties();
        mapWidth = properties.get("width", Integer.class);
        mapHeight = properties.get("height", Integer.class);
        tileWidth = properties.get("tilewidth", Integer.class);
        tileHeight = properties.get("tileheight", Integer.class);

        mapMinX = 0f;
        mapMaxX = (mapWidth + mapHeight) * tileWidth / 2f;
        mapMinY = -mapWidth * tileHeight / 2f;
        mapMaxY = mapHeight * tileHeight / 2f + PLAYER_SIZE;
        createRigidBodies();
    }

    private void createRigidBodies() {
        if (map.getLayers().get("RigidBodies") == null) return;

        MapObjects objects = map.getLayers().get("RigidBodies").getObjects();
        for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
            Rectangle rectangle = rectangleObject.getRectangle();
            MapProperties properties = rectangleObject.getProperties();
            float x = propertyFloat(properties, "worldX", rectangle.x);
            float y = propertyFloat(properties, "worldY", rectangle.y);
            float width = propertyFloat(properties, "worldWidth", rectangle.width);
            float height = propertyFloat(properties, "worldHeight", rectangle.height);
            createStaticBox(
                    x + width / 2f,
                    y + height / 2f,
                    width,
                    height,
                    rectangleObject.getName());
        }
    }

    private float propertyFloat(MapProperties properties, String name, float fallback) {
        Object value = properties.get(name);
        if (value instanceof Number) return ((Number) value).floatValue();
        if (value instanceof String) return Float.parseFloat((String) value);
        return fallback;
    }

    private void createStaticBox(float centerX, float centerY, float width, float height, String name) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(centerX / PPM, centerY / PPM);

        Body body = physicsWorld.createBody(bodyDef);
        body.setUserData(name);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / PPM, height / 2f / PPM);
        body.createFixture(shape, 0f);
        shape.dispose();
    }

    private void createPlayerBody(Player player) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set((player.getX() + PLAYER_SIZE / 2f) / PPM, (player.getY() + PLAYER_SIZE * 0.28f) / PPM);

        playerBody = physicsWorld.createBody(bodyDef);
        playerBody.setUserData("player");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(24f / PPM, 28f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Keep root table instance initialized, but empty of game objects so it doesn't wash them out
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Custom actor for player animation
        Image animatedSprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Player player = model.getPlayerOne();
                if (player == null)
                    return;

                Direction dir = player.getDirection();
                TextureRegion frame = null;

                float drawX = getX();
                float drawY = getY();
                float drawWidth = getWidth();
                float drawHeight = getHeight();

                if (attackStateTime >= 0 && attackAnim != null) {
                    frame = attackAnim.getKeyFrame(attackStateTime, false);
                    if (dir == Direction.LEFT) {
                        TextureRegion flippedFrame = new TextureRegion(frame);
                        flippedFrame.flip(true, false);
                        frame = flippedFrame;
                    }
                    
                    // The child character's attack sprites are drawn extremely large in the raw PNGs.
                    // Scaling down to 45% (0.45f) and centering perfectly aligns it to the idle size!
                    if ("bambino".equals(player.getCharacterId())) {
                        drawWidth = getWidth() * 0.45f;
                        drawHeight = getHeight() * 0.45f;
                        drawX = getX() + (getWidth() - drawWidth) / 2f;
                        drawY = getY(); // Keep the feet aligned on the ground
                    }
                } else {
                    boolean isMoving = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.A)
                            || Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.D);

                    Animation<TextureRegion> currentAnim = isMoving ? childWalkAnims.get(dir) : childIdleAnims.get(dir);

                    if (currentAnim != null) {
                        frame = currentAnim.getKeyFrame(stateTime, true);
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
                // Synchronize position with player model coordinates
                if (model.getPlayerOne() != null) {
                    setX(model.getPlayerOne().getX());
                    setY(model.getPlayerOne().getY());
                }
            }
        };
        
        // Add character directly to stage so it renders on top of the background
        stage.addActor(animatedSprite);
        animatedSprite.setSize(PLAYER_SIZE, PLAYER_SIZE); // Professional, balanced size

        // Place the player near the beginning of the level 1 path.
        if (model.getPlayerOne() != null && model.getPlayerOne().getX() == 0) {
            float[] spawn = tileToWorld(6, 28);
            model.getPlayerOne().setX(spawn[0]);
            model.getPlayerOne().setY(spawn[1]);
        }
        if (model.getPlayerOne() != null) {
            createPlayerBody(model.getPlayerOne());
        }
    }

    private float[] tileToWorld(int tileX, int tileY) {
        return new float[] {
                (tileX + tileY) * tileWidth / 2f,
                (tileY - tileX) * tileHeight / 2f
        };
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
        float bodyX = playerBody != null ? playerBody.getPosition().x * PPM - PLAYER_SIZE / 2f : 0f;
        float bodyY = playerBody != null ? playerBody.getPosition().y * PPM - PLAYER_SIZE * 0.28f : 0f;

        // 1. Update logic (via Controller)
        controller.update(delta);
        stateTime += delta;

        if (player != null && playerBody != null) {
            float attemptedVelocityX = (player.getX() - bodyX) / Math.max(delta, 0.0001f);
            float attemptedVelocityY = (player.getY() - bodyY) / Math.max(delta, 0.0001f);
            playerBody.setLinearVelocity(attemptedVelocityX / PPM, attemptedVelocityY / PPM);
            physicsWorld.step(Math.min(delta, 1f / 30f), 6, 2);
            player.setX(playerBody.getPosition().x * PPM - PLAYER_SIZE / 2f);
            player.setY(playerBody.getPosition().y * PPM - PLAYER_SIZE * 0.28f);
        }

        // Gestione timer dell'attacco
        if (attackStateTime >= 0) {
            attackStateTime += delta;
            if (attackStateTime >= attackDuration) {
                attackStateTime = -1f; // fine attacco
            }
        }

        // Trigger attacco se l'utente preme SPAZIO o Z e non sta già attaccando
        if (attackAnim != null
                && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.Z))) {
            if (attackStateTime < 0) {
                attackStateTime = 0f;
                if (attackSound != null) {
                    attackSound.play();
                }
            }
        }

        // 2. Camera follow player with clamping inside the isometric countryside map
        if (player != null) {
            float minPlayerX = mapMinX;
            float maxPlayerX = mapMaxX - PLAYER_SIZE;
            float minPlayerY = mapMinY;
            float maxPlayerY = mapMaxY - PLAYER_SIZE;
            if (player.getX() < minPlayerX) player.setX(minPlayerX);
            if (player.getX() > maxPlayerX) player.setX(maxPlayerX);
            if (player.getY() < minPlayerY) player.setY(minPlayerY);
            if (player.getY() > maxPlayerY) player.setY(maxPlayerY);
            if (playerBody != null) {
                playerBody.setTransform((player.getX() + PLAYER_SIZE / 2f) / PPM,
                        (player.getY() + PLAYER_SIZE * 0.28f) / PPM, 0f);
            }

            OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
            cam.zoom = CAMERA_ZOOM;

            float targetCamX = player.getX() + PLAYER_SIZE / 2f;
            float targetCamY = player.getY() + PLAYER_SIZE / 2f;

            float halfViewportWidth = (stage.getViewport().getWorldWidth() * cam.zoom) / 2f;
            float halfViewportHeight = (stage.getViewport().getWorldHeight() * cam.zoom) / 2f;
            float minCamX = mapMinX + halfViewportWidth;
            float maxCamX = mapMaxX - halfViewportWidth;
            float minCamY = mapMinY + halfViewportHeight;
            float maxCamY = mapMaxY - halfViewportHeight;

            if (targetCamX < minCamX) targetCamX = minCamX;
            if (targetCamX > maxCamX) targetCamX = maxCamX;
            if (targetCamY < minCamY) targetCamY = minCamY;
            if (targetCamY > maxCamY) targetCamY = maxCamY;

            cam.position.set(targetCamX, targetCamY, 0);
            cam.update();
        }

        // 3. Render (View)
        ScreenUtils.clear(0.38f, 0.56f, 0.30f, 1f);

        mapRenderer.setView((OrthographicCamera) stage.getCamera());
        mapRenderer.render();
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

        // Dispose all textures loaded individually by JsonAnimationLoader
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        loadedTextures.clear();

        // Dispose attack sound
        if (attackSound != null) {
            attackSound.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (map != null) {
            map.dispose();
        }
        if (physicsWorld != null) {
            physicsWorld.dispose();
        }
    }
}
