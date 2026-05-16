package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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

public class MainMenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch batch;

    private final Main game;
    private final GameModel model;
    private final GameController controller;

    private final MainMenuModel menuModel;
    private final MainMenuController menuController;

    public MainMenuScreen(Main game, GameModel model, GameController controller) {
        this.game = game;
        this.model = model;
        this.controller = controller;
        this.batch = new SpriteBatch();

        // Menu-specific MVC components
        this.menuModel = new MainMenuModel();
        this.menuController = new MainMenuController(game, model, controller);

        // Carichiamo gli asset in cartella
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.backgroundTexture = new Texture(Gdx.files.internal("background_init.png"));

        createUI();
    }

    public void createUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        for (MainMenuModel.MenuAction action : menuModel.getAvailableActions()) {
            TextButton button = new TextButton(action.name().replace("_", " "), skin);

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    menuController.handleMenuAction(action);
                }
            });

            table.add(button).width(300).pad(10);
            table.row();
        }
    }

    @Override
    public void render(float delta) {
        // Pulisce lo schermo prima di ogni frame
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    // Metodi obbligatori dell'interfaccia Screen
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        batch.dispose();
    }
}
