package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.BattleController;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.factory.CollectibleConfigLoader;
import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.factory.EnemyFactory;
import io.github.iss_2025_2026.factory.YamlEnemyFactory;
import io.github.iss_2025_2026.factory.NpcFactory;
import io.github.iss_2025_2026.config.CollectibleCatalog;
import io.github.iss_2025_2026.map.LevelRuntime;
import io.github.iss_2025_2026.map.TmxCollectibleLoader;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.CharacterSheetModel;
import io.github.iss_2025_2026.model.DialogueSession;
import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.EvolutionResult;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
import io.github.iss_2025_2026.model.NpcHelpRequest;
import io.github.iss_2025_2026.model.NpcHelpRequestChoice;
import io.github.iss_2025_2026.model.NpcHelpRequestResult;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.physics.PhysicsFacade;
import io.github.iss_2025_2026.service.CheckpointService;
import io.github.iss_2025_2026.service.DialogueRequestContext;
import io.github.iss_2025_2026.service.EnemyEncounter;
import io.github.iss_2025_2026.service.EnemyEncounterService;
import io.github.iss_2025_2026.service.EvolutionService;
import io.github.iss_2025_2026.service.GameProperties;
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.NpcDialogueController;
import io.github.iss_2025_2026.service.NpcDialogueService;
import io.github.iss_2025_2026.service.NpcHelpRequestService;
import io.github.iss_2025_2026.service.NpcInteraction;
import io.github.iss_2025_2026.service.NpcInteractionService;
import io.github.iss_2025_2026.service.RunMusicManager;
import io.github.iss_2025_2026.service.SaveResult;
import io.github.iss_2025_2026.service.CollectibleService;
import io.github.iss_2025_2026.service.tts.NpcSpeechService;
import io.github.iss_2025_2026.view.collectibles.CollectibleRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Game View (Parte del pattern MVC).
 * Implementazione di {@link Screen} di LibGDX.
 * Si occupa esclusivamente del rendering dello stato del Model.
 * Delega la logica di aggiornamento al Controller, la fisica a PhysicsFacade e
 * gli asset a PlayerAssets.
 */
public class LevelScreen implements Screen {
    private static final Logger LOGGER = Logger.getLogger(LevelScreen.class.getName());
    private static final float DEFAULT_ENCOUNTER_RADIUS = 130f;
    private static final float DEFAULT_NPC_INTERACTION_RADIUS = 190f;
    private static final float ENEMY_MAP_SPRITE_SCALE = 1.05f;
    private static final String KEY_NPC_SIZE = "npc_size";

    private final Main game;
    private final GameContext gameContext;
    private final GameModel model;
    private final GameController controller;
    private final InputAdapter inputListener;
    private final LevelRuntime levelRuntime;

    private Stage stage;
    private Stage uiStage;
    private Skin skin;
    private Table root;
    private Table saveToast;
    private Label saveStatusLabel;
    private Table dialogueRoot;
    private Table dialoguePanel;
    private Label dialogueSpeakerLabel;
    private Label dialogueTextLabel;
    private Label dialogueHintLabel;
    private TextField dialogueInputField;
    private Cell<TextField> dialogueInputCell;
    private Table dialogueChoiceTable;
    private Cell<Table> dialogueChoiceCell;
    private float saveStatusTimer;
    private PlayerHud playerOneHud;
    private PlayerHud playerTwoHud;
    private TmxLevel level;
    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;

    // Design Patterns delegation
    private PhysicsFacade physicsFacade;
    private CheckpointService checkpointService;
    private EnemyEncounterService encounterService;
    private NpcDialogueController dialogueController;
    private NpcInteractionService npcInteractionService;
    private NpcDialogueService npcDialogueService;
    private NpcSpeechService npcSpeechService;
    private NpcHelpRequestService npcHelpRequestService;
    private EvolutionService evolutionService;
    private PlayerAssets playerAssets;
    private PlayerAssets playerTwoAssets;
    private final Map<String, NpcAssets> npcAssetsById = new HashMap<>();

    private CollectibleService collectibleService;
    private CollectibleRenderer collectibleRenderer;
    private Label pickupPromptLabel;
    private CharacterSheetModel characterSheetModel;

    private Rectangle mapBounds;
    private float cameraEdgePadding;
    private boolean battleTransitionPending;
    private float encounterCooldownTimer;
    private NpcInteraction nearbyNpcInteraction;
    private Player nearbyNpcPlayer;

    // Enemy sprites on the overworld map
    private float enemySpriteTime;
    private final Map<String, Animation<TextureRegion>> enemyIdleAnimCache = new HashMap<>();
    private final List<Texture> enemySpriteTextures = new ArrayList<>();
    private final List<Image> enemySpriteActors = new ArrayList<>();

    private static final float DEFAULT_CAMERA_EDGE_PADDING = 320f;

    // Configurable game properties
    private final float playerSize;
    private final float playerYOffset;
    private final float npcSize;
    private final float npcYOffset;
    private final float encounterRadius;
    private final float npcInteractionRadius;
    private final float cameraZoom;
    private boolean drawObstacleDebug;

