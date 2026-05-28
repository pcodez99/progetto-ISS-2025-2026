package io.github.iss_2025_2026.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.PlayerSaveState;
import io.github.iss_2025_2026.persistence.SaveManager;
import io.github.iss_2025_2026.service.GameSaveService;
import io.github.iss_2025_2026.service.MenuMusicManager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoadGameScreen implements Screen {
    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;

    private Stage stage;
    private Skin skin;
    private Texture background;

    private List<String> saveFiles;
    private Map<String, GameState> saveStates;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> saveListWidget;
    private Label selectedSaveLabel;
    private Label saveDetailsLabel;
    private Label statusLabel;

    public LoadGameScreen(Main game, GameModel gameModel, GameController gameController) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.skin = GameUiTheme.loadSkin();
        this.background = new Texture(Gdx.files.internal("background_init.png"));
        this.saveFiles = SaveManager.listSaveFiles();
        this.saveStates = loadSaveStates(this.saveFiles);

        buildUI();
    }

    private Map<String, GameState> loadSaveStates(List<String> files) {
        Map<String, GameState> states = new LinkedHashMap<>();
        for (String fileName : files) {
            try {
                states.put(fileName, SaveManager.loadGame(fileName));
            } catch (IOException e) {
                Gdx.app.error("LoadGameScreen", "Impossibile leggere il salvataggio " + fileName, e);
            }
        }
        return states;
    }

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = GameUiFactory.createScreenRoot(stage, background, skin);
        Table shell = GameUiFactory.createPanel(skin, GameUiTheme.SPACE_5);
        shell.defaults().left().growX();

        shell.add(GameUiFactory.createHeroBlock(
                skin,
                "ARCHIVIO SALVATAGGI",
                "Carica la tua partita",
                "Scegli un salvataggio gia presente nella cartella saves e riparti subito dalla run registrata."))
                .width(640f).padBottom(GameUiTheme.SPACE_5).row();

        if (saveFiles.isEmpty()) {
            Table emptyPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
            emptyPanel.defaults().left().growX();
            emptyPanel.add(new Label("Nessun salvataggio trovato", skin, GameUiTheme.LABEL_SECTION))
                    .padBottom(GameUiTheme.SPACE_2).row();
            emptyPanel.add(GameUiFactory.createMutedLabel(
                    "Avvia una nuova partita: verra salvata automaticamente in /saves e comparira qui.", skin))
                    .width(560f).row();
            shell.add(emptyPanel).width(640f).padBottom(GameUiTheme.SPACE_4).row();
            shell.add(createBackButtonRow()).left().row();
            root.add(new Container<>(shell)).center();
            return;
        }

        Table contentPanel = GameUiFactory.createStrongPanel(skin, GameUiTheme.SPACE_4);
        contentPanel.defaults().top().left();

        contentPanel.add(buildSaveListPanel()).width(280f).padRight(GameUiTheme.SPACE_4);
        contentPanel.add(buildSaveDetailsPanel()).width(320f);
        shell.add(contentPanel).padBottom(GameUiTheme.SPACE_4).row();

        statusLabel = new Label("", skin, GameUiTheme.LABEL_MUTED);
        shell.add(statusLabel).padBottom(GameUiTheme.SPACE_2).row();
        shell.add(buildActionButtons()).left().row();

        Container<Table> shellWrap = new Container<>(shell);
        shellWrap.width(760f);
        root.add(shellWrap).center();

        updateSelectedSaveDetails();
    }

    private Table buildSaveListPanel() {
        Table panel = new Table();
        panel.defaults().left().growX();
        panel.add(new Label("Elenco salvataggi", skin, GameUiTheme.LABEL_SECTION))
                .padBottom(GameUiTheme.SPACE_2).row();
        panel.add(GameUiFactory.createMutedLabel("I file piu recenti compaiono in alto.", skin))
                .width(260f).padBottom(GameUiTheme.SPACE_3).row();

        Array<String> itemNames = new Array<>();
        for (String fileName : saveFiles) {
            itemNames.add(SaveManager.stripJsonExtension(fileName));
        }

        saveListWidget = new com.badlogic.gdx.scenes.scene2d.ui.List<>(skin);
        saveListWidget.setItems(itemNames);
        saveListWidget.setSelectedIndex(0);
        saveListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clearStatus();
                updateSelectedSaveDetails();
            }
        });

        ScrollPane listScroll = new ScrollPane(saveListWidget, skin);
        listScroll.setFadeScrollBars(false);
        listScroll.setScrollingDisabled(true, false);
        panel.add(listScroll).width(260f).height(220f);
        return panel;
    }

    private Table buildSaveDetailsPanel() {
        Table panel = new Table();
        panel.defaults().left().growX();
        panel.add(new Label("Dettagli salvataggio", skin, GameUiTheme.LABEL_SECTION))
                .padBottom(GameUiTheme.SPACE_2).row();

        selectedSaveLabel = new Label("", skin, GameUiTheme.LABEL_BODY);
        selectedSaveLabel.setWrap(true);
        panel.add(selectedSaveLabel).width(300f).padBottom(GameUiTheme.SPACE_2).row();

        saveDetailsLabel = new Label("", skin, GameUiTheme.LABEL_MUTED);
        saveDetailsLabel.setWrap(true);
        panel.add(saveDetailsLabel).width(300f).row();
        return panel;
    }

    private Table buildActionButtons() {
        Table buttons = new Table();

        TextButton backBtn = GameUiFactory.createButton("Indietro", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });

        TextButton loadBtn = GameUiFactory.createButton("Carica Salvataggio", skin, GameUiTheme.BUTTON_PRIMARY);
        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadSelectedSave();
            }
        });

        buttons.add(backBtn).width(180f).height(56f).padRight(GameUiTheme.SPACE_2);
        buttons.add(loadBtn).width(240f).height(56f);
        return buttons;
    }

    private Table createBackButtonRow() {
        Table buttons = new Table();
        TextButton backBtn = GameUiFactory.createButton("Torna al menu", skin, GameUiTheme.BUTTON_GHOST);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, gameModel, gameController));
                dispose();
            }
        });
        buttons.add(backBtn).width(200f).height(56f);
        return buttons;
    }

    private void updateSelectedSaveDetails() {
        String selectedFile = getSelectedFileName();
        GameState state = selectedFile == null ? null : saveStates.get(selectedFile);
        if (state == null) {
            selectedSaveLabel.setText("Salvataggio non leggibile");
            saveDetailsLabel.setText("Questo file esiste, ma non e stato possibile interpretarne il contenuto.");
            return;
        }

        selectedSaveLabel.setText(resolveSaveTitle(selectedFile, state));
        saveDetailsLabel.setText(buildSaveDetails(state));
    }

    private void loadSelectedSave() {
        String selectedFile = getSelectedFileName();
        if (selectedFile == null) {
            setError("Nessun salvataggio selezionato.");
            return;
        }

        try {
            GameSaveService.loadGameIntoModel(gameModel, selectedFile);
            game.getGameContext().getFlowController().startCurrentLevel();
            dispose();
        } catch (IOException e) {
            setError("Caricamento fallito: " + e.getMessage());
            Gdx.app.error("LoadGameScreen", "Errore nel caricamento del salvataggio " + selectedFile, e);
        }
    }

    private String getSelectedFileName() {
        if (saveListWidget == null || saveFiles.isEmpty()) {
            return null;
        }
        int index = saveListWidget.getSelectedIndex();
        if (index < 0 || index >= saveFiles.size()) {
            return null;
        }
        return saveFiles.get(index);
    }

    private String resolveSaveTitle(String fileName, GameState state) {
        String gameName = state.getGameName();
        if (gameName != null && !gameName.trim().isEmpty()) {
            return gameName;
        }
        return SaveManager.stripJsonExtension(fileName);
    }

    private String buildSaveDetails(GameState state) {
        if (state.getPlayerOne() != null) {
            StringBuilder details = new StringBuilder();
            String modeLabel = state.getPlayerTwo() != null
                    ? "MULTIPLAYER"
                    : state.getGameMode() != null ? state.getGameMode().name() : "SINGLE_PLAYER";
            details.append("Modalita: ").append(modeLabel);
            details.append("\nGiocatore 1: ").append(describePlayer(state.getPlayerOne()));
            if (state.getPlayerTwo() != null) {
                details.append("\nGiocatore 2: ").append(describePlayer(state.getPlayerTwo()));
            }
            if (state.getSavedAt() != null && !state.getSavedAt().trim().isEmpty()) {
                details.append("\nSalvato il: ").append(state.getSavedAt());
            }
            return details.toString();
        }

        if (state.hasLegacySinglePlayerData()) {
            return "Formato legacy\nGiocatore: " + state.getPlayerName()
                    + "\nPersonaggio: " + state.getCharacterType()
                    + "\nLivello: " + state.getLevel();
        }

        return "Dettagli non disponibili.";
    }

    private String describePlayer(PlayerSaveState player) {
        return player.getName() + " - Lv " + player.getLevel() + ", HP " + player.getHp() + "/" + player.getMaxHp();
    }

    private void clearStatus() {
        if (statusLabel != null) {
            statusLabel.setText("");
            statusLabel.setColor(GameUiTheme.MUTED);
        }
    }

    private void setError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setColor(GameUiTheme.DANGER);
        }
    }

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
        background.dispose();
    }
}
