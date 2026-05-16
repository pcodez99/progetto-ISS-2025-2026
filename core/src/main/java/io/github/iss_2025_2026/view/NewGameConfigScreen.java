package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.service.MenuMusicManager;

public class NewGameConfigScreen implements Screen {

    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final NewGameConfigModel configModel;

    private Stage stage;
    private Skin skin;
    private Texture background;

    public NewGameConfigScreen(Main game, GameModel gameModel, GameController gameController) {
        this(game, gameModel, gameController, null);
    }

    public NewGameConfigScreen(Main game, GameModel gameModel, GameController gameController,
            NewGameConfigModel existingConfig) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.configModel = existingConfig != null ? existingConfig : new NewGameConfigModel();

        this.skin = GameUiTheme.loadSkin();
        this.background = new Texture(Gdx.files.internal("background_init.png"));

        buildUI();
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = GameUiFactory.createScreenRoot(stage, background, skin);
        Table shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_5);
        shell.defaults().growX().left();

        shell.add(GameUiFactory.createHeroBlock(
                skin,
                "SETUP PARTITA",
                "Configura la tua run",
                "Scegli nome del salvataggio e modalita prima di passare alla selezione del personaggio."))
                .width(620f).padBottom(GameUiTheme.SPACE_5).row();

        Table formPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        formPanel.defaults().left().growX();

        formPanel.add(new Label("Parametri iniziali", skin, GameUiTheme.LABEL_SECTION))
                .padBottom(GameUiTheme.SPACE_1).row();
        formPanel.add(GameUiFactory.createMutedLabel(
                "Dai un nome alla partita e scegli la modalita prima di passare al roster.", skin))
                .width(560f).padBottom(GameUiTheme.SPACE_4).row();

        Label nameLabel = new Label("Nome salvataggio", skin, GameUiTheme.LABEL_BODY);
        final TextField nameField = new TextField(configModel.getGameName(), skin, GameUiTheme.TEXT_FIELD_GAME);
        nameField.setMessageText("Es. Difesa di Viddana");
        nameField.setAlignment(Align.center);
        float nameFieldInset = Math.max(14f, nameField.getStyle().background.getLeftWidth());
        formPanel.add(nameLabel).padLeft(nameFieldInset).padBottom(GameUiTheme.SPACE_1).row();
        formPanel.add(GameUiFactory.createMutedLabel(
                "Usa un nome corto e riconoscibile: apparira come titolo del salvataggio.", skin))
                .width(560f).padLeft(nameFieldInset).padBottom(GameUiTheme.SPACE_2).row();
        formPanel.add(nameField).width(560f).height(68f).padBottom(GameUiTheme.SPACE_4).row();

        Label modeLabel = new Label("Modalita di gioco", skin, GameUiTheme.LABEL_BODY);
        final SelectBox<NewGameConfigModel.GameMode> modeSelect = new SelectBox<>(skin, GameUiTheme.SELECT_BOX_GAME);
        modeSelect.setItems(NewGameConfigModel.GameMode.values());
        modeSelect.setSelected(configModel.getGameMode());
        modeSelect.setAlignment(Align.center);
        modeSelect.getList().setAlignment(Align.left);
        modeSelect.getScrollPane().setFadeScrollBars(false);
        float modeSelectInset = Math.max(14f, modeSelect.getStyle().background.getLeftWidth());
        formPanel.add(modeLabel).padLeft(modeSelectInset).padBottom(GameUiTheme.SPACE_1).row();
        formPanel.add(GameUiFactory.createMutedLabel(
                "Single player per una run solitaria, multiplayer per due selezioni personaggio consecutive.", skin))
                .width(560f).padLeft(modeSelectInset).padBottom(GameUiTheme.SPACE_2).row();
        formPanel.add(modeSelect).width(560f).height(68f).padBottom(GameUiTheme.SPACE_6).row();

        shell.add(formPanel).width(640f).padBottom(GameUiTheme.SPACE_4).row();

        TextButton backBtn = GameUiFactory.createButton("Indietro", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });

        TextButton nextBtn = GameUiFactory.createButton("Scegli Personaggio", skin, GameUiTheme.BUTTON_PRIMARY);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                configModel.setGameName(nameField.getText());
                configModel.setGameMode(modeSelect.getSelected());
                configModel.clearCharacterSelections();
                game.setScreen(new CharacterSelectionScreen(game, gameModel, gameController, configModel, 1));
                dispose();
            }
        });

        Table buttons = new Table();
        buttons.add(backBtn).width(180f).height(56f).padRight(GameUiTheme.SPACE_2);
        buttons.add(nextBtn).width(240f).height(56f);
        shell.add(buttons).left();

        Container<Table> shellWrap = new Container<>(shell);
        shellWrap.width(760f);
        root.add(shellWrap).center();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

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
    }

    @Override
    public void dispose() {
        CursorHoverUtil.resetDefaultCursor();
        stage.dispose();
        skin.dispose();
        background.dispose();
    }
}
