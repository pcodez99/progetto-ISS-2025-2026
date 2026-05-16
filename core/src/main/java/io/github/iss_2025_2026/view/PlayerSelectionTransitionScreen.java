package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.service.MenuMusicManager;

public class PlayerSelectionTransitionScreen implements Screen {
    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final NewGameConfigModel configModel;
    private final int nextPlayerIndex;

    private Stage stage;
    private Skin skin;
    private Texture background;

    public PlayerSelectionTransitionScreen(Main game, GameModel gameModel, GameController gameController,
            NewGameConfigModel configModel, int nextPlayerIndex) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.configModel = configModel;
        this.nextPlayerIndex = nextPlayerIndex;

        this.skin = GameUiTheme.loadSkin();
        this.background = new Texture(Gdx.files.internal("select-character-bg.png").exists()
                ? Gdx.files.internal("select-character-bg.png")
                : Gdx.files.internal("background_init.png"));

        buildUI();
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = GameUiFactory.createScreenRoot(stage, background, skin);
        Table shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_5);
        shell.defaults().growX().left();

        String previousCharacter = configModel.getSelectedCharacterPlayerOne();
        shell.add(GameUiFactory.createHeroBlock(
                skin,
                "PASSAGGIO TURNO",
                "Ora tocca al Giocatore " + nextPlayerIndex,
                "Il Giocatore 1 ha confermato " + previousCharacter + ". Passa il controllo e continua con la seconda scelta."))
                .width(620f).padBottom(GameUiTheme.SPACE_5).row();

        Table infoPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        infoPanel.defaults().left().growX();
        infoPanel.add(GameUiFactory.createChip(skin, "Giocatore 1 pronto")).padBottom(GameUiTheme.SPACE_2).row();
        infoPanel.add(GameUiFactory.createBodyLabel(
                "Quando premi continua si apre la selezione dedicata al Giocatore " + nextPlayerIndex + ".", skin))
                .row();
        shell.add(infoPanel).width(640f).padBottom(GameUiTheme.SPACE_4).row();

        TextButton backBtn = GameUiFactory.createButton("Modifica Giocatore 1", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectionScreen(game, gameModel, gameController, configModel, 1));
                dispose();
            }
        });

        TextButton continueBtn = GameUiFactory.createButton(
                "Vai alla scelta Giocatore " + nextPlayerIndex, skin, GameUiTheme.BUTTON_PRIMARY);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectionScreen(game, gameModel, gameController, configModel,
                        nextPlayerIndex));
                dispose();
            }
        });

        Table buttons = new Table();
        buttons.add(backBtn).width(220f).height(56f).padRight(GameUiTheme.SPACE_2);
        buttons.add(continueBtn).width(280f).height(56f);
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
