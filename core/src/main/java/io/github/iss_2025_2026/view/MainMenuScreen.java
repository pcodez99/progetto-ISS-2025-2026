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

import io.github.iss_2025_2026.controller.MainMenuController;
import io.github.iss_2025_2026.model.MainMenuModel;

public class MainMenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private final MainMenuModel model;
    private final MainMenuController controller;

    public MainMenuScreen(MainMenuModel model, MainMenuController controller) {
        this.batch = new SpriteBatch();
        this.model = model;
        this.controller = controller;

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

        for (MainMenuModel.MenuAction action : model.getAvailableActions()) {
            TextButton button = new TextButton(action.name().replace("_", " "), skin);

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    controller.handleMenuAction(action);
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
