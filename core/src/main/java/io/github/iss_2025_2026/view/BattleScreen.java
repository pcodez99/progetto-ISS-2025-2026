package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.BattleController;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.model.combat.BattlePhase;
import io.github.iss_2025_2026.model.combat.ItemUseResult;
import io.github.iss_2025_2026.service.EnemyEncounter;
import io.github.iss_2025_2026.service.EnemyEncounterService;
import io.github.iss_2025_2026.service.GameOverService;
import io.github.iss_2025_2026.service.RunMusicManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schermata di combattimento a turni in stile Final Fantasy.
 */
public class BattleScreen implements Screen {
    private static final float STATUS_PANEL_WIDTH = 380f;
    private static final float ENEMY_PANEL_WIDTH = 390f;
    private static final float ACTION_PANEL_WIDTH = 320f;
    private static final float LOG_PANEL_WIDTH = 430f;
    private static final float LOG_PANEL_HEIGHT = 128f;
    private static final float ACTION_BUTTON_WIDTH = 264f;
    private static final float ACTION_BUTTON_HEIGHT = 50f;
    private static final float ENEMY_COUNTER_DELAY = 0.8f;
    private static final float ATTACK_FRAME_DURATION = 0.05f;
    private static final float PLAYER_ARENA_Y_RATIO = 0.12f;
    private static final float ENEMY_ARENA_Y_RATIO = 0.10f;
    private static final String MOM_SLIPPER_EFFECT_PATH = "effects/mom-slipper.png";
    private static final float MOM_SLIPPER_BASE_SIZE = 112f;
    private static final String ALIEN_ORB_EFFECT_PATH = "effects/alien-orb.png";
    private static final float ALIEN_ORB_BASE_SIZE = 104f;
    private static final String MOM_SPECIAL_SHEET_PATH = "characters/battle-sidescroller/mom/special_right/spritesheet.png";
    private static final int MOM_SPECIAL_FRAME_SIZE = 256;
    private static final int MOM_SPECIAL_COLUMNS = 7;
    private static final int MOM_SPECIAL_ROWS = 7;
    private static final float MOM_SPECIAL_FRAME_DURATION = 0.045f;

    private final Main game;
    private final GameContext gameContext;
    private final GameModel gameModel;
    private final LevelScreen returnScreen;
    private final BattleModel battleModel;
    private final BattleController battleController;
    private final EnemyEncounter encounter;
    private final EnemyEncounterService encounterService;

    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Texture momSlipperTexture;
    private Texture alienOrbTexture;
    private Group combatLayer;
    private Group effectsLayer;
    private Table root;
    private Table enemiesRow;
    private Table playersRow;
    private Table menuPanel;
    private Table logPanel;
    private ScrollPane logScroll;
    private Label logLabel;
    private TextButton fleeButton;
    private Label statusLabel;
    private BattlePhase lastRenderedPhase;
    private BattleController.MenuState lastRenderedMenuState;
    private final Map<Player, PlayerBattleAssets> playerAssets = new HashMap<>();
    private final Map<Player, Image> playerActors = new HashMap<>();
    private final Map<Enemy, EnemyBattleAssets> enemyAssets = new HashMap<>();
    private final Map<Enemy, Image> enemyActors = new HashMap<>();
    private final InputAdapter inputListener;
    private float battleTime;
    private Player activePlayerAttacker;
    private Player activePlayerSpecialAttacker;
    private float playerAttackTimer;
    private float playerAttackStateTime;
    private float playerSpecialTimer;
    private float playerSpecialStateTime;
    private float enemyTurnDelayTimer;
    private float enemyAttackTimer;
    private float enemyAttackStateTime;

    public BattleScreen(Main game, GameContext gameContext, GameModel gameModel, LevelScreen returnScreen,
            BattleModel battleModel, BattleController battleController, EnemyEncounter encounter,
            EnemyEncounterService encounterService) {
        this.game = game;
        this.gameContext = gameContext;
        this.gameModel = gameModel;
        this.returnScreen = returnScreen;
        this.battleModel = battleModel;
        this.battleController = battleController;
        this.encounter = encounter;
        this.encounterService = encounterService;

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.Q) {
                    if (battleController.onFleeAttempt()) {
                        finishBattle(false);
                    }
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void show() {
        skin = GameUiTheme.loadSkin();
        int currentLevel = gameModel.getGameState().getCurrentLevelId();
        backgroundTexture = new Texture(Gdx.files.internal("map/levels/" + currentLevel + "/Arena_level_" + currentLevel + ".png"));
        momSlipperTexture = new Texture(Gdx.files.internal(MOM_SLIPPER_EFFECT_PATH));
        momSlipperTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        alienOrbTexture = new Texture(Gdx.files.internal(ALIEN_ORB_EFFECT_PATH));
        alienOrbTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        stage = new Stage(new ScreenViewport());
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputListener);
        Gdx.input.setInputProcessor(multiplexer);
        buildUi();
        RunMusicManager.pause();
    }

