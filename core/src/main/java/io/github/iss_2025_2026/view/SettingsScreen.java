package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameSettings;
import io.github.iss_2025_2026.model.SettingsMenuModel;
import io.github.iss_2025_2026.service.SettingsManager;

import java.util.Map;

/**
 * Schermata delle impostazioni.
 * <p>
 * Permette al giocatore di:
 * <ul>
 *   <li>Modificare il volume master (musica + effetti)</li>
 *   <li>Modificare il volume degli effetti sonori (SFX)</li>
 *   <li>Visualizzare la mappatura dei comandi di gioco</li>
 *   <li>Salvare le impostazioni nel file {@code settings.json}</li>
 * </ul>
 */
public class SettingsScreen implements Screen {

    /** Percorso del file di salvataggio impostazioni. */
    private static final String SETTINGS_FILE = "settings.json";

    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;

    private final SettingsMenuModel settingsModel;
    private final SettingsManager settingsManager;

    private Stage stage;
    private Skin skin;
    private SpriteBatch batch;
    private Texture backgroundTexture;

    /** Label di feedback mostrata dopo il salvataggio. */
    private Label statusLabel;

    public SettingsScreen(Main game, GameModel gameModel, GameController gameController) {
        this.game           = game;
        this.gameModel      = gameModel;
        this.gameController = gameController;
        this.settingsModel  = new SettingsMenuModel();
        this.settingsManager = new SettingsManager();

        // Tentiamo di caricare le impostazioni esistenti
        GameSettings saved = settingsManager.load(SETTINGS_FILE);
        settingsModel.loadFrom(saved);

        this.batch = new SpriteBatch();
        this.skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.backgroundTexture = new Texture(Gdx.files.internal("settings_background.png"));

        buildUI();
    }

    // ---------------------------------------------------------------
    // Costruzione interfaccia
    // ---------------------------------------------------------------

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Contenitore principale con scroll
        Table root = new Table();
        root.setFillParent(true);
        root.pad(30);
        stage.addActor(root);

        // ---- Titolo ------------------------------------------------
        Label title = new Label("IMPOSTAZIONI", skin, "default");
        root.add(title).colspan(2).padBottom(20).row();

        // ---- Sezione Volume ----------------------------------------
        root.add(new Label("--- Volume ---", skin)).colspan(2).padBottom(10).row();

        // Definizione stile personalizzato neon per le slider
        Slider.SliderStyle neonStyle = new Slider.SliderStyle();
        neonStyle.background = skin.newDrawable("progress-bar-square", new Color(0.1f, 0.4f, 0.5f, 1f)); // Ciano scuro
        neonStyle.knob = skin.newDrawable("slider-knob", new Color(0f, 0.8f, 1f, 1f));       // Ciano acceso
        neonStyle.knobOver = skin.newDrawable("slider-knob", new Color(0.3f, 0.9f, 1f, 1f));   // Ciano ancora più acceso
        neonStyle.knobDown = skin.newDrawable("slider-knob", new Color(0f, 0.6f, 0.8f, 1f));   // Ciano premuto

        // Slider Master Volume
        root.add(new Label("Volume Generale:", skin)).left().padRight(10);
        final Slider masterSlider = new Slider(0f, 1f, 0.01f, false, neonStyle);
        masterSlider.setValue(settingsModel.getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsModel.setMasterVolume(masterSlider.getValue());
                clearStatus();
            }
        });
        root.add(masterSlider).width(300).padBottom(8).row();

        // Slider SFX Volume
        root.add(new Label("Volume Effetti (SFX):", skin)).left().padRight(10);
        final Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, neonStyle);
        sfxSlider.setValue(settingsModel.getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsModel.setSfxVolume(sfxSlider.getValue());
                clearStatus();
            }
        });
        root.add(sfxSlider).width(300).padBottom(20).row();

        // ---- Sezione Comandi ---------------------------------------
        root.add(new Label("--- Comandi di Gioco ---", skin)).colspan(2).padBottom(10).row();

        Map<String, String> bindings = settingsModel.getKeyBindings();
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            root.add(new Label(entry.getKey() + ":", skin)).left().padRight(20);
            root.add(new Label("[ " + entry.getValue() + " ]", skin)).left().padBottom(6).row();
        }

        root.add(new Label("", skin)).colspan(2).padBottom(20).row(); // spazio

        // ---- Status label -----------------------------------------
        statusLabel = new Label("", skin);
        root.add(statusLabel).colspan(2).padBottom(10).row();

        // ---- Pulsanti ---------------------------------------------
        Table buttons = new Table();

        TextButton saveBtn = new TextButton("Salva", skin);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveSettings();
            }
        });

        TextButton backBtn = new TextButton("Indietro", skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });

        buttons.add(saveBtn).width(150).padRight(20);
        buttons.add(backBtn).width(150);
        root.add(buttons).colspan(2);
    }

    // ---------------------------------------------------------------
    // Logica di salvataggio
    // ---------------------------------------------------------------

    private void saveSettings() {
        try {
            GameSettings gs = settingsModel.toGameSettings();
            settingsManager.save(gs, SETTINGS_FILE);
            statusLabel.setText("Impostazioni salvate!");
        } catch (Exception e) {
            statusLabel.setText("Errore nel salvataggio.");
            Gdx.app.error("SettingsScreen", "Salvataggio fallito", e);
        }
    }

    private void clearStatus() {
        if (statusLabel != null) statusLabel.setText("");
    }

    // ---------------------------------------------------------------
    // Ciclo di vita LibGDX
    // ---------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        batch.dispose();
    }
}
