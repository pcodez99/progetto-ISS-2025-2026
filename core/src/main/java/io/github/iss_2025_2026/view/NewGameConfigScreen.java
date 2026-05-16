package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;

public class NewGameConfigScreen implements Screen {

    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final NewGameConfigModel configModel;

    private Stage stage;
    private Skin skin;
    private SpriteBatch batch;
    private Texture background;

    public NewGameConfigScreen(Main game, GameModel gameModel, GameController gameController) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.configModel = new NewGameConfigModel();

        this.batch = new SpriteBatch();
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.background = new Texture(Gdx.files.internal("background_init.png"));

        buildUI();
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Responsive background
        Image backgroundActor = new Image(background);
        backgroundActor.setFillParent(true);
        backgroundActor.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stage.addActor(backgroundActor);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("CONFIGURAZIONE NUOVA PARTITA", skin);
        root.add(title).colspan(2).padBottom(40).row();

        root.add(new Label("Nome Salvataggio:", skin)).left().padRight(20);
        final TextField nameField = new TextField(configModel.getGameName(), skin);
        root.add(nameField).width(300).padBottom(20).row();

        root.add(new Label("Modalità di Gioco:", skin)).left().padRight(20);
        final SelectBox<NewGameConfigModel.GameMode> modeSelect = new SelectBox<>(skin);
        modeSelect.setItems(NewGameConfigModel.GameMode.values());
        root.add(modeSelect).width(300).padBottom(40).row();

        TextButton backBtn = new TextButton("Indietro", skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });

        TextButton nextBtn = new TextButton("Avanti", skin);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                configModel.setGameName(nameField.getText());
                configModel.setGameMode(modeSelect.getSelected());
                game.setScreen(new CharacterSelectionScreen(game, gameModel, gameController, configModel));
                dispose();
            }
        });

        Table buttons = new Table();
        buttons.add(backBtn).width(150).padRight(20);
        buttons.add(nextBtn).width(150);
        root.add(buttons).colspan(2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
    }
}
