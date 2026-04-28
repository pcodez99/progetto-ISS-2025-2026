package io.github.iss_2025_2026;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    Texture startBackground;
    Texture playBackground;
    Texture bucketTexture;
    Texture dropTexture;
    Texture buttonTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    BitmapFont font;
    GlyphLayout glyphLayout;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    Rectangle startButtonRectangle;
    Matrix4 uiProjection;
    Vector3 uiButtonBottomLeft;
    Vector3 uiButtonTopRight;
    boolean gameStarted;

    @Override
    public void create() {
        startBackground = new Texture("background_init.png");
        playBackground = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        startButtonRectangle = new Rectangle();
        font = new BitmapFont();
        font.getData().setScale(1f);
        glyphLayout = new GlyphLayout();
        uiProjection = new Matrix4();
        uiButtonBottomLeft = new Vector3();
        uiButtonTopRight = new Vector3();
        gameStarted = false;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        buttonTexture = new Texture(pixmap);
        pixmap.dispose();

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        updateStartButtonBounds();
        float worldWidth = viewport.getWorldWidth();
        bucketSprite.setPosition((worldWidth - bucketSprite.getWidth()) / 2f, 0.2f);
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateStartButtonBounds();
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        if (!gameStarted) {
            if (Gdx.input.justTouched()) {
                touchPos.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);
                if (startButtonRectangle.contains(touchPos)) {
                    gameStarted = true;
                    dropSprites.clear();
                    dropTimer = 0f;
                }
            }
            return;
        }

        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }
    }

    private void logic() {
        if (!gameStarted) return;

        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        if (!gameStarted) {
            spriteBatch.draw(startBackground, 0, 0, worldWidth, worldHeight);
            spriteBatch.setColor(0f, 0f, 0f, 0.55f);
            spriteBatch.draw(buttonTexture, startButtonRectangle.x, startButtonRectangle.y,
                    startButtonRectangle.width, startButtonRectangle.height);
            spriteBatch.setColor(Color.WHITE);
            spriteBatch.end();

            uiProjection.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            spriteBatch.setProjectionMatrix(uiProjection);
            spriteBatch.begin();

            uiButtonBottomLeft.set(startButtonRectangle.x, startButtonRectangle.y, 0f);
            uiButtonTopRight.set(
                    startButtonRectangle.x + startButtonRectangle.width,
                    startButtonRectangle.y + startButtonRectangle.height,
                    0f
            );
            viewport.project(uiButtonBottomLeft);
            viewport.project(uiButtonTopRight);

            glyphLayout.setText(font, "START");
            float textX = uiButtonBottomLeft.x + (uiButtonTopRight.x - uiButtonBottomLeft.x - glyphLayout.width) / 2f;
            float textY = uiButtonBottomLeft.y + (uiButtonTopRight.y - uiButtonBottomLeft.y + glyphLayout.height) / 2f;
            font.setColor(0f, 0f, 0f, 0.75f);
            font.draw(spriteBatch, glyphLayout, textX + 2f, textY - 2f);
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, glyphLayout, textX, textY);
            spriteBatch.end();
            return;
        }

        spriteBatch.draw(playBackground, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    private void updateStartButtonBounds() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float buttonWidth = 2.2f;
        float buttonHeight = 0.55f;
        startButtonRectangle.set(
                (worldWidth - buttonWidth) / 2f,
                (worldHeight - buttonHeight) / 2f,
                buttonWidth,
                buttonHeight
        );
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void dispose() {
        startBackground.dispose();
        playBackground.dispose();
        bucketTexture.dispose();
        dropTexture.dispose();
        buttonTexture.dispose();
        dropSound.dispose();
        music.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}