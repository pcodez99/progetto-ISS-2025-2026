package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;

/**
 * Game View (LibGDX Screen).
 * Renders the state of the Model.
 */
public class TestScreen implements Screen {
    private final GameModel model;
    private final GameController controller;
    private final SpriteBatch batch;
    private final BitmapFont font;

    public TestScreen(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);
    }

    @Override
    public void show() {
        // Prepare resources if needed
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
        font.draw(batch, "Press SPACE or TOUCH screen to test Controller", 50, 100);
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
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
