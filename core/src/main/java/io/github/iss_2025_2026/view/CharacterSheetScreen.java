package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.CharacterSheetModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Schermata personaggio — aperta tramite il tasto "I" da LevelScreen.
 * Mostra inventario, statistiche (HP, attacco, livello, karma) e vita corrente.
 * Non può essere aperta durante il combattimento.
 *
 * Pattern: View passiva. Legge i dati da {@link CharacterSheetModel} senza
 * modificarli. Per chiudere torna alla schermata precedente.
 */
public class CharacterSheetScreen implements Screen {

    private final Main game;
    private final CharacterSheetModel model;
    private final Screen previousScreen;

    private Stage stage;
    private Skin skin;

    // Texture cache per i collectibles (dispose on screen close)
    private final List<Texture> managedTextures = new ArrayList<>();

    public CharacterSheetScreen(Main game, CharacterSheetModel model, Screen previousScreen) {
        this.game = game;
        this.model = model;
        this.previousScreen = previousScreen;

        this.skin = GameUiTheme.loadSkin();
        buildUI();
    }

    // -------------------------------------------------------------------------
    // UI Build
    // -------------------------------------------------------------------------

    private void buildUI() {
        stage = new Stage(new ScreenViewport());

        // --- Sfondo semi-trasparente scuro ---
        Table bg = new Table();
        bg.setFillParent(true);
        bg.setBackground(skin.newDrawable("white", GameUiTheme.SCREEN_WASH));
        stage.addActor(bg);

        // --- Pannello centrale ---
        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", GameUiTheme.PANEL_STRONG));
        panel.pad(GameUiTheme.SPACE_5);

        // Titolo
        Label titleLabel = new Label("⚔  " + model.getPlayerName(), skin, GameUiTheme.LABEL_TITLE);
        titleLabel.setAlignment(Align.center);
        panel.add(titleLabel).colspan(2).center().padBottom(GameUiTheme.SPACE_4).row();

        // Divisore
        addDivider(panel, 2);

        // --- Colonna sinistra: Statistiche ---
        Table statsTable = buildStatsTable();
        panel.add(statsTable).top().padRight(GameUiTheme.SPACE_5).width(320f);

        // --- Colonna destra: Inventario ---
        Table inventoryTable = buildInventoryTable();
        panel.add(inventoryTable).top().width(320f);

        panel.row();

        // --- Pulsante chiudi ---
        addDivider(panel, 2);
        TextButton closeBtn = new TextButton("Chiudi  [I / ESC]", skin, GameUiTheme.BUTTON_GHOST);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                close();
            }
        });
        panel.add(closeBtn).colspan(2).center().padTop(GameUiTheme.SPACE_4).width(280f).height(52f);

        // Centra il pannello nello stage
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.add(panel).width(720f).pad(GameUiTheme.SPACE_5);
        stage.addActor(root);
    }

    private Table buildStatsTable() {
        Table t = new Table();
        t.defaults().left().padBottom(GameUiTheme.SPACE_3);

        addSectionTitle(t, "Statistiche", 1);

        // HP
        addStatRow(t, "❤  Vita",
                model.getHp() + " / " + model.getMaxHp(),
                GameUiTheme.DANGER);

        // Barra HP
        ProgressBar hpBar = buildProgressBar(model.getHpPercent(), GameUiTheme.DANGER);
        t.add(hpBar).colspan(1).fillX().padBottom(GameUiTheme.SPACE_3).row();

        // Attacco
        addStatRow(t, "⚔  Attacco", String.valueOf(model.getBaseDamage()), GameUiTheme.ACCENT_AMBER);

        // Livello
        addStatRow(t, "⭐  Livello", String.valueOf(model.getLevel()), GameUiTheme.ACCENT_LIME);

        // XP
        String xpText = model.getXp() + " / " + model.getXpToNext() + " XP";
        addStatRow(t, "✨  Esperienza", xpText, GameUiTheme.ACCENT_CYAN);

        // Barra XP
        float xpPercent = model.getXpToNext() > 0
                ? Math.min(1f, (float) model.getXp() / model.getXpToNext())
                : 0f;
        ProgressBar xpBar = buildProgressBar(xpPercent, GameUiTheme.ACCENT_CYAN);
        t.add(xpBar).colspan(1).fillX().padBottom(GameUiTheme.SPACE_3).row();

        // Karma
        String karmaText = model.getKarma() > 0 ? "+" + model.getKarma() : String.valueOf(model.getKarma());
        Color karmaColor = model.getKarma() >= 0 ? GameUiTheme.SUCCESS : GameUiTheme.DANGER;
        addStatRow(t, "☯  Karma", karmaText, karmaColor);

        return t;
    }

    private Table buildInventoryTable() {
        Table t = new Table();
        t.defaults().left().padBottom(GameUiTheme.SPACE_2);

        String inventoryTitle = "Inventario  ("
                + model.getInventorySize() + "/" + model.getInventoryCapacity() + ")";
        addSectionTitle(t, inventoryTitle, 1);

        if (model.isInventoryEmpty()) {
            Label emptyLabel = new Label("Nessun oggetto nel zaino.", skin, GameUiTheme.LABEL_MUTED);
            emptyLabel.setAlignment(Align.center);
            t.add(emptyLabel).center().padTop(GameUiTheme.SPACE_3).row();
        } else {
            // Griglia di oggetti con scroll
            Table itemsGrid = new Table();
            itemsGrid.defaults().pad(GameUiTheme.SPACE_2);

            List<Collectible> items = model.getInventory();
            for (int i = 0; i < items.size(); i++) {
                Collectible c = items.get(i);
                Table itemCard = buildItemCard(c);
                itemsGrid.add(itemCard).width(280f).height(60f).pad(GameUiTheme.SPACE_1);
                itemsGrid.row();
            }
            itemsGrid.row();

            ScrollPane scrollPane = new ScrollPane(itemsGrid, skin);
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(true, false);
            t.add(scrollPane).fillX().maxHeight(280f).row();
        }

        return t;
    }

    private Table buildItemCard(Collectible collectible) {
        Table card = new Table();
        card.setBackground(skin.newDrawable("white", GameUiTheme.CHIP));
        card.pad(GameUiTheme.SPACE_2);

        // Icona oggetto (se asset disponibile)
        String assetPath = getAssetPath(collectible.getId());
        if (assetPath != null && Gdx.files.internal(assetPath).exists()) {
            try {
                Texture texture = new Texture(Gdx.files.internal(assetPath));
                managedTextures.add(texture);
                TextureRegion region;
                if (assetPath.contains("spritesheet")) {
                    int frameSize = texture.getHeight();
                    TextureRegion[][] tmp = TextureRegion.split(texture, frameSize, frameSize);
                    region = tmp[0][0];
                } else {
                    region = new TextureRegion(texture);
                }
                Image icon = new Image(region);
                card.add(icon).size(36f, 36f).center().padRight(GameUiTheme.SPACE_2);
            } catch (Exception e) {
                Gdx.app.error("CharacterSheetScreen", "Errore caricamento texture: " + assetPath, e);
            }
        }

        Label nameLabel = new Label(collectible.getName(), skin, GameUiTheme.LABEL_MUTED);
        nameLabel.setAlignment(Align.left);
        card.add(nameLabel).fillX().expandX();

        return card;
    }

    // -------------------------------------------------------------------------
    // Helpers UI
    // -------------------------------------------------------------------------

    private void addSectionTitle(Table t, String text, int colspan) {
        Label lbl = new Label(text, skin, GameUiTheme.LABEL_SECTION);
        t.add(lbl).colspan(colspan).left().padBottom(GameUiTheme.SPACE_2).row();
        addDivider(t, colspan);
    }

    private void addStatRow(Table t, String labelText, String valueText, Color valueColor) {
        Label lbl = new Label(labelText, skin, GameUiTheme.LABEL_BODY);
        Label val = new Label(valueText, skin, GameUiTheme.LABEL_BODY);
        val.setColor(valueColor);
        t.add(lbl).left();
        t.add(val).right().padLeft(GameUiTheme.SPACE_3).row();
    }

    private void addDivider(Table t, int colspan) {
        Table line = new Table();
        line.setBackground(skin.newDrawable("white", new Color(0.25f, 0.30f, 0.33f, 0.6f)));
        t.add(line).colspan(colspan).fillX().height(1f).padBottom(GameUiTheme.SPACE_3).row();
    }

    private ProgressBar buildProgressBar(float value, Color fillColor) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = skin.newDrawable("white", new Color(0.15f, 0.20f, 0.22f, 1f));
        style.knobBefore = skin.newDrawable("white", fillColor);
        style.knob = skin.newDrawable("white", new Color(0, 0, 0, 0)); // trasparente
        ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, style);
        bar.setValue(value);
        return bar;
    }

    /** Mappa ID collectible → path asset (stessa logica di LevelScreen) */
    private String getAssetPath(String id) {
        if ("level_1_potion".equals(id)) return "collectibles/Cavuliceddu-nobg.png";
        if ("level_3_potion".equals(id)) return "collectibles/Fico-nobg.png";
        if ("level_2_potion".equals(id)) return "collectibles/Larva-nobg.png";
        if ("level_3_buff".equals(id))   return "collectibles/Miele-spritesheet.png";
        if ("level_1_bomb".equals(id))   return "collectibles/Molotov-spritesheet.png";
        if ("level_3_bomb".equals(id))   return "collectibles/Patata bomba-spritesheet.png";
        if ("level_2_buff".equals(id))   return "collectibles/Siero sinaptico-spritesheet.png";
        if ("level_1_buff".equals(id))   return "collectibles/Siero-nobg.png";
        if ("level_2_bomb".equals(id))   return "collectibles/lampadina-spritesheet.png";
        return null;
    }

    // -------------------------------------------------------------------------
    // Screen lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.I || keycode == Input.Keys.O || keycode == Input.Keys.ESCAPE) {
                    close();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        for (Texture t : managedTextures) {
            if (t != null) t.dispose();
        }
        managedTextures.clear();
    }

    // -------------------------------------------------------------------------
    // Navigazione
    // -------------------------------------------------------------------------

    private void close() {
        game.setScreen(previousScreen);
    }
}
