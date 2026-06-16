package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.service.RunMusicManager;

/**
 * Schermata intermedia mostrata dopo aver sconfitto il boss di un livello.
 * Mostra un recap della battaglia (XP guadagnati, stato dei giocatori)
 * e permette al giocatore di proseguire al livello successivo o al menu principale.
 */
public class LevelCompletedScreen implements Screen {
    private static final float AUTO_CONTINUE_DELAY = 6f;

    private final Main game;
    private final GameContext gameContext;
    private final GameModel gameModel;
    private final boolean isGameCompleted;
    private final int xpEarned;
    private final String completedLevelName;
    private final InputAdapter inputListener;

    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private float continueTimer;
    private boolean transitioned;

    public LevelCompletedScreen(Main game, GameContext gameContext, boolean isGameCompleted,
            BattleModel battleModel, String completedLevelName) {
        this.game = game;
        this.gameContext = gameContext;
        this.gameModel = gameContext.getModel();
        this.isGameCompleted = isGameCompleted;
        this.xpEarned = battleModel != null ? battleModel.getTotalXpEarned() : 0;
        this.completedLevelName = completedLevelName != null ? completedLevelName : "Livello";
        this.continueTimer = AUTO_CONTINUE_DELAY;

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    proceed();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void show() {
        skin = GameUiTheme.loadSkin();
        shapeRenderer = new ShapeRenderer();
        stage = new Stage(new ScreenViewport());
        buildUi();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputListener);
        Gdx.input.setInputProcessor(multiplexer);

        RunMusicManager.pause();
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // Titolo principale
        String titleText = isGameCompleted ? "GIOCO COMPLETATO!" : "LIVELLO COMPLETATO!";
        Label titleLabel = new Label(titleText, skin, GameUiTheme.LABEL_TITLE);
        titleLabel.setColor(GameUiTheme.ACCENT_LIME);
        root.add(titleLabel).padBottom(GameUiTheme.SPACE_2).row();

        // Sottotitolo con il nome del livello completato
        Label levelNameLabel = new Label(completedLevelName, skin, GameUiTheme.LABEL_SECTION);
        levelNameLabel.setColor(GameUiTheme.ACCENT_AMBER);
        root.add(levelNameLabel).padBottom(GameUiTheme.SPACE_5).row();

        // Pannello recap
        Table recapPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        recapPanel.defaults().left().padBottom(GameUiTheme.SPACE_2);

        // XP guadagnati
        Table xpRow = new Table();
        xpRow.add(new Label("XP Guadagnati: ", skin, GameUiTheme.LABEL_BODY)).left();
        Label xpValue = new Label("+" + xpEarned, skin, GameUiTheme.LABEL_BODY);
        xpValue.setColor(GameUiTheme.ACCENT_LIME);
        xpRow.add(xpValue).left();
        recapPanel.add(xpRow).growX().row();

        // Stato dei giocatori
        Player p1 = gameModel.getPlayerOne();
        if (p1 != null) {
            recapPanel.add(createPlayerRecap(p1)).growX().row();
        }
        Player p2 = gameModel.getPlayerTwo();
        if (gameModel.isMultiplayerGame() && p2 != null) {
            recapPanel.add(createPlayerRecap(p2)).growX().row();
        }

        // Livelli completati
        int completedCount = gameModel.getGameState().getCompletedLevelIds().size();
        Label completedLabel = new Label("Livelli completati: " + completedCount, skin, GameUiTheme.LABEL_MUTED);
        recapPanel.add(completedLabel).growX().row();

        root.add(recapPanel).width(520f).padBottom(GameUiTheme.SPACE_5).row();

        // Pulsante per proseguire
        String buttonText = isGameCompleted ? "Torna al Menu" : "Prossimo Livello";
        TextButton continueButton = GameUiFactory.createButton(buttonText, skin, GameUiTheme.BUTTON_PRIMARY);
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                proceed();
            }
        });
        root.add(continueButton).width(320f).height(56f).padBottom(GameUiTheme.SPACE_3).row();

        // Hint
        Label hintLabel = new Label("Premi INVIO o SPAZIO per continuare", skin, GameUiTheme.LABEL_MUTED);
        hintLabel.setColor(new Color(0.6f, 0.65f, 0.68f, 1f));
        root.add(hintLabel).row();

        // Animazione fade-in
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(0.6f));
    }

    private Table createPlayerRecap(Player player) {
        Table row = new Table();
        row.defaults().left().padRight(GameUiTheme.SPACE_3);

        Label nameLabel = new Label(player.getName(), skin, GameUiTheme.LABEL_BODY);
        nameLabel.setColor(GameUiTheme.ACCENT_CYAN);
        row.add(nameLabel);

        String hpText = "HP: " + player.getHp() + "/" + player.getMaxHp();
        Label hpLabel = new Label(hpText, skin, GameUiTheme.LABEL_BODY);
        if (player.getHp() <= player.getMaxHp() * 0.3f) {
            hpLabel.setColor(GameUiTheme.DANGER);
        } else {
            hpLabel.setColor(GameUiTheme.SUCCESS);
        }
        row.add(hpLabel);

        Label levelLabel = new Label("Lv. " + player.getLevel(), skin, GameUiTheme.LABEL_MUTED);
        row.add(levelLabel);

        return row;
    }

    private void proceed() {
        if (transitioned) {
            return;
        }
        transitioned = true;

        if (isGameCompleted) {
            // Gioco completato: torna al menu principale
            game.setScreen(new MainMenuScreen(game, gameModel, gameContext.getController()));
        } else {
            // Carica il livello successivo (l'ID è già stato avanzato da GameFlowController)
            gameContext.getFlowController().startCurrentLevel();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        drawBackground();

        if (!transitioned) {
            continueTimer -= delta;
            if (continueTimer <= 0f) {
                proceed();
            }
        }

        stage.act(delta);
        stage.draw();
    }

    private void drawBackground() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Sfondo scuro con sfumatura
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 1f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Sfumatura verde/ambrata per la vittoria
        if (isGameCompleted) {
            shapeRenderer.setColor(0.18f, 0.12f, 0.05f, 0.45f);
        } else {
            shapeRenderer.setColor(0.05f, 0.15f, 0.08f, 0.45f);
        }
        shapeRenderer.rect(0, Gdx.graphics.getHeight() * 0.3f,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.7f);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
