package io.github.iss_2025_2026.view;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Color;
import io.github.iss_2025_2026.model.Player;

/**
 * HUD con barre separate e colore interpolato. Usa TextureRegionDrawable su una texture 1x1
 * per permettere la colorazione tramite Image.setColor().
 */
public class PlayerHud implements Disposable {
    private final Player player;
    private final Skin skin;
    private final Table table;

    // Shared 1x1 white texture used to build TextureRegionDrawable
    private static Texture sharedPixelTex;
    private static TextureRegionDrawable sharedDrawable;
    private static int instanceCount = 0;

    private final Label levelLabel;
    private final GradientBar hpBar;
    private final GradientBar xpBar;
    private final GradientBar karmaBar;

    public PlayerHud(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;

        ensureSharedTexture();
        instanceCount++;

        table = new Table();
        table.right().top();
        table.pad(GameUiTheme.SPACE_2);

        // Level label will be displayed next to the XP bar
        levelLabel = new Label("Lv " + player.getLevel(), skin, GameUiTheme.LABEL_TAG);

        // Bars with sizes and spacing
        hpBar = new GradientBar(200f, 12f, sharedDrawable);
        xpBar = new GradientBar(200f, 12f, sharedDrawable);
        karmaBar = new GradientBar(200f, 12f, sharedDrawable);

        // Layout: bars stacked vertically, each bar on its own row
        // Row 1: HP label + HP bar
        table.add(new Label("HP", skin, GameUiTheme.LABEL_TAG)).left().padRight(GameUiTheme.SPACE_1);
        table.add(hpBar.getTable()).width(200f).height(12f).padBottom(GameUiTheme.SPACE_2).row();

        // Row 2: Level label + XP bar
        table.add(levelLabel).left().padRight(GameUiTheme.SPACE_1);
        table.add(xpBar.getTable()).width(200f).height(12f).padBottom(GameUiTheme.SPACE_2).row();

        // Row 3: Karma label + Karma bar
        table.add(new Label("Karma", skin, GameUiTheme.LABEL_TAG)).left().padRight(GameUiTheme.SPACE_1);
        table.add(karmaBar.getTable()).width(200f).height(12f).row();

        // Initial update
        update();
    }

    private static void ensureSharedTexture() {
        if (sharedPixelTex == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            sharedPixelTex = new Texture(pix);
            pix.dispose();
            sharedDrawable = new TextureRegionDrawable(new TextureRegion(sharedPixelTex));
        }
    }

    public Table getTable() {
        return table;
    }

    public void attachToStage(Stage uiStage) {
        uiStage.addActor(table);
    }

    /**
     * Aggiorna i valori e i colori delle barre.
     */
    public void update() {
        if (player == null) return;

        levelLabel.setText("Lv " + player.getLevel());

        // HP
        float hpPercent = player.getMaxHp() > 0 ? (float) player.getHp() / (float) player.getMaxHp() : 0f;
        hpPercent = clamp01(hpPercent);
        Color hpColor = computeHpColor(hpPercent);
        hpBar.setPercent(hpPercent, hpColor);

        // XP
        float xpPercent = player.getXpToNext() > 0 ? (float) player.getXp() / (float) player.getXpToNext() : 0f;
        xpPercent = clamp01(xpPercent);
        Color xpColor = computeLevelColor(xpPercent);
        xpBar.setPercent(xpPercent, xpColor);

        // Karma - map -50..50 -> 0..1
        float karmaPercent = (player.getKarma() + 50f) / 100f;
        karmaPercent = clamp01(karmaPercent);
        Color karmaColor = computeKarmaColor(karmaPercent);
        karmaBar.setPercent(karmaPercent, karmaColor);
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    // HP color: red -> yellow -> green
    private Color computeHpColor(float p) {
        if (p <= 0.5f) {
            float t = p / 0.5f; // 0..1
            return lerpColor(Color.RED, Color.YELLOW, t);
        } else {
            float t = (p - 0.5f) / 0.5f;
            return lerpColor(Color.YELLOW, Color.GREEN, t);
        }
    }

    // Karma: shades of cyan (light to darker)
    private Color computeKarmaColor(float p) {
        Color a = new Color(0.6f, 0.9f, 0.98f, 1f);
        Color b = new Color(0.12f, 0.66f, 0.86f, 1f);
        return lerpColor(a, b, p);
    }

    // Level/XP: shades of green
    private Color computeLevelColor(float p) {
        Color a = new Color(0.12f, 0.4f, 0.12f, 1f);
        Color b = new Color(0.56f, 0.9f, 0.25f, 1f);
        return lerpColor(a, b, p);
    }

    private Color lerpColor(Color a, Color b, float t) {
        t = clamp01(t);
        return new Color(
            a.r + (b.r - a.r) * t,
            a.g + (b.g - a.g) * t,
            a.b + (b.b - a.b) * t,
            a.a + (b.a - a.a) * t);
    }

    /**
     * Dispose shared texture. Call from outer-level dispose (LevelScreen.dispose()).
     */
    @Override
    public void dispose() {
        instanceCount = Math.max(0, instanceCount - 1);
        if (instanceCount == 0 && sharedPixelTex != null) {
            sharedPixelTex.dispose();
            sharedPixelTex = null;
            sharedDrawable = null;
        }
    }

    // --- Inner helper class: simple fill bar built with Images ---
    private static class GradientBar {
        private final Table table;
        private final Image background;
        private final Image fill;
        private final float width;
        private final float height;

        GradientBar(float width, float height, TextureRegionDrawable baseDrawable) {
            this.width = width;
            this.height = height;
            table = new Table();
            table.setSize(width, height);

            // background (darker)
            background = new Image(baseDrawable);
            background.setColor(GameUiTheme.PANEL_STRONG);
            background.setSize(width, height);
            background.setPosition(0f, 0f);

            // fill (colored) - start 0 width
            fill = new Image(baseDrawable);
            fill.setColor(Color.GREEN);
            fill.setSize(0f, height - 2f);
            fill.setPosition(1f, 1f);

            table.addActor(background);
            table.addActor(fill);
        }

        public Table getTable() {
            return table;
        }

        public void setPercent(float percent, Color color) {
            percent = Math.max(0f, Math.min(1f, percent));
            // Update fill width and color
            float w = width * percent;
            fill.setColor(color);
            fill.setSize(Math.max(0f, w), height - 2f);
            // position fill slightly inset
            fill.setPosition(1f, 1f);
            background.setPosition(0f, 0f);
        }
    }
}

