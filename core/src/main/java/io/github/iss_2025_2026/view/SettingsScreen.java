package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
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
import io.github.iss_2025_2026.service.MenuMusicManager;
import io.github.iss_2025_2026.service.RunMusicManager;
import io.github.iss_2025_2026.service.SettingsManager;

import java.util.Map;

/**
 * Schermata delle impostazioni.
 * <p>
 * Permette al giocatore di:
 * <ul>
 * <li>Modificare il volume master generale</li>
 * <li>Modificare il volume della musica</li>
 * <li>Modificare il volume degli effetti sonori (SFX)</li>
 * <li>Visualizzare la mappatura dei comandi di gioco</li>
 * <li>Salvare le impostazioni nel file {@code settings.json}</li>
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
    private Texture backgroundTexture;

    /** Label di feedback mostrata dopo il salvataggio. */
    private Label statusLabel;

    public SettingsScreen(Main game, GameModel gameModel, GameController gameController) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.settingsModel = new SettingsMenuModel();
        this.settingsManager = new SettingsManager();

        // Tentiamo di caricare le impostazioni esistenti
        GameSettings saved = settingsManager.load(SETTINGS_FILE);
        settingsModel.loadFrom(saved);

        this.skin = GameUiTheme.loadSkin();
        this.backgroundTexture = new Texture(Gdx.files.internal("settings_background.png"));

        buildUI();
    }

    // ---------------------------------------------------------------
    // Costruzione interfaccia
    // ---------------------------------------------------------------

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = GameUiFactory.createScreenRoot(stage, backgroundTexture, skin);

        Table shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_5);
        shell.defaults().left().growX();
        shell.add(GameUiFactory.createHeroBlock(
                skin,
                "SISTEMA GIOCO",
                "Impostazioni",
                "Regola il mix audio e consulta i controlli principali mantenendo un look coerente con il resto del gioco."))
                .padBottom(GameUiTheme.SPACE_4).row();

        Table audioPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        audioPanel.defaults().left();
        audioPanel.add(new Label("Audio", skin, GameUiTheme.LABEL_SECTION)).colspan(2)
                .padBottom(GameUiTheme.SPACE_3).row();

        audioPanel.add(new Label("Volume generale", skin, GameUiTheme.LABEL_BODY)).padRight(GameUiTheme.SPACE_3);
        final Slider masterSlider = new Slider(0f, 1f, 0.01f, false,
                skin.get(GameUiTheme.SLIDER_GAME, Slider.SliderStyle.class));
        masterSlider.setValue(settingsModel.getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsModel.setMasterVolume(masterSlider.getValue());
                applyAudioSettingsRealtime();
                clearStatus();
            }
        });
        audioPanel.add(masterSlider).width(320f).padBottom(GameUiTheme.SPACE_2).row();

        audioPanel.add(new Label("Volume musica", skin, GameUiTheme.LABEL_BODY)).padRight(GameUiTheme.SPACE_3);
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false,
                skin.get(GameUiTheme.SLIDER_GAME, Slider.SliderStyle.class));
        musicSlider.setValue(settingsModel.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsModel.setMusicVolume(musicSlider.getValue());
                applyAudioSettingsRealtime();
                clearStatus();
            }
        });
        audioPanel.add(musicSlider).width(320f).padBottom(GameUiTheme.SPACE_2).row();

        audioPanel.add(new Label("Volume effetti (SFX)", skin, GameUiTheme.LABEL_BODY)).padRight(GameUiTheme.SPACE_3);
        final Slider sfxSlider = new Slider(0f, 1f, 0.01f, false,
                skin.get(GameUiTheme.SLIDER_GAME, Slider.SliderStyle.class));
        sfxSlider.setValue(settingsModel.getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsModel.setSfxVolume(sfxSlider.getValue());
                applyAudioSettingsRealtime();
                clearStatus();
            }
        });
        audioPanel.add(sfxSlider).width(320f).padBottom(GameUiTheme.SPACE_3).row();

        Table audioChips = new Table();
        audioChips.left();
        audioChips.add(GameUiFactory.createChip(skin, "Audio dinamico")).padRight(GameUiTheme.SPACE_2);
        audioChips.add(GameUiFactory.createChip(skin, "Salvataggio locale"));
        audioPanel.add(audioChips).colspan(2).left().row();

        shell.add(audioPanel).width(700f).padBottom(GameUiTheme.SPACE_4).row();

        Table controlsPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        controlsPanel.defaults().left();
        controlsPanel.add(new Label("Comandi di gioco", skin, GameUiTheme.LABEL_SECTION)).padBottom(GameUiTheme.SPACE_3)
                .row();

        Table controlsGrid = new Table();
        controlsGrid.defaults().left().pad(GameUiTheme.SPACE_1);
        Map<String, String> bindings = settingsModel.getKeyBindings();
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            Table bindingCard = GameUiFactory.createStatChip(skin, entry.getKey(), entry.getValue());
            controlsGrid.add(bindingCard).width(150f);
        }
        controlsPanel.add(controlsGrid).row();

        shell.add(controlsPanel).width(700f).padBottom(GameUiTheme.SPACE_3).row();

        statusLabel = new Label("", skin, GameUiTheme.LABEL_MUTED);
        shell.add(statusLabel).padBottom(GameUiTheme.SPACE_2).row();

        Table buttons = new Table();

        TextButton saveBtn = GameUiFactory.createButton("Salva Impostazioni", skin, GameUiTheme.BUTTON_PRIMARY);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveSettings();
            }
        });

        TextButton backBtn = GameUiFactory.createButton("Indietro", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });

        buttons.add(saveBtn).width(240f).height(56f).padRight(GameUiTheme.SPACE_2);
        buttons.add(backBtn).width(180f).height(56f);
        shell.add(buttons).left();

        Container<Table> shellWrap = new Container<>(shell);
        shellWrap.width(820f);
        root.add(shellWrap).center();
    }

    // ---------------------------------------------------------------
    // Logica di salvataggio
    // ---------------------------------------------------------------

    private void saveSettings() {
        try {
            GameSettings gs = settingsModel.toGameSettings();
            settingsManager.save(gs, SETTINGS_FILE);
            applyAudioSettings(gs);
            statusLabel.setText("Impostazioni salvate!");
            statusLabel.setColor(GameUiTheme.SUCCESS);
        } catch (Exception e) {
            statusLabel.setText("Errore nel salvataggio.");
            statusLabel.setColor(GameUiTheme.DANGER);
            Gdx.app.error("SettingsScreen", "Salvataggio fallito", e);
        }
    }

    private void clearStatus() {
        if (statusLabel != null)
            statusLabel.setText("");
        if (statusLabel != null)
            statusLabel.setColor(GameUiTheme.MUTED);
    }

    private void applyAudioSettingsRealtime() {
        applyAudioSettings(settingsModel.toGameSettings());
    }

    private void applyAudioSettings(GameSettings settings) {
        MenuMusicManager.applySettings(settings);
        RunMusicManager.applySettings(settings);
    }

    // ---------------------------------------------------------------
    // Ciclo di vita LibGDX
    // ---------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        MenuMusicManager.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
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
        backgroundTexture.dispose();
    }
}
