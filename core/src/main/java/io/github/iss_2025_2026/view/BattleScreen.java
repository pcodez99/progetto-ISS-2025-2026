package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
import io.github.iss_2025_2026.service.EnemyEncounter;
import io.github.iss_2025_2026.service.EnemyEncounterService;
import io.github.iss_2025_2026.service.GameOverService;
import io.github.iss_2025_2026.service.RunMusicManager;
import java.util.List;

/**
 * Schermata di combattimento a turni in stile Final Fantasy.
 */
public class BattleScreen implements Screen {
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
    private Table root;
    private Table enemiesRow;
    private Table playersRow;
    private Table menuPanel;
    private Table logPanel;
    private Label logLabel;
    private Label fleeLabel;
    private Label statusLabel;
    private BattlePhase lastRenderedPhase;
    private BattleController.MenuState lastRenderedMenuState;
    private final InputAdapter inputListener;

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
        backgroundTexture = new Texture(Gdx.files.internal("map/levels/1/Arena_level_1.png"));
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
        backgroundImage.setScaling(Scaling.stretch);
        stage.addActor(backgroundImage);

        root = new Table();
        root.setFillParent(true);
        root.top().pad(GameUiTheme.SPACE_4);
        stage.addActor(root);

        statusLabel = new Label(buildStatusText(), skin, GameUiTheme.LABEL_SECTION);
        statusLabel.setWrap(true);
        root.add(statusLabel).growX().padBottom(GameUiTheme.SPACE_3).row();

        enemiesRow = new Table();
        refreshEnemyPanels();
        root.add(enemiesRow).growX().padBottom(GameUiTheme.SPACE_3).row();

        logPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        logLabel = new Label("", skin, GameUiTheme.LABEL_BODY);
        logLabel.setWrap(true);
        ScrollPane logScroll = new ScrollPane(logLabel, skin);
        logScroll.setFadeScrollBars(false);
        logPanel.add(logScroll).grow().height(140f);
        root.add(logPanel).growX().height(160f).padBottom(GameUiTheme.SPACE_3).row();

        playersRow = new Table();
        refreshPlayerPanels();
        root.add(playersRow).growX().padBottom(GameUiTheme.SPACE_3).row();

        menuPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_2);
        fleeLabel = new Label("[Q] Fuggi (10s)", skin, GameUiTheme.LABEL_MUTED);
        menuPanel.add(fleeLabel).left().padBottom(GameUiTheme.SPACE_2).row();
        root.add(menuPanel).growX().bottom();
        lastRenderedPhase = battleModel.getPhase();
        lastRenderedMenuState = battleController.getMenuState();
        refreshMenu();
        refreshLog();
    }

    private void refreshEnemyPanels() {
        enemiesRow.clearChildren();
        for (Enemy enemy : battleModel.getAliveEnemies()) {
            enemiesRow.add(createEnemyPanel(enemy)).pad(GameUiTheme.SPACE_2);
        }
        if (battleModel.getAliveEnemies().isEmpty()) {
            enemiesRow.add(new Label("Nessun nemico", skin, GameUiTheme.LABEL_MUTED)).pad(GameUiTheme.SPACE_2);
        }
    }

    private void refreshPlayerPanels() {
        playersRow.clearChildren();
        playersRow.add(createPlayerPanel(battleModel.getPlayerOne())).pad(GameUiTheme.SPACE_2);
        if (battleModel.getPlayerTwo() != null) {
            playersRow.add(createPlayerPanel(battleModel.getPlayerTwo())).pad(GameUiTheme.SPACE_2);
        }
    }

    private void refreshBattleDisplay() {
        refreshEnemyPanels();
        refreshPlayerPanels();
        refreshLog();
        refreshStatus();
    }

    private Table createEnemyPanel(Enemy enemy) {
        Table panel = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_2);
        panel.add(new Label(enemy.getName(), skin, GameUiTheme.LABEL_SECTION)).row();
        panel.add(new Label("HP: " + enemy.getHp() + "/" + enemy.getMaxHp(), skin, GameUiTheme.LABEL_BODY)).row();
        return panel;
    }

    private Table createPlayerPanel(Player player) {
        Table panel = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_2);
        panel.add(new Label(player.getName(), skin, GameUiTheme.LABEL_SECTION)).row();
        panel.add(new Label("HP: " + player.getHp() + "/" + player.getMaxHp(), skin, GameUiTheme.LABEL_BODY)).row();
        panel.add(new Label("Livello: " + player.getLevel(), skin, GameUiTheme.LABEL_MUTED)).row();
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
        menuPanel.add(fleeLabel).left().padBottom(GameUiTheme.SPACE_2).row();
        fleeLabel.setText(String.format("[Q] Fuggi (%.0fs)", Math.max(0f, battleModel.getFleeTimer())));

        BattleController.MenuState menuState = battleController.getMenuState();
        if (menuState == BattleController.MenuState.TARGET_SELECTION) {
            menuPanel.add(new Label("Seleziona bersaglio:", skin, GameUiTheme.LABEL_BODY)).left().padBottom(GameUiTheme.SPACE_1).row();
            for (Enemy enemy : battleModel.getAliveEnemies()) {
                menuPanel.add(createTargetButton(enemy)).left().padBottom(GameUiTheme.SPACE_1).row();
            }
            menuPanel.add(createBackButton()).left().row();
            return;
        }

        if (menuState == BattleController.MenuState.INVENTORY_SELECTION) {
            menuPanel.add(new Label("Inventario:", skin, GameUiTheme.LABEL_BODY)).left().padBottom(GameUiTheme.SPACE_1).row();
            Player current = battleModel.getCurrentTurnPlayer();
            List<Collectible> items = current.getBackpack().getItems();
            if (items.isEmpty()) {
                menuPanel.add(new Label("Zaino vuoto", skin, GameUiTheme.LABEL_MUTED)).left().padBottom(GameUiTheme.SPACE_1).row();
            } else {
                for (Collectible item : items) {
                    menuPanel.add(createItemButton(item)).left().padBottom(GameUiTheme.SPACE_1).row();
                }
            }
            menuPanel.add(createBackButton()).left().row();
            return;
        }

        if (!isPlayerTurn()) {
            menuPanel.add(new Label("Attendi il turno nemico...", skin, GameUiTheme.LABEL_MUTED)).left().row();
            return;
        }

        menuPanel.add(createMenuButton("Attacco", this::onAttack)).left().padBottom(GameUiTheme.SPACE_1).row();

        // Crea il bottone dell'abilità speciale e lo disabilita se già usato
        TextButton specialAbilityButton = createMenuButton("Abilita Speciale", this::onSpecial);
        Player currentPlayer = battleModel.getCurrentTurnPlayer();
        if (currentPlayer != null && currentPlayer.hasUsedSpecialAbilityThisBattle()) {
            specialAbilityButton.setDisabled(true);
        }
        menuPanel.add(specialAbilityButton).left().padBottom(GameUiTheme.SPACE_1).row();

        menuPanel.add(createMenuButton("Inventario", this::onInventory)).left().row();
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

    private TextButton createTargetButton(Enemy enemy) {
        TextButton button = GameUiFactory.createButton(enemy.getName(), skin, GameUiTheme.BUTTON_PRIMARY);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
                battleController.onItemSelected(item, fallbackTarget);
                afterPlayerAction();
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
        if (battleModel.getCurrentTurnPlayer().getAbility() == null) {
            return;
        }
        battleController.onSpecialAbilitySelected();
        afterPlayerAction();
    }

    private void onInventory() {
        battleController.onInventorySelected();
        refreshMenuIfNeeded(true);
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
        refreshMenuIfNeeded(true);
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
        for (String line : battleModel.getBattleLog()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line);
        }
        logLabel.setText(builder.toString());
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
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        BattlePhase phaseBeforeUpdate = battleModel.getPhase();
        battleController.update(delta);
        BattlePhase phaseAfterUpdate = battleModel.getPhase();

        if (phaseBeforeUpdate == BattlePhase.ENEMY_TURN && phaseAfterUpdate != BattlePhase.ENEMY_TURN) {
            refreshBattleDisplay();
            refreshMenuIfNeeded(true);
            if (battleModel.isBattleOver()) {
                finishBattle(battleModel.isVictory());
                return;
            }
        }

        fleeLabel.setText(String.format("[Q] Fuggi (%.0fs)", Math.max(0f, battleModel.getFleeTimer())));

        if (battleModel.getPhase() == BattlePhase.FLED) {
            finishBattle(false);
            return;
        }
        if (battleModel.isBattleOver()) {
            finishBattle(battleModel.isVictory());
            return;
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
    }
}
