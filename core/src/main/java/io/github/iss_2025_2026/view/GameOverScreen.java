package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.controller.GameOverController;
import io.github.iss_2025_2026.service.GameOverResult;
import io.github.iss_2025_2026.service.RunMusicManager;

/**
 * Schermata di Game Over: sfondo nero e ripristino dall'ultimo salvataggio.
 */
public class GameOverScreen implements Screen {
    private static final float AUTO_CONTINUE_DELAY = 3f;

    private final Main game;
    private final GameOverController controller;
    private final InputAdapter inputListener;

    private Stage stage;
    private Skin skin;
    private Label subtitleLabel;
    private float continueTimer;
    private boolean continued;

    public GameOverScreen(Main game, GameContext gameContext) {
        this.game = game;
        this.controller = gameContext.createGameOverController();
        this.continueTimer = AUTO_CONTINUE_DELAY;

        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    continueFromCheckpoint();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void show() {
        skin = GameUiTheme.loadSkin();
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

        Label title = new Label("GAME OVER", skin, GameUiTheme.LABEL_TITLE);
        title.setColor(Color.WHITE);
        root.add(title).padBottom(GameUiTheme.SPACE_4).row();

        subtitleLabel = new Label(buildSubtitle(), skin, GameUiTheme.LABEL_MUTED);
        subtitleLabel.setWrap(true);
        subtitleLabel.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        root.add(subtitleLabel).width(640f).padBottom(GameUiTheme.SPACE_3).row();

        Label hint = new Label("Premi INVIO o SPAZIO per continuare", skin, GameUiTheme.LABEL_BODY);
        hint.setColor(Color.LIGHT_GRAY);
        root.add(hint).row();
    }

    private String buildSubtitle() {
        if (controller.getModel().getGameState().getLastCheckpointId() != null) {
            return "Riprendi dall'ultimo checkpoint salvato...";
        }
        return "Nessun checkpoint trovato. La squadra verra rinforzata...";
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        continueTimer -= delta;
        if (!continued && continueTimer <= 0f) {
            continueFromCheckpoint();
        }

        stage.act(delta);
        stage.draw();
    }

    private void continueFromCheckpoint() {
        if (continued) {
            return;
        }
        continued = true;

        GameOverResult result = controller.onContinue();
        controller.getModel().setMessage(result.getMessage());
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
    }
}
