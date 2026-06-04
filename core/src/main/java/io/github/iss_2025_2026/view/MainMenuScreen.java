package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.controller.MainMenuController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.MainMenuModel;
import io.github.iss_2025_2026.service.MenuMusicManager;

public class MainMenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private final boolean isRunning;
    private final GameModel gameModel;
    private Label statusLabel;

    private final MainMenuModel menuModel;
    private final MainMenuController menuController;

    public MainMenuScreen(Main game, GameModel model, GameController controller) {
        this(game, model, controller, false, null);
    }

    public MainMenuScreen(Main game, GameModel model, GameController controller, boolean isRunning,
            Screen resumeScreen) {
        this.isRunning = isRunning;
        this.gameModel = model;
        // Menu-specific MVC components
        this.menuModel = new MainMenuModel();
        this.menuController = new MainMenuController(game, model, controller, isRunning, resumeScreen);

        // Carichiamo gli asset in cartella
        this.skin = GameUiTheme.loadSkin();
        this.backgroundTexture = new Texture(Gdx.files.internal("background_init.png"));

        createUI();
    }

    public void createUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = GameUiFactory.createScreenRoot(stage, backgroundTexture, skin);
        Table shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_6);
        shell.defaults().growX();

        shell.add(GameUiFactory.createHeroBlock(
                skin,
                isRunning ? "PAUSA" : "Menù principale",
                isRunning ? "Gioco in pausa" : "VIDDANI VS ALIENI",
                isRunning
                        ? "Riprendi la run oppure torna al menu principale senza riaprire la configurazione della partita."
                        : "Difendi la Terra con i tuoi viddani tra campagna, invasioni e abilita speciali."
        )).width(560f).padBottom(GameUiTheme.SPACE_5).row();

        if (!isRunning) {
            Table chips = new Table();
            chips.left();
            shell.add(chips).padBottom(GameUiTheme.SPACE_5).row();
        }

        Table actions = new Table();
        actions.defaults().width(320f).padBottom(GameUiTheme.SPACE_2);

        for (MainMenuModel.MenuAction action : menuModel.getAvailableActions(isRunning)) {
            String styleName = action == MainMenuModel.MenuAction.NEW_GAME
                    || action == MainMenuModel.MenuAction.CONTINUE_GAME
                    ? GameUiTheme.BUTTON_PRIMARY
                    : GameUiTheme.BUTTON_SECONDARY;
            TextButton button = GameUiFactory.createButton(formatAction(action), skin, styleName);

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    menuController.handleMenuAction(action);
                    if (action == MainMenuModel.MenuAction.SAVE_GAME) {
                        updateStatusLabel();
                    }
                }
            });

            actions.add(button).height(58f).row();
        }

        shell.add(actions).left().row();

        if (isRunning) {
            statusLabel = new Label("", skin, GameUiTheme.LABEL_MUTED);
            statusLabel.setWrap(true);
            updateStatusLabel();
            shell.add(statusLabel).width(520f).padTop(GameUiTheme.SPACE_2).row();
        }

        Container<Table> shellWrap = new Container<>(shell);
        shellWrap.width(760f);
        root.add(shellWrap).center();
    }

    private String formatAction(MainMenuModel.MenuAction action) {
        switch (action) {
            case CONTINUE_GAME:
                return "Continua a giocare";
            case SAVE_GAME:
                return "Salva partita";
            case NEW_GAME:
                return "Nuova Partita";
            case LOAD_GAME:
                return "Carica Partita";
            case SETTINGS:
                return "Impostazioni";
            case RETURN_TO_MAIN_MENU:
                return "Torna al menu principale";
            case EXIT:
                return "Esci";
            default:
                return action.name().replace("_", " ");
        }
    }

    private void updateStatusLabel() {
        if (statusLabel == null || gameModel == null) {
            return;
        }
        statusLabel.setText(gameModel.getMessage() != null ? gameModel.getMessage() : "");
        if (gameModel.getMessage() != null && gameModel.getMessage().toLowerCase().contains("fallito")) {
            statusLabel.setColor(GameUiTheme.DANGER);
        } else {
            statusLabel.setColor(GameUiTheme.SUCCESS);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    // Metodi obbligatori dell'interfaccia Screen
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        MenuMusicManager.play();
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
        CursorHoverUtil.resetDefaultCursor();
        MenuMusicManager.pause();
        if (isRunning) {
            dispose();
        }
    }

    @Override
    public void dispose() {
        CursorHoverUtil.resetDefaultCursor();
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
    }
}