    private void buildUi() {
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        backgroundImage.setScaling(Scaling.fill);
        backgroundImage.setAlign(Align.center);
        stage.addActor(backgroundImage);

        combatLayer = new Group();
        combatLayer.setBounds(0f, 0f, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        stage.addActor(combatLayer);
        rebuildCombatActors();

        effectsLayer = new Group();
        effectsLayer.setBounds(0f, 0f, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        effectsLayer.setTouchable(Touchable.disabled);
        stage.addActor(effectsLayer);

        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        buildHudLayout();
    }

    private void buildHudLayout() {
        root.clearChildren();
        root.top().pad(GameUiTheme.SPACE_3);

        Table topHud = new Table();
        topHud.top();

        float worldWidth = stage.getViewport().getWorldWidth();
        boolean compact = worldWidth < 860f;
        float compactPanelWidth = Math.max(260f, worldWidth - GameUiTheme.SPACE_6);
        float statusPanelWidth = compact ? Math.min(STATUS_PANEL_WIDTH, compactPanelWidth) : STATUS_PANEL_WIDTH;
        float enemyPanelWidth = compact ? Math.min(ENEMY_PANEL_WIDTH, compactPanelWidth) : ENEMY_PANEL_WIDTH;
        float statusTextWidth = Math.max(220f, statusPanelWidth - GameUiTheme.SPACE_4);

        Table playerStatusPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        playerStatusPanel.defaults().left().growX();
        statusLabel = new Label(buildStatusText(), skin, GameUiTheme.LABEL_SECTION);
        statusLabel.setWrap(true);
        playerStatusPanel.add(statusLabel).width(statusTextWidth)
                .padBottom(GameUiTheme.SPACE_2).row();

        playersRow = new Table();
        playersRow.left();
        refreshPlayerPanels();
        playerStatusPanel.add(playersRow).growX().row();

        Table enemiesPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        enemiesPanel.defaults().left().growX();
        enemiesPanel.add(new Label("NEMICI", skin, GameUiTheme.LABEL_TAG))
                .padBottom(GameUiTheme.SPACE_2).row();
        enemiesRow = new Table();
        enemiesRow.left();
        refreshEnemyPanels();
        enemiesPanel.add(enemiesRow).growX().row();

        if (compact) {
            topHud.add(playerStatusPanel).width(statusPanelWidth).growX().top().row();
            topHud.add(enemiesPanel).width(enemyPanelWidth).growX().top().padTop(GameUiTheme.SPACE_2).row();
        } else {
            topHud.add(playerStatusPanel).width(statusPanelWidth).top().left();
            topHud.add().growX();
            topHud.add(enemiesPanel).width(enemyPanelWidth).top().right();
        }
        root.add(topHud).growX().top().row();
        root.add().growY().row();

        Table bottomHud = new Table();
        bottomHud.bottom();

        menuPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_3);
        logPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        logPanel.defaults().left().growX();
        logPanel.add(new Label("LOG", skin, GameUiTheme.LABEL_TAG)).padBottom(GameUiTheme.SPACE_1).row();
        logLabel = new Label("", skin, GameUiTheme.LABEL_BODY);
        logLabel.setWrap(true);
        logScroll = new ScrollPane(logLabel);
        logScroll.setFadeScrollBars(false);
        logScroll.setScrollingDisabled(true, false);
        logScroll.setOverscroll(false, false);
        logPanel.add(logScroll).grow().height(LOG_PANEL_HEIGHT - 36f).row();

        if (compact) {
            bottomHud.add(menuPanel).growX().bottom().row();
            bottomHud.add(logPanel).growX().height(LOG_PANEL_HEIGHT).padTop(GameUiTheme.SPACE_2).row();
        } else {
            bottomHud.add(menuPanel).width(ACTION_PANEL_WIDTH).bottom().left();
            bottomHud.add().growX();
            bottomHud.add(logPanel).width(LOG_PANEL_WIDTH).height(LOG_PANEL_HEIGHT).bottom().right();
        }
        root.add(bottomHud).growX().bottom();

        lastRenderedPhase = battleModel.getPhase();
        lastRenderedMenuState = battleController.getMenuState();
        refreshMenu();
        refreshLog();
    }

    private void refreshEnemyPanels() {
        if (enemiesRow == null) {
            return;
        }
        enemiesRow.clearChildren();
        for (Enemy enemy : battleModel.getAliveEnemies()) {
            enemiesRow.add(createEnemyPanel(enemy)).growX().padBottom(GameUiTheme.SPACE_1).row();
        }
        if (battleModel.getAliveEnemies().isEmpty()) {
            enemiesRow.add(new Label("Nessun nemico", skin, GameUiTheme.LABEL_MUTED)).left();
        }
    }

    private void refreshPlayerPanels() {
        if (playersRow == null) {
            return;
        }
        playersRow.clearChildren();
        playersRow.add(createPlayerPanel(battleModel.getPlayerOne())).growX().padBottom(GameUiTheme.SPACE_1).row();
        if (battleModel.getPlayerTwo() != null) {
            playersRow.add(createPlayerPanel(battleModel.getPlayerTwo())).growX().row();
        }
    }

    private void refreshBattleDisplay() {
        rebuildCombatActors();
        refreshEnemyPanels();
        refreshPlayerPanels();
        refreshLog();
        refreshStatus();
    }

    private Table createEnemyPanel(Enemy enemy) {
        Table panel = new Table();
        panel.left();
        Label nameLabel = new Label(enemy.getName(), skin, GameUiTheme.LABEL_BODY);
        nameLabel.setWrap(true);
        Label hpLabel = new Label("HP " + Math.max(0, enemy.getHp()) + "/" + enemy.getMaxHp(),
                skin, GameUiTheme.LABEL_TAG);
        if (enemy.getMaxHp() > 0 && enemy.getHp() / (float) enemy.getMaxHp() <= 0.35f) {
            hpLabel.setColor(GameUiTheme.DANGER);
        }
        panel.add(nameLabel).left().growX().width(220f).padRight(GameUiTheme.SPACE_2);
        panel.add(hpLabel).right();
        return panel;
    }

    private void rebuildCombatActors() {
        if (combatLayer == null) {
            return;
        }
        disposePlayerAssets();
        disposeEnemyAssets();
        playerActors.clear();
        enemyActors.clear();
        combatLayer.clearChildren();

        List<Player> alivePlayers = battleModel.getAlivePlayers();
        for (int i = 0; i < alivePlayers.size(); i++) {
            Player player = alivePlayers.get(i);
            PlayerBattleAssets assets = new PlayerBattleAssets(player);
            Image actor = createPlayerActor(player, assets, i, alivePlayers.size());
            playerAssets.put(player, assets);
            playerActors.put(player, actor);
            combatLayer.addActor(actor);
        }

        List<Enemy> aliveEnemies = battleModel.getAliveEnemies();
        for (int i = 0; i < aliveEnemies.size(); i++) {
            Enemy enemy = aliveEnemies.get(i);
            EnemyBattleAssets assets = new EnemyBattleAssets(enemy);
            Image actor = createEnemyActor(enemy, assets, i, aliveEnemies.size());
            enemyAssets.put(enemy, assets);
            enemyActors.put(enemy, actor);
            combatLayer.addActor(actor);
        }
    }

    private Image createPlayerActor(Player player, PlayerBattleAssets assets, int index, int playerCount) {
        Image sprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Animation<TextureRegion> animation = player == activePlayerSpecialAttacker && playerSpecialTimer > 0f
                        && assets.getSpecialAnim() != null
                        ? assets.getSpecialAnim()
                        : player == activePlayerAttacker && playerAttackTimer > 0f
                        && assets.getAttackAnim() != null
                        ? assets.getAttackAnim()
                        : assets.getIdleAnim();
                if (animation == null) {
                    return;
                }
                boolean usingSpecial = animation == assets.getSpecialAnim();
                boolean usingAttack = animation == assets.getAttackAnim();
                boolean looping = !usingSpecial && (!usingAttack || player != activePlayerAttacker
                        || playerAttackTimer <= 0f || animation == assets.getIdleAnim());
                float stateTime = usingSpecial ? playerSpecialStateTime
                        : usingAttack ? playerAttackStateTime : battleTime;
                TextureRegion frame = animation.getKeyFrame(stateTime, looping);
                batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),
                        getScaleY(), getRotation());
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                positionPlayerActor(this, assets, index, playerCount);
            }
        };
        positionPlayerActor(sprite, assets, index, playerCount);
        return sprite;
    }

    private void positionPlayerActor(Image sprite, PlayerBattleAssets assets, int index, int playerCount) {
        float worldWidth = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        float size = assets.getDisplaySize();
        float spacingX = Math.min(145f, size * 0.48f);
        float safeLeft = worldWidth < 860f ? worldWidth * 0.12f : ACTION_PANEL_WIDTH + GameUiTheme.SPACE_4;
        float startX = Math.max(worldWidth * 0.14f, safeLeft);
        float startY = worldHeight * PLAYER_ARENA_Y_RATIO;

        sprite.setSize(size, size);
        sprite.setPosition(startX + index * spacingX, startY + (playerCount - index - 1) * 18f);
    }

    private Image createEnemyActor(Enemy enemy, EnemyBattleAssets assets, int index, int enemyCount) {
        Image sprite = new Image() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Animation<TextureRegion> animation = enemyAttackTimer > 0f && assets.getAttackAnim() != null
                        ? assets.getAttackAnim()
                        : assets.getIdleAnim();
                if (animation == null) {
                    return;
                }
                boolean looping = enemyAttackTimer <= 0f || animation == assets.getIdleAnim();
                float stateTime = animation == assets.getAttackAnim() ? enemyAttackStateTime : battleTime;
                TextureRegion frame = animation.getKeyFrame(stateTime, looping);
                batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),
                        getScaleY(), getRotation());
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                positionEnemyActor(this, assets, index, enemyCount);
            }
        };
        positionEnemyActor(sprite, assets, index, enemyCount);
        return sprite;
    }

    private void positionEnemyActor(Image sprite, EnemyBattleAssets assets, int index, int enemyCount) {
        float worldWidth = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        float size = assets.getDisplaySize();
        int columns = Math.min(4, Math.max(1, enemyCount));
        int column = index % columns;
        int row = index / columns;
        float spacingX = Math.min(160f, size * 0.56f);
        float spacingY = Math.min(120f, size * 0.42f);
        float startX = worldWidth * 0.57f;
        float startY = worldHeight * ENEMY_ARENA_Y_RATIO;

        sprite.setSize(size, size);
        sprite.setPosition(startX + column * spacingX, startY - row * spacingY - (column % 2) * 14f);
    }

    private Table createPlayerPanel(Player player) {
        Table panel = new Table();
        panel.left();

        Label nameLabel = new Label(player.getName(), skin, GameUiTheme.LABEL_BODY);
        if (player == battleModel.getCurrentTurnPlayer() && isPlayerTurn()) {
            nameLabel.setColor(GameUiTheme.ACCENT_LIME);
        } else {
            nameLabel.setColor(GameUiTheme.ACCENT_CYAN);
        }

        Label hpLabel = new Label("HP " + Math.max(0, player.getHp()) + "/" + player.getMaxHp(),
                skin, GameUiTheme.LABEL_TAG);
        if (player.getMaxHp() > 0 && player.getHp() / (float) player.getMaxHp() <= 0.35f) {
            hpLabel.setColor(GameUiTheme.DANGER);
        }

        Label levelLabel = new Label("Lv " + player.getLevel(), skin, GameUiTheme.LABEL_MUTED);
        panel.add(nameLabel).left().growX().width(150f).padRight(GameUiTheme.SPACE_2);
        panel.add(hpLabel).right().width(96f).padRight(GameUiTheme.SPACE_2);
        panel.add(levelLabel).right();
        return panel;
    }

    private String buildStatusText() {
        BattlePhase phase = battleModel.getPhase();
        if (phase == BattlePhase.PLAYER_ONE_TURN) {
            return "Turno di " + battleModel.getPlayerOne().getName();
        }
        if (phase == BattlePhase.PLAYER_TWO_TURN && battleModel.getPlayerTwo() != null) {
            return "Turno di " + battleModel.getPlayerTwo().getName();
        }
        if (phase == BattlePhase.ENEMY_TURN) {
            return "Turno nemico...";
        }
        return "Battaglia";
    }

    private void refreshMenu() {
        menuPanel.clearChildren();
        fleeButton = null;

        BattleController.MenuState menuState = battleController.getMenuState();
        if (menuState == BattleController.MenuState.TARGET_SELECTION) {
            addActionHeader("BERSAGLIO");
            for (Enemy enemy : battleModel.getAliveEnemies()) {
                addActionButton(createTargetButton(enemy));
            }
            addActionButton(createBackButton());
            addFleeButton();
            return;
        }

        if (menuState == BattleController.MenuState.INVENTORY_SELECTION) {
            addActionHeader("INVENTARIO");
            Player current = battleModel.getCurrentTurnPlayer();
            List<Collectible> items = current.getBackpack().getItems();
            if (items.isEmpty()) {
                menuPanel.add(new Label("Zaino vuoto", skin, GameUiTheme.LABEL_MUTED))
                        .left().padBottom(GameUiTheme.SPACE_1).row();
            } else {
                for (Collectible item : items) {
                    addActionButton(createItemButton(item));
                }
            }
            addActionButton(createBackButton());
            addFleeButton();
            return;
        }

        if (!isPlayerTurn()) {
            addActionHeader("ATTESA");
            menuPanel.add(new Label("Turno nemico", skin, GameUiTheme.LABEL_MUTED))
                    .left().padBottom(GameUiTheme.SPACE_2).row();
            addFleeButton();
            return;
        }

        addActionHeader("AZIONI");
        addActionButton(createMenuButton("Attacco", this::onAttack));

        // Crea il bottone dell'abilità speciale e lo disabilita se già usato
        Player currentPlayer = battleModel.getCurrentTurnPlayer();
        String specialLabel = currentPlayer != null && currentPlayer.getAbility() != null
                ? currentPlayer.getAbility().getName()
                : "Abilita speciale";
        TextButton specialAbilityButton = createMenuButton(specialLabel, this::onSpecial);
        if (currentPlayer != null && currentPlayer.hasUsedSpecialAbilityThisBattle()) {
            specialAbilityButton.setDisabled(true);
        }
        addActionButton(specialAbilityButton);

        TextButton inventoryButton = createMenuButton("Inventario", this::onInventory);
        inventoryButton.setDisabled(!battleModel.canCurrentPlayerUseItem());
        addActionButton(inventoryButton);
        addFleeButton();
    }

    private void addActionHeader(String text) {
        Label label = new Label(text, skin, GameUiTheme.LABEL_TAG);
        menuPanel.add(label).left().padBottom(GameUiTheme.SPACE_2).row();
    }

    private void addActionButton(TextButton button) {
        menuPanel.add(button).width(ACTION_BUTTON_WIDTH).height(ACTION_BUTTON_HEIGHT)
                .left().padBottom(GameUiTheme.SPACE_1).row();
    }

    private void addFleeButton() {
        fleeButton = createFleeButton();
        addActionButton(fleeButton);
        updateFleeButton();
    }

    private TextButton createMenuButton(String text, Runnable action) {
        TextButton button = GameUiFactory.createButton(text, skin, GameUiTheme.BUTTON_SECONDARY);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
    }

    private TextButton createFleeButton() {
        TextButton button = GameUiFactory.createButton(formatFleeText(), skin, GameUiTheme.BUTTON_GHOST);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onFlee();
            }
        });
        return button;
    }

    private TextButton createTargetButton(Enemy enemy) {
        TextButton button = GameUiFactory.createButton(enemy.getName(), skin, GameUiTheme.BUTTON_PRIMARY);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Player attacker = battleModel.getCurrentTurnPlayer();
                triggerPlayerAttack(attacker);
                triggerMomSlipperAttack(attacker, enemy);
                battleController.onTargetSelected(enemy);
                afterPlayerAction();
            }
        });
        return button;
    }

    private TextButton createItemButton(Collectible item) {
        TextButton button = GameUiFactory.createButton(item.getName(), skin, GameUiTheme.BUTTON_SECONDARY);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Enemy fallbackTarget = firstAliveEnemy();
                ItemUseResult result = battleController.onItemSelected(item, fallbackTarget);
                afterItemUse(result);
            }
        });
        return button;
    }

    private TextButton createBackButton() {
        TextButton button = GameUiFactory.createButton("Indietro", skin, GameUiTheme.BUTTON_GHOST);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                battleController.onMenuBack();
                refreshMenuIfNeeded(true);
            }
        });
        return button;
    }

    private void onAttack() {
        battleController.onAttackSelected();
        refreshMenuIfNeeded(true);
    }

    private void onSpecial() {
        Player currentPlayer = battleModel.getCurrentTurnPlayer();
        if (currentPlayer == null || currentPlayer.getAbility() == null
                || currentPlayer.hasUsedSpecialAbilityThisBattle()) {
            return;
        }
        triggerPlayerSpecial(currentPlayer);
        battleController.onSpecialAbilitySelected();
        afterPlayerAction();
    }

    private void onInventory() {
        battleController.onInventorySelected();
        refreshMenuIfNeeded(true);
    }

    private void onFlee() {
        if (battleController.onFleeAttempt()) {
            finishBattle(false);
        }
    }

    private String formatFleeText() {
        return String.format("Fuggi (%.0fs)", Math.max(0f, battleModel.getFleeTimer()));
    }

    private void updateFleeButton() {
        if (fleeButton == null) {
            return;
        }
        fleeButton.setText(formatFleeText());
        fleeButton.setDisabled(battleModel.getFleeTimer() <= 0f);
    }

    private Enemy firstAliveEnemy() {
        List<Enemy> alive = battleModel.getAliveEnemies();
        return alive.isEmpty() ? null : alive.get(0);
    }

    private boolean isPlayerTurn() {
        BattlePhase phase = battleModel.getPhase();
        return phase == BattlePhase.PLAYER_ONE_TURN || phase == BattlePhase.PLAYER_TWO_TURN;
    }

    private void afterPlayerAction() {
        refreshBattleDisplay();
        if (battleModel.isBattleOver()) {
            finishBattle(battleModel.isVictory());
            return;
        }
        scheduleEnemyCounterDelayIfNeeded();
        refreshMenuIfNeeded(true);
    }

    private void afterItemUse(ItemUseResult result) {
        refreshBattleDisplay();
        if (battleModel.isBattleOver()) {
            finishBattle(battleModel.isVictory());
            return;
        }
        refreshMenuIfNeeded(true);
    }

    private void scheduleEnemyCounterDelayIfNeeded() {
        if (battleModel.getPhase() == BattlePhase.ENEMY_TURN) {
            enemyTurnDelayTimer = ENEMY_COUNTER_DELAY;
        }
    }

    private void triggerPlayerAttack(Player player) {
        if (player == null) {
            return;
        }
        activePlayerAttacker = player;
        playerAttackTimer = PlayerBattleAssets.ATTACK_DURATION;
        playerAttackStateTime = 0f;
    }

    private void triggerPlayerSpecial(Player player) {
        if (player == null) {
            return;
        }
        PlayerBattleAssets assets = playerAssets.get(player);
        if (isMom(player) && assets != null && assets.getSpecialAnim() != null) {
            activePlayerSpecialAttacker = player;
            playerSpecialTimer = assets.getSpecialAnim().getAnimationDuration();
            playerSpecialStateTime = 0f;
            return;
        }
        triggerPlayerAttack(player);
    }

    private void triggerMomSlipperAttack(Player player, Enemy target) {
        if (!isMom(player) || target == null || momSlipperTexture == null || effectsLayer == null) {
            return;
        }

        float worldWidth = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        Image sourceActor = playerActors.get(player);
        Image targetActor = enemyActors.get(target);
        float sourceX = sourceActor != null
                ? sourceActor.getX() + sourceActor.getWidth() * 0.58f
                : Math.max(worldWidth * 0.18f,
                        worldWidth < 860f ? worldWidth * 0.16f : ACTION_PANEL_WIDTH + GameUiTheme.SPACE_5);
        float sourceY = sourceActor != null
                ? sourceActor.getY() + sourceActor.getHeight() * 0.58f
                : worldHeight * 0.48f;
        float targetX = targetActor != null ? targetActor.getX() + targetActor.getWidth() * 0.42f : worldWidth * 0.68f;
        float targetY = targetActor != null ? targetActor.getY() + targetActor.getHeight() * 0.52f : worldHeight * 0.50f;

        float size = MOM_SLIPPER_BASE_SIZE * MathUtils.random(0.92f, 1.12f);
        float startX = sourceX + MathUtils.random(-20f, 18f);
        float startY = sourceY + MathUtils.random(-18f, 24f);
        float endX = targetX + MathUtils.random(-30f, 34f);
        float endY = targetY + MathUtils.random(-36f, 32f);
        float arcX = (startX + endX) * 0.5f + MathUtils.random(-28f, 30f);
        float arcY = Math.max(startY, endY) + MathUtils.random(70f, 116f);
        float duration = MathUtils.random(0.56f, 0.70f);
        float spin = MathUtils.random(260f, 500f) * (MathUtils.randomBoolean() ? 1f : -1f);
        float endScale = MathUtils.random(0.76f, 0.94f);

        Image slipper = new Image(momSlipperTexture);
        slipper.setTouchable(Touchable.disabled);
        slipper.setSize(size, size);
        slipper.setOrigin(Align.center);
        slipper.setPosition(startX, startY);
        slipper.setRotation(MathUtils.random(0f, 360f));
        slipper.setColor(1f, 1f, 1f, 0f);
        slipper.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.sequence(
                                Actions.fadeIn(0.08f),
                                Actions.delay(duration * 0.38f),
                                Actions.fadeOut(duration * 0.44f)),
                        Actions.sequence(
                                Actions.moveTo(arcX, arcY, duration * 0.42f, Interpolation.sineOut),
                                Actions.moveTo(endX, endY, duration * 0.58f, Interpolation.sineIn)),
                        Actions.rotateBy(spin, duration, Interpolation.sine),
                        Actions.scaleTo(endScale, endScale, duration, Interpolation.sineOut)),
                Actions.removeActor()));
        effectsLayer.addActor(slipper);
    }

    private void triggerEnemyTwoOrbAttacks() {
        if (alienOrbTexture == null || effectsLayer == null) {
            return;
        }
        List<Player> alivePlayers = battleModel.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            return;
        }

        for (Enemy enemy : battleModel.getAliveEnemies()) {
            if (!isEnemyTwo(enemy)) {
                continue;
            }
            Player target = alivePlayers.get(MathUtils.random(alivePlayers.size() - 1));
            triggerAlienOrbAttack(enemy, target);
        }
    }

    private void triggerAlienOrbAttack(Enemy enemy, Player target) {
        Image sourceActor = enemyActors.get(enemy);
        Image targetActor = playerActors.get(target);
        float worldWidth = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        float sourceX = sourceActor != null ? sourceActor.getX() + sourceActor.getWidth() * 0.30f : worldWidth * 0.70f;
        float sourceY = sourceActor != null ? sourceActor.getY() + sourceActor.getHeight() * 0.55f : worldHeight * 0.48f;
        float targetX = targetActor != null ? targetActor.getX() + targetActor.getWidth() * 0.58f : worldWidth * 0.28f;
        float targetY = targetActor != null ? targetActor.getY() + targetActor.getHeight() * 0.54f : worldHeight * 0.42f;

        float size = ALIEN_ORB_BASE_SIZE * MathUtils.random(0.88f, 1.16f);
        float startX = sourceX + MathUtils.random(-18f, 18f);
        float startY = sourceY + MathUtils.random(-22f, 22f);
        float endX = targetX + MathUtils.random(-28f, 28f);
        float endY = targetY + MathUtils.random(-28f, 28f);
        float duration = MathUtils.random(0.50f, 0.66f);
        float spin = MathUtils.random(180f, 420f) * (MathUtils.randomBoolean() ? 1f : -1f);

        Image orb = new Image(alienOrbTexture);
        orb.setTouchable(Touchable.disabled);
        orb.setSize(size, size);
        orb.setOrigin(Align.center);
        orb.setPosition(startX, startY);
        orb.setRotation(MathUtils.random(0f, 360f));
        orb.setColor(1f, 1f, 1f, 0f);
        orb.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.sequence(
                                Actions.fadeIn(0.07f),
                                Actions.delay(duration * 0.34f),
                                Actions.fadeOut(duration * 0.44f)),
                        Actions.moveTo(endX, endY, duration, Interpolation.sineIn),
                        Actions.rotateBy(spin, duration, Interpolation.sine),
                        Actions.scaleTo(0.78f, 0.78f, duration, Interpolation.sineIn)),
                Actions.removeActor()));
        effectsLayer.addActor(orb);
    }

    private boolean isMom(Player player) {
        return player != null && "mamma".equals(player.getCharacterId());
    }

    private boolean isEnemyTwo(Enemy enemy) {
        if (enemy == null) {
            return true;
        }
        String enemyId = enemy.getEnemyId();
        return !"boss_livello_3".equals(enemyId)
                && !"alieno_guardiano".equals(enemyId)
                && !"boss_livello_1".equals(enemyId)
                && !"boss_livello_2".equals(enemyId);
    }

    private void refreshMenuIfNeeded(boolean force) {
        BattlePhase phase = battleModel.getPhase();
        BattleController.MenuState menuState = battleController.getMenuState();
        if (force || phase != lastRenderedPhase || menuState != lastRenderedMenuState) {
            lastRenderedPhase = phase;
            lastRenderedMenuState = menuState;
            refreshMenu();
        }
    }

    private void refreshLog() {
        StringBuilder builder = new StringBuilder();
        List<String> logLines = battleModel.getBattleLog();
        int startIndex = Math.max(0, logLines.size() - 5);
        for (int i = startIndex; i < logLines.size(); i++) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(logLines.get(i));
        }
        logLabel.setText(builder.toString());
        if (logScroll != null) {
            logScroll.layout();
            logScroll.setScrollPercentY(1f);
        }
    }

    private void refreshStatus() {
        statusLabel.setText(buildStatusText());
    }

    private void finishBattle(boolean victory) {
        if (victory) {
            battleModel.awardXpToPlayers();
            if (encounterService != null && encounter != null) {
                encounterService.removeEncounter(encounter);
            }
        }
        if (victory) {
            gameModel.setActiveBattleModel(null);

            // Se era una boss battle, completa il livello e mostra la schermata di recap
            if (battleModel.wasBossBattle()) {
                // Salva il nome del livello completato prima che il FlowController avanzi l'ID
                String levelName = null;
                if (gameContext.getSceneController().getActiveScene() != null) {
                    levelName = gameContext.getSceneController().getActiveScene().getName();
                }

                gameModel.setMessage("Boss sconfitto! Livello completato!");
                gameContext.getFlowController().completeCurrentLevel();

                // Dispose della vecchia LevelScreen (risorse native: mappa, fisica, sprite)
                if (returnScreen != null) {
                    returnScreen.dispose();
                }

                boolean isGameCompleted = gameModel.getGameState().getPhase() == GameState.Phase.GAME_COMPLETED;
                game.setScreen(new LevelCompletedScreen(
                        game, gameContext, isGameCompleted, battleModel, levelName));
                return;
            }

            gameModel.getGameState().setPhase(GameState.Phase.PLAYING);
            gameModel.setMessage("Vittoria! +" + battleModel.getTotalXpEarned() + " XP");
            game.setScreen(returnScreen);
            return;
        }

        if (battleModel.getPhase() == BattlePhase.FLED) {
            gameModel.setActiveBattleModel(null);
            gameModel.getGameState().setPhase(GameState.Phase.PLAYING);
            gameModel.setMessage("Sei fuggito dalla battaglia.");
            returnScreen.startEncounterCooldown(3f);
            game.setScreen(returnScreen);
            return;
        }

        if (battleModel.isDefeat()) {
            new GameOverService().enterGameOver(gameModel);
            game.setScreen(new GameOverScreen(game, gameContext));
            return;
        }

        gameModel.setActiveBattleModel(null);
        gameModel.getGameState().setPhase(GameState.Phase.PLAYING);
        game.setScreen(returnScreen);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.32f, 0.56f, 0.72f, 1f);

        BattlePhase phaseBeforeUpdate = battleModel.getPhase();
        if (phaseBeforeUpdate == BattlePhase.ENEMY_TURN && enemyTurnDelayTimer > 0f) {
            enemyTurnDelayTimer = Math.max(0f, enemyTurnDelayTimer - delta);
            battleModel.updateFleeTimer(delta);
        } else {
            if (phaseBeforeUpdate == BattlePhase.ENEMY_TURN && enemyAttackTimer <= 0f) {
                enemyAttackTimer = EnemyBattleAssets.ATTACK_DURATION;
                enemyAttackStateTime = 0f;
                triggerEnemyTwoOrbAttacks();
            }
            battleController.update(delta);
        }
        BattlePhase phaseAfterUpdate = battleModel.getPhase();

        if (phaseBeforeUpdate == BattlePhase.ENEMY_TURN && phaseAfterUpdate != BattlePhase.ENEMY_TURN) {
            enemyTurnDelayTimer = 0f;
            refreshBattleDisplay();
            refreshMenuIfNeeded(true);
            if (battleModel.isBattleOver()) {
                finishBattle(battleModel.isVictory());
                return;
            }
        }

        updateFleeButton();

        if (battleModel.getPhase() == BattlePhase.FLED) {
            finishBattle(false);
            return;
        }
        if (battleModel.isBattleOver()) {
            finishBattle(battleModel.isVictory());
            return;
        }

        battleTime += delta;
        if (playerAttackTimer > 0f) {
            playerAttackStateTime += delta;
        }
        playerAttackTimer = Math.max(0f, playerAttackTimer - delta);
        if (playerAttackTimer <= 0f) {
            activePlayerAttacker = null;
        }
        if (playerSpecialTimer > 0f) {
            playerSpecialStateTime += delta;
        }
        playerSpecialTimer = Math.max(0f, playerSpecialTimer - delta);
        if (playerSpecialTimer <= 0f) {
            activePlayerSpecialAttacker = null;
        }
        if (enemyAttackTimer > 0f) {
            enemyAttackStateTime += delta;
        }
        enemyAttackTimer = Math.max(0f, enemyAttackTimer - delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (combatLayer != null) {
            combatLayer.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        }
        if (effectsLayer != null) {
            effectsLayer.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        }
        if (root != null) {
            buildHudLayout();
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
        RunMusicManager.play();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (momSlipperTexture != null) {
            momSlipperTexture.dispose();
        }
        if (alienOrbTexture != null) {
            alienOrbTexture.dispose();
        }
        disposePlayerAssets();
        disposeEnemyAssets();
    }

    private void disposePlayerAssets() {
        for (PlayerBattleAssets assets : new ArrayList<>(playerAssets.values())) {
            assets.dispose();
        }
        playerAssets.clear();
        playerActors.clear();
    }

    private void disposeEnemyAssets() {
        for (EnemyBattleAssets assets : new ArrayList<>(enemyAssets.values())) {
            assets.dispose();
        }
        enemyAssets.clear();
        enemyActors.clear();
    }

    private static final class PlayerBattleAssets {
        private static final float ATTACK_DURATION = 0.75f;

        private final List<Texture> loadedTextures = new ArrayList<>();
        private final Animation<TextureRegion> idleAnim;
        private final Animation<TextureRegion> attackAnim;
        private final Animation<TextureRegion> specialAnim;
        private final float displaySize;

        private PlayerBattleAssets(Player player) {
            String basePath = resolveBasePath(player);
            this.idleAnim = JsonAnimationLoader.load(basePath + "/idle_right", 0.1f, loadedTextures, false);
            this.attackAnim = JsonAnimationLoader.load(basePath + "/attack_right",
                    ATTACK_FRAME_DURATION, loadedTextures, false);
            this.specialAnim = loadSpecialAnimation(player, loadedTextures);
            this.displaySize = resolveDisplaySize(player);
        }

        private static Animation<TextureRegion> loadSpecialAnimation(Player player, List<Texture> loadedTextures) {
            if (player == null || !"mamma".equals(player.getCharacterId())
                    || !Gdx.files.internal(MOM_SPECIAL_SHEET_PATH).exists()) {
                return null;
            }

            Texture sheet = new Texture(Gdx.files.internal(MOM_SPECIAL_SHEET_PATH));
            sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            loadedTextures.add(sheet);

            Array<TextureRegion> frames = new Array<>(MOM_SPECIAL_COLUMNS * MOM_SPECIAL_ROWS);
            for (int row = 0; row < MOM_SPECIAL_ROWS; row++) {
                for (int column = 0; column < MOM_SPECIAL_COLUMNS; column++) {
                    frames.add(new TextureRegion(
                            sheet,
                            column * MOM_SPECIAL_FRAME_SIZE,
                            row * MOM_SPECIAL_FRAME_SIZE,
                            MOM_SPECIAL_FRAME_SIZE,
                            MOM_SPECIAL_FRAME_SIZE));
                }
            }
            return new Animation<>(MOM_SPECIAL_FRAME_DURATION, frames, Animation.PlayMode.NORMAL);
        }

        private static String resolveBasePath(Player player) {
            String characterId = player != null ? player.getCharacterId() : "bambino";
            if ("papa".equals(characterId)) {
                return "characters/battle-sidescroller/father";
            }
            if ("mamma".equals(characterId)) {
                return "characters/battle-sidescroller/mom";
            }
            if ("nonno".equals(characterId)) {
                return "characters/battle-sidescroller/nonno";
            }
            return "characters/battle-sidescroller/child";
        }

        private static float resolveDisplaySize(Player player) {
            String characterId = player != null ? player.getCharacterId() : "bambino";
            if ("nonno".equals(characterId)) {
                return 275f;
            }
            if ("bambino".equals(characterId)) {
                return 230f;
            }
            return 310f;
        }

        private Animation<TextureRegion> getIdleAnim() {
            return idleAnim;
        }

        private Animation<TextureRegion> getAttackAnim() {
            return attackAnim;
        }

        private Animation<TextureRegion> getSpecialAnim() {
            return specialAnim;
        }

        private float getDisplaySize() {
            return displaySize;
        }

        private void dispose() {
            for (Texture texture : loadedTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            loadedTextures.clear();
        }
    }

    private static final class EnemyBattleAssets {
        private static final float ATTACK_DURATION = 0.8f;

        private final List<Texture> loadedTextures = new ArrayList<>();
        private final Animation<TextureRegion> idleAnim;
        private final Animation<TextureRegion> attackAnim;
        private final float displaySize;

        private EnemyBattleAssets(Enemy enemy) {
            String basePath = resolveBasePath(enemy);
            this.idleAnim = JsonAnimationLoader.load(basePath + "/idle_right", 0.1f, loadedTextures, true);
            this.attackAnim = JsonAnimationLoader.load(basePath + "/attack_right",
                    ATTACK_FRAME_DURATION, loadedTextures, true);
            this.displaySize = resolveDisplaySize(basePath);
        }

        private static String resolveBasePath(Enemy enemy) {
            if (enemy == null) {
                return "characters/battle-sidescroller/enemy-2";
            }
            String enemyId = enemy.getEnemyId();
            if ("boss_livello_3".equals(enemyId)) {
                return "characters/battle-sidescroller/enemy-3";
            }
            if ("alieno_guardiano".equals(enemyId)
                    || "boss_livello_1".equals(enemyId)
                    || "boss_livello_2".equals(enemyId)) {
                return "characters/battle-sidescroller/enemy-1";
            }
            return "characters/battle-sidescroller/enemy-2";
        }

        private static float resolveDisplaySize(String basePath) {
            if (basePath.endsWith("enemy-1")) {
                return 390f;
            }
            if (basePath.endsWith("enemy-3")) {
                return 420f;
            }
            return 330f;
        }

        private Animation<TextureRegion> getIdleAnim() {
            return idleAnim;
        }

        private Animation<TextureRegion> getAttackAnim() {
            return attackAnim;
        }

        private float getDisplaySize() {
            return displaySize;
        }

        private void dispose() {
            for (Texture texture : loadedTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            loadedTextures.clear();
        }
    }
}
