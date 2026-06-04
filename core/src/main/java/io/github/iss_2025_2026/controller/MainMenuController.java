package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.view.SettingsScreen;
import io.github.iss_2025_2026.view.LoadGameScreen;
import io.github.iss_2025_2026.view.MainMenuScreen;
import io.github.iss_2025_2026.view.NewGameConfigScreen;
import io.github.iss_2025_2026.model.MainMenuModel;
import io.github.iss_2025_2026.service.GameSaveService;
import java.io.IOException;

/**
 * Controller per il Main Menu.
 * Gestisce l'input dell'utente e coordina le azioni tra Model e View.
 */
public class MainMenuController {

    private final Main game;
    private final GameModel model;
    private final GameController controller;
    private final boolean isRunning;
    private final Screen resumeScreen;

    public MainMenuController(Main game, GameModel model, GameController controller) {
        this(game, model, controller, false, null);
    }

    public MainMenuController(Main game, GameModel model, GameController controller, boolean isRunning,
            Screen resumeScreen) {
        this.game = game;
        this.model = model;
        this.controller = controller;
        this.isRunning = isRunning;
        this.resumeScreen = resumeScreen;
    }

    /**
     * Esegue l'azione corrispondente alla selezione del menu.
     * 
     * @param action L'azione scelta dall'utente.
     */
    public void handleMenuAction(MainMenuModel.MenuAction action) {
        switch (action) {
            case CONTINUE_GAME:
                if (isRunning && resumeScreen != null) {
                    game.setScreen(resumeScreen);
                }
                break;
            case SAVE_GAME:
                saveRunningGame();
                break;
            case NEW_GAME:
                System.out.println("Avvio nuova configurazione partita...");
                game.setScreen(new NewGameConfigScreen(game, model, controller));
                break;
            case LOAD_GAME:
                System.out.println("Caricamento partita...");
                game.setScreen(new LoadGameScreen(game, model, controller));
                break;
            case SETTINGS:
                System.out.println("Apertura impostazioni...");
                game.setScreen(new SettingsScreen(game, model, controller, isRunning, resumeScreen));
                break;
            case RETURN_TO_MAIN_MENU:
                if (resumeScreen != null) {
                    resumeScreen.dispose();
                }
                game.setScreen(new MainMenuScreen(game, model, controller));
                break;
            case EXIT:
                System.out.println("Chiusura gioco...");
                Gdx.app.exit();
                break;
        }
    }

    private void saveRunningGame() {
        try {
            GameSaveService.saveManual(model);
        } catch (IOException exception) {
            model.setMessage("Salvataggio fallito: " + exception.getMessage());
            Gdx.app.error("MainMenuController", "Impossibile salvare la partita.", exception);
        }
    }
}
