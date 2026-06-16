package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.BattleController;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.factory.CharacterFactory;
import io.github.iss_2025_2026.factory.NpcFactory;
import io.github.iss_2025_2026.factory.YamlCharacterFactory;
import io.github.iss_2025_2026.map.LevelRuntime;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.DialogueSession;
import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.EvolutionResult;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
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
import io.github.iss_2025_2026.service.NpcInteraction;
import io.github.iss_2025_2026.service.NpcInteractionService;
import io.github.iss_2025_2026.service.RunMusicManager;
import io.github.iss_2025_2026.service.SaveResult;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Game View (Parte del pattern MVC).
 * Implementazione di {@link Screen} di LibGDX.
 * Si occupa esclusivamente del rendering dello stato del Model.
 * Delega la logica di aggiornamento al Controller, la fisica a PhysicsFacade e gli asset a PlayerAssets.
 */
public class LevelScreen implements Screen {
    private static final Logger LOGGER = Logger.getLogger(LevelScreen.class.getName());
    private static final float ENCOUNTER_RADIUS = 80f;
    private static final float NPC_INTERACTION_RADIUS = 120f;

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
    private EvolutionService evolutionService;
    private PlayerAssets playerAssets;
    private PlayerAssets playerTwoAssets;

    private CharacterState lastStateP1 = CharacterState.IDLE;
    private CharacterState lastStateP2 = CharacterState.IDLE;
    private Rectangle mapBounds;
    private float cameraEdgePadding;
    private boolean battleTransitionPending;
    private float encounterCooldownTimer;
    private NpcInteraction nearbyNpcInteraction;
    private Player nearbyNpcPlayer;

    private static final float DEFAULT_CAMERA_EDGE_PADDING = 320f;

    // Configurable game properties
    private final float playerSize;
    private final float playerYOffset;
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
        this.cameraZoom = GameProperties.getFloat(GameProperties.KEY_CAMERA_ZOOM, 0.72f);
        this.drawObstacleDebug = GameProperties.getBoolean(GameProperties.KEY_DRAW_PHYSICS_DEBUG, true);

        configureLevel(levelRuntime);

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
        this.checkpointService = new CheckpointService(model, levelRuntime);
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
                if (keycode == Input.Keys.F3) {
                    drawObstacleDebug = !drawObstacleDebug;
                    return true;
                }

                return false;
            }
        };
    }

    private void configureLevel(LevelRuntime levelRuntime) {
        level = levelRuntime.getLevel();
        map = level.getMap();
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
        mapBounds = level.getGeometry().getBounds();
        cameraEdgePadding = level.getGeometry().mapPropertyFloat("camera_edge_padding", DEFAULT_CAMERA_EDGE_PADDING);

        CharacterFactory characterFactory = new YamlCharacterFactory();
        encounterService = new EnemyEncounterService(
                level.enemyObjects(), characterFactory, level.getGeometry(), ENCOUNTER_RADIUS);
        NpcFactory npcFactory = new NpcFactory();
        npcDialogueService = new NpcDialogueService();
        dialogueController = new NpcDialogueController();
        evolutionService = new EvolutionService();
        npcInteractionService = new NpcInteractionService(
                level.npcObjects(), npcFactory, level.getGeometry(), levelRuntime.getId(), NPC_INTERACTION_RADIUS);

        // Inizializza la facciata fisica Box2D con le dimensioni configurate
        physicsFacade = new PhysicsFacade(level, playerSize, playerYOffset);
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

        dialoguePanel.add(dialogueSpeakerLabel).padBottom(GameUiTheme.SPACE_1).row();
        dialoguePanel.add(dialogueTextLabel).growX().padBottom(GameUiTheme.SPACE_2).row();
        dialogueInputCell = dialoguePanel.add(dialogueInputField).growX().height(0f).padBottom(0f);
        dialoguePanel.row();
        dialoguePanel.add(dialogueHintLabel).growX();
        setDialogueInputVisible(false);

        dialogueRoot.add(dialoguePanel).growX().minWidth(320f).maxWidth(760f);
        updateDialoguePanelLayout();
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
        // Aggiorna HUD prima di processare la UI
        if (playerOneHud != null) playerOneHud.update();
        if (playerTwoHud != null) playerTwoHud.update();
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        updateDialoguePanelLayout();
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
        uiStage.dispose();
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
        LOGGER.info("[NPC UI] Risposta AI applicata alla sessione "
                + requestContext.getSessionId() + ": " + result.getReply());
        dialogueInputField.setDisabled(dialogueController.isEnded());
        refreshDialogueSessionPanel(dialogueController.getStatusMessage());
        if (dialogueController.isEnded()) {
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

    private void refreshDialogueSessionPanel(String hint) {
        DialogueSession session = dialogueController != null ? dialogueController.getActiveSession() : null;
        if (session == null) {
            return;
        }
        boolean inputVisible = !dialogueController.isEnded();
        setDialogueInputVisible(inputVisible);
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
}