    public LevelScreen(Main game, GameContext gameContext, GameModel model, GameController controller,
            LevelRuntime levelRuntime) {
        this.game = game;
        this.gameContext = gameContext;
        this.model = model;
        this.controller = controller;
        this.levelRuntime = levelRuntime;
        this.skin = GameUiTheme.loadSkin();

        // Carica le proprietà configurabili dal file properties
        this.playerSize = GameProperties.getFloat(GameProperties.KEY_PLAYER_SIZE, 160f);
        this.playerYOffset = this.playerSize * 0.48f;
        this.npcSize = GameProperties.getFloat(KEY_NPC_SIZE, 150f);
        this.npcYOffset = this.npcSize * 0.48f;
        this.encounterRadius = GameProperties.getFloat(GameProperties.KEY_ENEMY_ENCOUNTER_RADIUS,
                DEFAULT_ENCOUNTER_RADIUS);
        this.npcInteractionRadius = GameProperties.getFloat(GameProperties.KEY_NPC_INTERACTION_RADIUS,
                DEFAULT_NPC_INTERACTION_RADIUS);
        this.cameraZoom = GameProperties.getFloat(GameProperties.KEY_CAMERA_ZOOM, 0.72f);
        this.drawObstacleDebug = GameProperties.getBoolean(GameProperties.KEY_DRAW_PHYSICS_DEBUG, true);

        configureLevel(levelRuntime);

        Player player = model.getPlayerOne();
        this.playerAssets = new PlayerAssets(player);

        Player playerTwo = model.getPlayerTwo();
        if (model.isMultiplayerGame() && playerTwo != null) {
            this.playerTwoAssets = new PlayerAssets(playerTwo);
        }

        buildUI();
        this.checkpointService = new CheckpointService(
                model, levelRuntime, playerSize / 2f, playerYOffset);
        showInitialStatus();

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (isDialogueBlockingGameplay()) {
                        closeDialogue();
                        return true;
                    }
                    game.setScreen(new MainMenuScreen(game, model, controller, true, LevelScreen.this));
                    return true;
                }
                if (keycode == Input.Keys.E) {
                    if (!isDialogueBlockingGameplay() && handleDialogueAction()) {
                        return true;
                    }
                }
                if (isHelpRequestPending() && keycode == Input.Keys.A) {
                    resolveHelpRequest(NpcHelpRequestChoice.ACCEPT);
                    return true;
                }
                if (isHelpRequestPending() && keycode == Input.Keys.R) {
                    resolveHelpRequest(NpcHelpRequestChoice.REFUSE);
                    return true;
                }
                if (keycode == Input.Keys.F3) {
                    drawObstacleDebug = !drawObstacleDebug;
                    return true;
                }
                // Apri la schermata personaggio — vietato durante il combattimento
                if (keycode == Input.Keys.I) {
                    if (characterSheetModel != null
                            && characterSheetModel.canOpenDuringPhase(model.getGameState().getPhase())) {
                        game.setScreen(new CharacterSheetScreen(game, characterSheetModel, LevelScreen.this));
                    }
                    return true;
                }

