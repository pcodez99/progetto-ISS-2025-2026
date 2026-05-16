package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.RunMusicManager;

/**
 * Game View (Parte del pattern MVC).
 * Implementazione di {@link Screen} di LibGDX.
 * Si occupa esclusivamente del rendering dello stato del Model.
 * Delega la logica di aggiornamento e input al Controller.
 */
public class TestScreen implements Screen {
    private final GameModel model;
    private final GameController controller;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final InputAdapter inputListener;

    public TestScreen(Main game, GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);
        this.inputListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MainMenuScreen(game, model, controller, true, TestScreen.this));
                    return true;
                }

                return false;
            }
        };
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputListener);
        MenuMusicManager.pause();
        RunMusicManager.play();
    }

    @Override
    public void render(float delta) {
        // 1. Update logic (via Controller)
        controller.update(delta);

        // 2. Render (View)
        ScreenUtils.clear(Color.DARK_GRAY);

        batch.begin();
        font.draw(batch, model.getMessage(), 50, 400);
        font.draw(batch, "Timer: " + String.format("%.2f", model.getTimer()), 50, 350);
        font.draw(batch, "Partita: " + model.getGameName(), 50, 300);
        font.draw(batch, "Modalita: " + model.getGameMode(), 50, 250);

        Player playerOne = model.getPlayerOne();
        Player playerTwo = model.getPlayerTwo();
        if (playerOne != null) {
            font.draw(batch, "Giocatore 1: " + playerOne.getName(), 50, 200);
        }
        if (playerTwo != null) {
            font.draw(batch, "Giocatore 2: " + playerTwo.getName(), 50, 150);
        }

        font.draw(batch, "Press SPACE or TOUCH screen to test Controller", 50, 100);
        font.draw(batch, "Premi ESC per mettere in pausa", 50, 50);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        RunMusicManager.pause();
    }

    @Override
    public void dispose() {
        RunMusicManager.stop();
        batch.dispose();
        font.dispose();
    }
}