                return false;
            }
        };
    }

    private void configureLevel(LevelRuntime levelRuntime) {
        level = levelRuntime.getLevel();
        map = level.getMap();
        if (levelRuntime.getId() == 2) {
            com.badlogic.gdx.maps.MapLayer layer = map.getLayers().get("Immagini");
            if (layer != null) {
                layer.setVisible(false);
            }
        }
        mapRenderer = new IsometricTiledMapRenderer(map, 1f) {
            @Override
            public void renderObject(com.badlogic.gdx.maps.MapObject object) {
                if (levelRuntime.getId() == 2
                        && object instanceof com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) {
                    com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject tileObj = (com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) object;
                    com.badlogic.gdx.maps.tiled.TiledMapTile tile = tileObj.getTile();
                    if (tile != null) {
                        TextureRegion region = tile.getTextureRegion();
                        if (region != null) {
                            float mapTileWidth = map.getProperties().get("tilewidth", Integer.class);
                            float mapTileHeight = map.getProperties().get("tileheight", Integer.class);

                            float tileX = tileObj.getX() / mapTileHeight - 0.5f;
                            float tileY = tileObj.getY() / mapTileHeight + 0.5f;

                            float objectWidth = io.github.iss_2025_2026.map.IsoMapGeometry.propertyFloat(
                                    tileObj.getProperties(), "width", region.getRegionWidth()) * unitScale;
                            float objectHeight = io.github.iss_2025_2026.map.IsoMapGeometry.propertyFloat(
                                    tileObj.getProperties(), "height", region.getRegionHeight()) * unitScale;

                            float drawX = (tileX + tileY) * (mapTileWidth / 2f) * unitScale
                                    - objectWidth * 0.5f + tile.getOffsetX() * unitScale;
                            float drawY = (tileY - tileX) * (mapTileHeight / 2f) * unitScale
                                    + tile.getOffsetY() * unitScale;

                            float scaleX = tileObj.getScaleX();
                            float scaleY = tileObj.getScaleY();
                            float rotation = tileObj.getRotation();

                            float halfWidth = objectWidth * 0.5f;
                            float halfHeight = objectHeight * 0.5f;

                            getBatch().draw(
                                    region,
                                    drawX,
                                    drawY,
                                    halfWidth,
                                    halfHeight,
                                    objectWidth,
                                    objectHeight,
                                    scaleX,
                                    scaleY,
                                    rotation);
                        }
                    }
                }
            }
        };
        mapBounds = level.getGeometry().getBounds();
        cameraEdgePadding = level.getGeometry().mapPropertyFloat("camera_edge_padding", DEFAULT_CAMERA_EDGE_PADDING);

        EnemyFactory enemyFactory = new YamlEnemyFactory();
        encounterService = new EnemyEncounterService(
                level.enemyObjects(), enemyFactory, level.getGeometry(), encounterRadius);
        NpcFactory npcFactory = new NpcFactory();
        npcDialogueService = new NpcDialogueService();
        npcSpeechService = new NpcSpeechService();
        dialogueController = new NpcDialogueController();
        evolutionService = new EvolutionService();
        CollectibleCatalog collectibleCatalog = CollectibleConfigLoader.loadDefault();
        CollectibleFactory collectibleFactory = new CollectibleFactory(collectibleCatalog);
        npcHelpRequestService = new NpcHelpRequestService(collectibleFactory, evolutionService);
        npcHelpRequestService.validateConfiguration(npcFactory.getAllNpcs());
        npcInteractionService = new NpcInteractionService(
                level.npcObjects(), npcFactory, level.getGeometry(), levelRuntime.getId(), npcInteractionRadius);

        // Inizializza la facciata fisica Box2D con le dimensioni configurate
        physicsFacade = new PhysicsFacade(level, playerSize, playerYOffset);

        collectibleService = new CollectibleService(
                new TmxCollectibleLoader(collectibleFactory).load(level, levelRuntime.getId()),
                playerSize / 2f,
                playerYOffset);
        collectibleRenderer = new CollectibleRenderer(collectibleCatalog.getVisualConfigs());
        collectibleService.setPickupListener((collectible,
                player) -> showSaveStatus(player.getName() + " ha raccolto " + collectible.getName() + "!", false));

        Player playerOne = model.getPlayerOne();
        if (playerOne != null) {
            characterSheetModel = new CharacterSheetModel(playerOne);
        }

        // Pre-carica le animazioni idle nemico per gli sprite sulla mappa
        preloadEnemyIdleAnimations();
    }

    /**
     * Pre-carica le animazioni idle per ogni tipo di nemico presente nei punti di
     * spawn attivi.
     */
    private void preloadEnemyIdleAnimations() {
        if (encounterService == null) {
            return;
        }
        for (EnemyEncounterService.EnemyEncounterInfo info : encounterService.getAllEncounterInfo()) {
            String enemyType = info.getEnemyType();
            if (!enemyIdleAnimCache.containsKey(enemyType)) {
                String spritePath = resolveOverworldEnemyPath(enemyType);
                Animation<TextureRegion> anim = JsonAnimationLoader.load(
                        spritePath + "/idle_right", 0.1f, enemySpriteTextures, true);
                if (anim != null) {
                    enemyIdleAnimCache.put(enemyType, anim);
                }
            }
        }
    }

    /**
     * Mappa l'ID del tipo nemico al percorso della cartella sprite overworld
     * corrispondente.
     */
    private static String resolveOverworldEnemyPath(String enemyType) {
        if ("alieno_guardiano".equals(enemyType)
                || "boss_livello_1".equals(enemyType)
                || "boss_livello_2".equals(enemyType)) {
            return "characters/enemy-alien-1";
        }
        // Default: alieno_sciame, alieno_base, boss_livello_3, ecc.
        return "characters/enemy-alien-2";
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        root = new Table();
        root.setFillParent(true);
        root.top().left();
        uiStage.addActor(root);

        saveToast = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        saveToast.setVisible(false);
        saveStatusLabel = new Label("", skin, GameUiTheme.LABEL_BODY);
        saveStatusLabel.setWrap(true);
        saveToast.add(saveStatusLabel).growX();
        root.add(saveToast).left().top().pad(GameUiTheme.SPACE_3).width(420f);
        root.add().expandX().row();

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

        addNpcActors();

        // Aggiungi sprite idle nemici ai punti di spawn
        addEnemySpritesToMap();

        // Aggiungi attori per gli oggetti della mappa per consentire il corretto Y-sorting
        addMapObjectActors();

        // HUD: crea e aggiungi alla UI (player1 sinistra, player2 destra)
        if (model.getPlayerOne() != null) {
            playerOneHud = new PlayerHud(model.getPlayerOne(), skin);
            root.add(playerOneHud.getTable()).left().top().pad(GameUiTheme.SPACE_3);
        }
        root.add().expandX();
        if (model.isMultiplayerGame() && model.getPlayerTwo() != null) {
            playerTwoHud = new PlayerHud(model.getPlayerTwo(), skin);
            root.add(playerTwoHud.getTable()).right().top().pad(GameUiTheme.SPACE_3);
        }

        pickupPromptLabel = new Label("Premi E per raccogliere", skin, GameUiTheme.LABEL_BODY);
        pickupPromptLabel.setVisible(false);
        uiStage.addActor(pickupPromptLabel);

        buildDialoguePanel();
    }

    private void buildDialoguePanel() {
        dialogueRoot = new Table();
        dialogueRoot.setFillParent(true);
        dialogueRoot.bottom().left().pad(GameUiTheme.SPACE_3);
        uiStage.addActor(dialogueRoot);

        dialoguePanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_3);
        dialoguePanel.setVisible(false);
        dialoguePanel.defaults().left().growX();

        dialogueSpeakerLabel = new Label("", skin, GameUiTheme.LABEL_SECTION);
        dialogueTextLabel = new Label("", skin, GameUiTheme.LABEL_BODY);
        dialogueTextLabel.setWrap(true);
        dialogueHintLabel = new Label("", skin, GameUiTheme.LABEL_TAG);
        dialogueHintLabel.setWrap(true);
        dialogueInputField = new TextField("", skin, GameUiTheme.TEXT_FIELD_GAME);
        dialogueInputField.setMessageText("Scrivi cosa vuoi dire...");
        dialogueInputField.setMaxLength(NpcDialogueController.DEFAULT_MAX_USER_INPUT_CHARS);
        dialogueInputField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    submitDialogueInput();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    closeDialogue();
                    return true;
                }
                return false;
            }
        });
        dialogueInputField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') {
                    submitDialogueInput();
                }
            }
        });

        dialogueChoiceTable = new Table();
        TextButton acceptButton = GameUiFactory.createButton("[A] Accetta", skin, GameUiTheme.BUTTON_PRIMARY);
        TextButton refuseButton = GameUiFactory.createButton("[R] Rifiuta", skin, GameUiTheme.BUTTON_GHOST);
        acceptButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveHelpRequest(NpcHelpRequestChoice.ACCEPT);
            }
        });
        refuseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveHelpRequest(NpcHelpRequestChoice.REFUSE);
            }
        });
        dialogueChoiceTable.add(acceptButton).height(48f).growX().padRight(GameUiTheme.SPACE_2);
        dialogueChoiceTable.add(refuseButton).height(48f).growX();

        dialoguePanel.add(dialogueSpeakerLabel).padBottom(GameUiTheme.SPACE_1).row();
        dialoguePanel.add(dialogueTextLabel).growX().padBottom(GameUiTheme.SPACE_2).row();
        dialogueInputCell = dialoguePanel.add(dialogueInputField).growX().height(0f).padBottom(0f);
        dialoguePanel.row();
        dialogueChoiceCell = dialoguePanel.add(dialogueChoiceTable).growX().height(0f).padBottom(0f);
        dialoguePanel.row();
        dialoguePanel.add(dialogueHintLabel).growX();
        setDialogueInputVisible(false);
        setDialogueChoiceVisible(false);

        dialogueRoot.add(dialoguePanel).growX().minWidth(320f).maxWidth(760f);
        updateDialoguePanelLayout();
    }

    /**
     * Crea e aggiunge gli attori sprite animati dei nemici ai loro punti di spawn
     * sulla mappa.
     */
    private void addEnemySpritesToMap() {
        if (encounterService == null) {
            return;
        }
        float enemySpriteSize = playerSize * ENEMY_MAP_SPRITE_SCALE;
        for (EnemyEncounterService.EnemyEncounterInfo info : encounterService.getAllEncounterInfo()) {
            Animation<TextureRegion> idleAnim = enemyIdleAnimCache.get(info.getEnemyType());
            if (idleAnim == null) {
                continue;
            }
            final float spawnX = info.getX() - enemySpriteSize / 2f;
            final float spawnY = info.getY() - enemySpriteSize * 0.48f;
            final Animation<TextureRegion> anim = idleAnim;

            Image enemySprite = new Image() {
                @Override
                public void draw(Batch batch, float parentAlpha) {
                    TextureRegion frame = anim.getKeyFrame(enemySpriteTime, true);
                    if (frame != null) {
                        batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(),
                                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
                    }
                }
            };
            enemySprite.setPosition(spawnX, spawnY);
            enemySprite.setSize(enemySpriteSize, enemySpriteSize);
            stage.addActor(enemySprite);
            enemySpriteActors.add(enemySprite);
        }
    }

    private void addPlayerActor(final Player player, final PlayerAssets assets) {
        // Actor personalizzato per il rendering delle animazioni del giocatore basato
        // sullo State Pattern
        Image animatedSprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Direction dir = player.getDirection();
                CharacterState state = player.getState();
                TextureRegion frame = null;

                Animation<TextureRegion> currentAnim = (state == CharacterState.WALKING)
                        ? assets.getWalkAnim(dir)
                        : assets.getIdleAnim(dir);

                if (currentAnim != null) {
                    frame = currentAnim.getKeyFrame(player.getStateTime(), true);
                }

                if (frame != null) {
                    batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
                            getScaleX(), getScaleY(), getRotation());
                }
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                // Sincronizza la posizione dell'attore con le coordinate del modello del
                // giocatore
                setX(player.getX());
                setY(player.getY());
            }
        };

        stage.addActor(animatedSprite);
        animatedSprite.setSize(playerSize, playerSize);
    }

    private void addNpcActors() {
        if (npcInteractionService == null) {
            return;
        }
        List<NpcInteraction> interactions = npcInteractionService.getInteractions();
        for (NpcInteraction interaction : interactions) {
            if (interaction == null || interaction.getNpc() == null || interaction.getNpc().getId() == null) {
                continue;
            }

            Npc npc = interaction.getNpc();
            NpcAssets assets = npcAssetsById.get(npc.getId());
            if (assets == null) {
                assets = new NpcAssets(npc);
                npcAssetsById.put(npc.getId(), assets);
            }
            if (!assets.isAvailable()) {
                LOGGER.warning("[NPC UI] Sprite NPC non trovato per npcId=" + npc.getId());
                continue;
            }

            stage.addActor(new NpcWorldActor(interaction, assets, npcSize, npcYOffset));
        }
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(inputListener);
        Gdx.input.setInputProcessor(multiplexer);
        battleTransitionPending = false;
        MenuMusicManager.pause();
        RunMusicManager.play();
    }

    @Override
    public void render(float delta) {
        Player p1 = model.getPlayerOne();
        Player p2 = model.getPlayerTwo();
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();

        // 1. Aggiorna lo stato del gioco e i controlli (Controller)
        if (isDialogueBlockingGameplay()) {
            model.update(delta);
        } else {
            controller.update(delta);
        }

        // 2. Aggiorna la simulazione fisica (Fisica)
        if (physicsFacade != null) {
            if (p1 != null && model.isMultiplayerGame() && p2 != null) {
                physicsFacade.setMultiplayerVelocities(p1, p2, delta);
            } else if (p1 != null) {
                physicsFacade.setPlayerVelocity(p1, delta);
            }

            physicsFacade.step(delta);
            physicsFacade.syncPlayerPositions();
        }

        if (encounterCooldownTimer > 0f) {
            encounterCooldownTimer -= delta;
        }

        if (!isDialogueBlockingGameplay() && encounterCooldownTimer <= 0f && !battleTransitionPending
                && encounterService != null && p1 != null) {
            EnemyEncounter encounter = encounterService.checkEncounter(p1);
            if (encounter == null && model.isMultiplayerGame() && p2 != null) {
                encounter = encounterService.checkEncounter(p2);
            }
            if (encounter != null && !encounter.getEnemies().isEmpty()) {
                startBattle(encounter, p1, p2);
                return;
            }
        }

        SaveResult checkpointSave = checkpointService != null
                ? checkpointService.pollCheckpointReachedEvent()
                : null;
        if (checkpointSave != null && checkpointSave.shouldNotifyPlayer()) {
            showSaveStatus(checkpointSave);
        }
        updateNpcDialoguePrompt(p1, p2);
        updateSaveStatus(delta);

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

            float halfViewportWidth = (stage.getViewport().getWorldWidth() * cam.zoom) / 2f;
            float halfViewportHeight = (stage.getViewport().getWorldHeight() * cam.zoom) / 2f;

            float minCamX = mapBounds.x + halfViewportWidth + cameraEdgePadding;
            float maxCamX = mapBounds.x + mapBounds.width - halfViewportWidth - cameraEdgePadding;
            float minCamY = mapBounds.y + halfViewportHeight + cameraEdgePadding;
            float maxCamY = mapBounds.y + mapBounds.height - halfViewportHeight - cameraEdgePadding;

            targetCamX = clamp(targetCamX, minCamX, maxCamX);
            targetCamY = clamp(targetCamY, minCamY, maxCamY);

            cam.position.set(targetCamX, targetCamY, 0);
            cam.update();
        }

        handleMapInteractions(p1, p2);

        // 3. Rendering (View)
        ScreenUtils.clear(76f / 255f, 126f / 255f, 62f / 255f, 1f);

        mapRenderer.setView((OrthographicCamera) stage.getCamera());
        mapRenderer.render();

        // Renderizza i collectibles a terra
        collectibleRenderer.update(delta);
        renderCollectibles(stage.getBatch());

        if (drawObstacleDebug && physicsFacade != null) {
            physicsFacade.drawDebug(cam);
        }

        // Aggiorna il timer animazione degli sprite nemici e la loro visibilità
        enemySpriteTime += delta;
        updateEnemySpriteVisibility();

        stage.act(delta);
        sortWorldActorsByDepth();
        stage.draw();
        // Aggiorna HUD prima di processare la UI
        if (playerOneHud != null)
            playerOneHud.update();
        if (playerTwoHud != null)
            playerTwoHud.update();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void handleMapInteractions(Player playerOne, Player playerTwo) {
        if (pickupPromptLabel == null || playerOne == null
                || isDialogueBlockingGameplay() || nearbyNpcInteraction != null) {
            if (pickupPromptLabel != null) {
                pickupPromptLabel.setVisible(false);
            }
            return;
        }

        CollectibleService.CollectibleOnMap collectible = collectibleService != null
                ? collectibleService.getClosestCollectible(playerOne)
                : null;
        Player interactingPlayer = playerOne;
        if (collectible == null && model.isMultiplayerGame() && playerTwo != null && collectibleService != null) {
            collectible = collectibleService.getClosestCollectible(playerTwo);
            interactingPlayer = playerTwo;
        }

        if (collectible != null) {
            String prefix = interactingPlayer == playerOne ? "Premi E" : "P2: Premi ENTER";
            showInteractionPrompt(prefix + " per raccogliere " + collectible.getCollectible().getName());
            if (interactionKeyPressed(interactingPlayer, playerOne)) {
                collectibleService.pickUp(interactingPlayer, collectible);
            }
            return;
        }

        pickupPromptLabel.setVisible(false);
    }

    private boolean interactionKeyPressed(Player interactingPlayer, Player playerOne) {
        if (interactingPlayer == playerOne) {
            return Gdx.input.isKeyJustPressed(Input.Keys.E);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0);
    }

    private void showInteractionPrompt(String text) {
        pickupPromptLabel.setText(text);
        pickupPromptLabel.setVisible(true);
        pickupPromptLabel.pack();
        pickupPromptLabel.setPosition(
                (stage.getViewport().getScreenWidth() - pickupPromptLabel.getWidth()) / 2f,
                80f);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        updateDialoguePanelLayout();
        if (pickupPromptLabel != null) {
            pickupPromptLabel.setPosition((width - pickupPromptLabel.getPrefWidth()) / 2f, 80f);
        }
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
        if (npcSpeechService != null) {
            npcSpeechService.close();
            npcSpeechService = null;
        }
        stage.dispose();
        uiStage.dispose();
        skin.dispose();

        if (playerAssets != null) {
            playerAssets.dispose();
        }
        if (playerTwoAssets != null) {
            playerTwoAssets.dispose();
        }
        for (NpcAssets assets : npcAssetsById.values()) {
            if (assets != null) {
                assets.dispose();
            }
        }
        npcAssetsById.clear();
        if (physicsFacade != null) {
            physicsFacade.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (map != null) {
            map.dispose();
        }
        if (dialogueController != null) {
            dialogueController.close();
        }
        // Dispose HUD shared resources
        if (playerOneHud != null) {
            playerOneHud.dispose();
            playerOneHud = null;
        }
        if (playerTwoHud != null) {
            playerTwoHud.dispose();
            playerTwoHud = null;
        }
        // Dispose enemy sprite textures
        for (Texture texture : enemySpriteTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        enemySpriteTextures.clear();
        enemyIdleAnimCache.clear();
        enemySpriteActors.clear();
        if (collectibleRenderer != null) {
            collectibleRenderer.dispose();
            collectibleRenderer = null;
        }
    }

    public void startEncounterCooldown(float seconds) {
        encounterCooldownTimer = Math.max(0f, seconds);
    }

    private void startBattle(EnemyEncounter encounter, Player p1, Player p2) {
        closeDialogue();
        battleTransitionPending = true;
        Player playerTwo = model.isMultiplayerGame() ? p2 : null;
        BattleModel battleModel = new BattleModel(p1, playerTwo, encounter.getEnemies());
        BattleController battleController = new BattleController(battleModel);
        model.setActiveBattleModel(battleModel);
        model.getGameState().setPhase(GameState.Phase.COMBAT);
        game.setScreen(new BattleScreen(game, gameContext, model, this, battleModel, battleController, encounter,
                encounterService));
    }

    private void updateNpcDialoguePrompt(Player p1, Player p2) {
        if (npcInteractionService == null || isDialogueBlockingGameplay()) {
            return;
        }

        nearbyNpcInteraction = null;
        nearbyNpcPlayer = null;

        NpcInteraction interaction = npcInteractionService.checkInteraction(p1);
        Player interactionPlayer = p1;
        if (interaction == null && model.isMultiplayerGame() && p2 != null) {
            interaction = npcInteractionService.checkInteraction(p2);
            interactionPlayer = p2;
        }

        if (interaction == null) {
            hideDialoguePrompt();
            return;
        }

        nearbyNpcInteraction = interaction;
        nearbyNpcPlayer = interactionPlayer;
        showDialoguePrompt(interaction.getNpc());
    }

    private boolean handleDialogueAction() {
        if (nearbyNpcInteraction == null) {
            return false;
        }

        Player player = nearbyNpcPlayer != null ? nearbyNpcPlayer : model.getPlayerOne();
        openDialogueSession(nearbyNpcInteraction, player);
        return true;
    }

    private void showDialoguePrompt(Npc npc) {
        if (dialoguePanel == null || npc == null) {
            return;
        }
        setDialogueInputVisible(false);
        setDialogueChoiceVisible(false);
        setDialoguePanelText(npc.getName(), "Premi E per parlare.", "E");
        dialoguePanel.setVisible(true);
    }

    private void hideDialoguePrompt() {
        if (dialoguePanel == null || isDialogueBlockingGameplay()) {
            return;
        }
        dialoguePanel.setVisible(false);
    }

    private void openDialogueSession(NpcInteraction interaction, Player player) {
        if (interaction == null || interaction.getNpc() == null) {
            return;
        }

        final Npc npc = interaction.getNpc().copy();
        Player dialoguePlayer = player != null ? player : model.getPlayerOne();
        dialogueController.open(dialoguePlayer, npc);
        LOGGER.info("[NPC UI] Sessione dialogo aperta: player="
                + (dialoguePlayer != null ? dialoguePlayer.getName() : "unknown")
                + ", npc=" + npc.getId() + " (" + npc.getName() + ")");
        setDialogueInputVisible(true);
        setDialogueChoiceVisible(false);
        dialogueInputField.setText("");
        dialogueInputField.setDisabled(false);
        uiStage.setKeyboardFocus(dialogueInputField);
        refreshDialogueSessionPanel(dialogueController.getStatusMessage());
        dialoguePanel.setVisible(true);
    }

    private void submitDialogueInput() {
        if (dialogueController == null || !dialogueController.isActive()
                || dialogueController.isWaitingForAi()
                || dialogueController.isEnded()) {
            return;
        }
        DialogueRequestContext requestContext = dialogueController.submitUserInput(dialogueInputField.getText());
        if (requestContext == null) {
            LOGGER.info("[NPC UI] Input dialogo ignorato: " + dialogueController.getStatusMessage());
            refreshDialogueSessionPanel(dialogueController.getStatusMessage());
            uiStage.setKeyboardFocus(dialogueInputField);
            return;
        }

        LOGGER.info("[NPC UI] Input utente inviato all'AI: sessionId=" + requestContext.getSessionId()
                + ", npc=" + requestContext.getNpc().getId()
                + ", text=" + requestContext.getUserInput());
        dialogueInputField.setText("");
        dialogueInputField.setDisabled(true);
        refreshDialogueSessionPanel(dialogueController.getStatusMessage());

        requestNpcDialogue(requestContext);
    }

    private void requestNpcDialogue(final DialogueRequestContext requestContext) {
        Thread dialogueThread = new Thread(() -> {
            NpcDialogueDecision dialogueDecision;
            try {
                dialogueDecision = npcDialogueService.getDialogueDecision(
                        requestContext.getPlayer(),
                        requestContext.getNpc(),
                        requestContext.getUserInput(),
                        requestContext.getHistory());
            } catch (RuntimeException exception) {
                LOGGER.log(Level.WARNING, "[NPC UI] Errore durante la richiesta AI del dialogo. Uso fallback.",
                        exception);
                dialogueDecision = NpcDialogueDecision.neutral(fallbackDialogue(requestContext.getNpc()));
            }

            final NpcDialogueDecision result = dialogueDecision;
            Gdx.app.postRunnable(() -> applyNpcDialogueResponse(requestContext, result));
        }, "npc-dialogue");
        dialogueThread.setDaemon(true);
        dialogueThread.start();
    }

    private void applyNpcDialogueResponse(DialogueRequestContext requestContext, NpcDialogueDecision result) {
        if (dialogueController == null
                || !dialogueController.completeNpcDecision(requestContext.getSessionId(), result)) {
            LOGGER.warning("[NPC UI] Risposta AI ignorata: sessione non piu attiva. sessionId="
                    + requestContext.getSessionId());
            return;
        }
        applyDialogueDecisionEffects(requestContext, result);
        speakNpcTurnsSince(requestContext.getHistory().size());
        LOGGER.info("[NPC UI] Risposta AI applicata alla sessione "
                + requestContext.getSessionId() + ": " + result.getReply());
        dialogueInputField.setDisabled(dialogueController.isEnded());
        refreshDialogueSessionPanel(dialogueController.getStatusMessage());
        if (dialogueController.isEnded() || dialogueController.isHelpRequestPending()) {
            uiStage.setKeyboardFocus(null);
        } else {
            uiStage.setKeyboardFocus(dialogueInputField);
        }
        if (dialoguePanel != null) {
            dialoguePanel.setVisible(true);
        }
    }

    private void applyDialogueDecisionEffects(DialogueRequestContext requestContext, NpcDialogueDecision decision) {
        if (requestContext == null || decision == null) {
            return;
        }

        Player player = requestContext.getPlayer();
        if (player != null && decision.getKarmaDelta() != 0) {
            int previousKarma = player.getKarma();
            player.modifyKarma(decision.getKarmaDelta());
            LOGGER.info("[NPC UI] Karma aggiornato da decisione AI: player=" + player.getName()
                    + ", delta=" + decision.getKarmaDelta()
                    + ", before=" + previousKarma
                    + ", after=" + player.getKarma());
        }

        ChoiceEventType eventType = decision.getChoiceEventType();
        if (player != null && eventType != null && evolutionService != null) {
            EvolutionResult result = evolutionService.applyNpcChoice(player, requestContext.getNpc(), eventType);
            LOGGER.info("[NPC UI] Evento evolutivo da dialogo AI: player=" + player.getName()
                    + ", npc=" + (requestContext.getNpc() != null ? requestContext.getNpc().getId() : "unknown")
                    + ", event=" + eventType
                    + ", path=" + result.getPreviousPath() + "->" + result.getCurrentPath()
                    + ", selectedAbility=" + result.getSelectedAbilitySlot()
                    + ", unlocked=" + result.getUnlockedSlots());
        }

        if (decision.isBadBehavior()) {
            LOGGER.info("[NPC UI] Comportamento negativo rilevato dall'AI: reason="
                    + (decision.getReason() != null ? decision.getReason() : ""));
        }
    }

    private void closeDialogue() {
        if (npcSpeechService != null) {
            npcSpeechService.stop();
        }
        if (dialogueController != null) {
            dialogueController.close();
        }
        if (uiStage != null) {
            uiStage.setKeyboardFocus(null);
        }
        if (dialogueInputField != null) {
            dialogueInputField.setText("");
            dialogueInputField.setDisabled(false);
        }
        setDialogueInputVisible(false);
        setDialogueChoiceVisible(false);
        if (dialoguePanel != null) {
            dialoguePanel.setVisible(false);
        }
    }

    private void setDialoguePanelText(String speaker, String text, String hint) {
        if (dialogueSpeakerLabel != null) {
            dialogueSpeakerLabel.setText(speaker != null ? speaker : "NPC");
        }
        if (dialogueTextLabel != null) {
            dialogueTextLabel.setText(text != null ? text : "");
        }
        if (dialogueHintLabel != null) {
            dialogueHintLabel.setText(hint != null ? hint : "");
        }
    }

    private void speakNpcTurnsSince(int historyStart) {
        if (npcSpeechService == null || dialogueController == null) {
            return;
        }
        DialogueSession session = dialogueController.getActiveSession();
        if (session == null) {
            return;
        }

        StringBuilder speech = new StringBuilder();
        List<DialogueTurn> history = session.getHistory();
        int start = Math.max(0, Math.min(historyStart, history.size()));
        for (int i = start; i < history.size(); i++) {
            DialogueTurn turn = history.get(i);
            if (turn == null || turn.isFromPlayer() || turn.getText() == null || turn.getText().trim().isEmpty()) {
                continue;
            }
            if (speech.length() > 0) {
                speech.append(' ');
            }
            speech.append(turn.getText().trim());
        }
        if (speech.length() > 0) {
            npcSpeechService.speak(speech.toString());
        }
    }

    private String fallbackDialogue(Npc npc) {
        if (npc != null && npc.getSampleDialogue() != null && !npc.getSampleDialogue().trim().isEmpty()) {
            return npc.getSampleDialogue();
        }
        if (npc != null && npc.getName() != null && !npc.getName().trim().isEmpty()) {
            return npc.getName() + " ti osserva in silenzio.";
        }
        return "L'NPC resta in silenzio.";
    }

    private boolean isDialogueBlockingGameplay() {
        return dialogueController != null && dialogueController.isActive();
    }

    private boolean isHelpRequestPending() {
        return dialogueController != null && dialogueController.isHelpRequestPending();
    }

    private void resolveHelpRequest(NpcHelpRequestChoice choice) {
        DialogueSession session = dialogueController != null ? dialogueController.getActiveSession() : null;
        if (session == null || !session.isHelpRequestPending() || npcHelpRequestService == null) {
            return;
        }

        NpcHelpRequestResult result = npcHelpRequestService.resolve(session.getPlayer(), session.getNpc(), choice);
        if (!result.isResolved()) {
            refreshDialogueSessionPanel(result.getFeedback());
            return;
        }

        NpcHelpRequest request = session.getNpc().getHelpRequest();
        boolean endConversation = request != null && request.isEndConversationAfterChoice();
        if (!dialogueController.completeHelpRequestChoice(session.getId(), result.getNpcReply(), endConversation)) {
            LOGGER.warning("[NPC UI] Esito richiesta non applicato alla sessione " + session.getId());
            return;
        }

        LOGGER.info("[NPC UI] Richiesta risolta: npc=" + session.getNpc().getId()
                + ", choice=" + choice
                + ", karmaDelta=" + result.getKarmaDelta()
                + ", reward=" + result.getRewardId());
        if (npcSpeechService != null) {
            npcSpeechService.speak(result.getNpcReply());
        }
        refreshDialogueSessionPanel(result.getFeedback());
        if (dialogueController.isEnded()) {
            uiStage.setKeyboardFocus(null);
        } else {
            uiStage.setKeyboardFocus(dialogueInputField);
        }
    }

    private void sortWorldActorsByDepth() {
        if (stage == null) {
            return;
        }
        stage.getActors().sort((first, second) -> Float.compare(second.getY(), first.getY()));
    }

    private void refreshDialogueSessionPanel(String hint) {
        DialogueSession session = dialogueController != null ? dialogueController.getActiveSession() : null;
        if (session == null) {
            return;
        }
        boolean inputVisible = session.isInputActive();
        setDialogueInputVisible(inputVisible);
        setDialogueChoiceVisible(session.isHelpRequestPending());
        if (dialogueInputField != null) {
            dialogueInputField.setDisabled(!session.isInputActive());
        }
        setDialoguePanelText(session.getNpc().getName(), buildDialogueTranscript(session),
                hint != null ? hint : "");
        if (dialoguePanel != null) {
            dialoguePanel.setVisible(true);
        }
    }

    private String buildDialogueTranscript(DialogueSession session) {
        if (session == null || session.getHistory().isEmpty()) {
            return "Scrivi cosa vuoi chiedere.";
        }

        StringBuilder builder = new StringBuilder();
        int start = Math.max(0, session.getHistory().size() - 6);
        for (int i = start; i < session.getHistory().size(); i++) {
            DialogueTurn turn = session.getHistory().get(i);
            if (turn == null || turn.getText() == null || turn.getText().trim().isEmpty()) {
                continue;
            }
            String speaker = turn.isFromPlayer() ? "Tu" : session.getNpc().getName();
            builder.append(speaker).append(": ").append(turn.getText().trim());
            if (i < session.getHistory().size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void setDialogueInputVisible(boolean visible) {
        if (dialogueInputField == null || dialogueInputCell == null) {
            return;
        }
        dialogueInputField.setVisible(visible);
        dialogueInputCell.height(visible ? 58f : 0f).padBottom(visible ? GameUiTheme.SPACE_2 : 0f);
        dialoguePanel.invalidateHierarchy();
    }

    private void setDialogueChoiceVisible(boolean visible) {
        if (dialogueChoiceTable == null || dialogueChoiceCell == null) {
            return;
        }
        dialogueChoiceTable.setVisible(visible);
        dialogueChoiceCell.height(visible ? 48f : 0f).padBottom(visible ? GameUiTheme.SPACE_2 : 0f);
        dialoguePanel.invalidateHierarchy();
    }

    private void updateDialoguePanelLayout() {
        if (uiStage == null || dialogueRoot == null || dialoguePanel == null || dialogueTextLabel == null) {
            return;
        }
        float viewportWidth = uiStage.getViewport().getWorldWidth();
        float panelWidth = Math.max(320f, Math.min(760f, viewportWidth - GameUiTheme.SPACE_6));
        float textWidth = Math.max(260f, panelWidth - GameUiTheme.SPACE_5);
        dialogueRoot.getCell(dialoguePanel).width(panelWidth);
        dialoguePanel.getCell(dialogueTextLabel).width(textWidth);
        if (dialogueInputCell != null) {
            dialogueInputCell.width(textWidth);
        }
        if (dialogueChoiceCell != null) {
            dialogueChoiceCell.width(textWidth);
        }
    }

    private float clamp(float value, float min, float max) {
        if (min > max) {
            return (min + max) / 2f;
        }
        return Math.max(min, Math.min(max, value));
    }

    private void showInitialStatus() {
        String message = model.getMessage();
        if (message != null && !message.trim().isEmpty()
                && !message.contains("MVC Base Architecture")) {
            showSaveStatus(message, false);
        }
    }

    private void showSaveStatus(SaveResult result) {
        boolean error = result.getStatus() == SaveResult.Status.ERROR
                || result.getStatus() == SaveResult.Status.INVALID;
        showSaveStatus(result.getMessage(), error);
    }

    private void showSaveStatus(String message, boolean error) {
        if (saveStatusLabel == null) {
            return;
        }
        saveStatusLabel.setText(message != null ? message : "");
        saveStatusLabel.setColor(error ? GameUiTheme.DANGER : GameUiTheme.SUCCESS);
        saveToast.setVisible(true);
        saveStatusTimer = 3f;
    }

    private void updateSaveStatus(float delta) {
        if (saveToast == null || !saveToast.isVisible()) {
            return;
        }
        saveStatusTimer -= delta;
        if (saveStatusTimer <= 0f) {
            saveToast.setVisible(false);
        }
    }

    /**
     * Aggiorna la visibilità degli sprite nemici in base allo stato degli encounter
     * point.
     * Nasconde gli sprite corrispondenti a encounter già sconfitti.
     */
    private void updateEnemySpriteVisibility() {
        if (encounterService == null || enemySpriteActors.isEmpty()) {
            return;
        }
        List<EnemyEncounterService.EnemyEncounterInfo> allInfos = encounterService.getAllEncounterInfo();
        for (int i = 0; i < enemySpriteActors.size() && i < allInfos.size(); i++) {
            enemySpriteActors.get(i).setVisible(allInfos.get(i).isActive());
        }
    }

    private static final class NpcWorldActor extends Actor {
        private final NpcInteraction interaction;
        private final NpcAssets assets;
        private final float drawSize;
        private final float yOffset;
        private float stateTime;

        private NpcWorldActor(NpcInteraction interaction, NpcAssets assets, float drawSize, float yOffset) {
            this.interaction = interaction;
            this.assets = assets;
            this.drawSize = drawSize;
            this.yOffset = yOffset;
            setSize(drawSize, drawSize);
            updatePosition();
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            updatePosition();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Animation<TextureRegion> animation = assets.getIdleSouthwestAnimation();
            if (animation == null) {
                return;
            }
            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            if (frame != null) {
                batch.draw(frame, getX(), getY(), getWidth(), getHeight());
            }
        }

        private void updatePosition() {
            setPosition(interaction.getX() - drawSize / 2f, interaction.getY() - yOffset);
        }
    }

    private static final class MapObjectActor extends Actor {
        private final TextureRegion region;
        private final float drawX;
        private final float drawYOffset;
        private final float objectWidth;
        private final float objectHeight;
        private final float scaleX;
        private final float scaleY;
        private final float rotation;
        private final float halfWidth;
        private final float halfHeight;
        private final float playerYOffset;

        private MapObjectActor(TextureRegion region, float drawX, float drawYOffset,
                float objectWidth, float objectHeight, float scaleX, float scaleY, float rotation,
                float sortingY, float playerYOffset) {
            this.region = region;
            this.drawX = drawX;
            this.drawYOffset = drawYOffset;
            this.objectWidth = objectWidth;
            this.objectHeight = objectHeight;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.rotation = rotation;
            this.halfWidth = objectWidth * 0.5f;
            this.halfHeight = objectHeight * 0.5f;
            this.playerYOffset = playerYOffset;

            setX(drawX);
            setY(sortingY);
            setSize(objectWidth, objectHeight);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (region == null) {
                return;
            }
            float drawY = getY() + playerYOffset + drawYOffset;
            batch.draw(
                    region,
                    getX(),
                    drawY,
                    halfWidth,
                    halfHeight,
                    objectWidth,
                    objectHeight,
                    scaleX,
                    scaleY,
                    rotation);
        }
    }

    private void addMapObjectActors() {
        if (levelRuntime.getId() != 2) {
            return;
        }
        com.badlogic.gdx.maps.MapLayer layer = map.getLayers().get("Immagini");
        if (layer == null) {
            return;
        }

        float mapTileWidth = map.getProperties().get("tilewidth", Integer.class);
        float mapTileHeight = map.getProperties().get("tileheight", Integer.class);

        for (com.badlogic.gdx.maps.MapObject object : layer.getObjects()) {
            if (object instanceof com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) {
                com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject tileObj = (com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) object;
                com.badlogic.gdx.maps.tiled.TiledMapTile tile = tileObj.getTile();
                if (tile != null) {
                    TextureRegion region = tile.getTextureRegion();
                    if (region != null) {
                        float tileX = tileObj.getX() / mapTileHeight - 0.5f;
                        float tileY = tileObj.getY() / mapTileHeight + 0.5f;

                        float objectWidth = io.github.iss_2025_2026.map.IsoMapGeometry.propertyFloat(
                                tileObj.getProperties(), "width", region.getRegionWidth());
                        float objectHeight = io.github.iss_2025_2026.map.IsoMapGeometry.propertyFloat(
                                tileObj.getProperties(), "height", region.getRegionHeight());

                        float drawX = (tileX + tileY) * (mapTileWidth / 2f)
                                - objectWidth * 0.5f + tile.getOffsetX();
                        float worldY = (tileY - tileX) * (mapTileHeight / 2f);
                        float sortingY = worldY - playerYOffset;

                        MapObjectActor actor = new MapObjectActor(
                                region,
                                drawX,
                                tile.getOffsetY(),
                                objectWidth,
                                objectHeight,
                                tileObj.getScaleX(),
                                tileObj.getScaleY(),
                                tileObj.getRotation(),
                                sortingY,
                                playerYOffset
                        );
                        stage.addActor(actor);
                    }
                }
            }
        }
    }

    private void renderCollectibles(Batch batch) {
        if (collectibleService == null || collectibleRenderer == null) {
            return;
        }

        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        collectibleRenderer.render(batch, cam.combined, collectibleService.getActiveCollectibles());
    }
}
