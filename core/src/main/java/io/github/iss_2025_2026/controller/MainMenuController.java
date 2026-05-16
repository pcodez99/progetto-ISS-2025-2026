package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.view.SettingsScreen;
import io.github.iss_2025_2026.view.NewGameConfigScreen;
import io.github.iss_2025_2026.model.MainMenuModel;

/**
 * Controller per il Main Menu.
 * Gestisce l'input dell'utente e coordina le azioni tra Model e View.
 */
public class MainMenuController {

    private final Main game;
    private final GameModel model;
    private final GameController controller;

    public MainMenuController(Main game, GameModel model, GameController controller) {
        this.game = game;
        this.model = model;
        this.controller = controller;
    }

    /**
     * Esegue l'azione corrispondente alla selezione del menu.
     * 
     * @param action L'azione scelta dall'utente.
     */
    public void handleMenuAction(MainMenuModel.MenuAction action) {
        switch (action) {
            case NEW_GAME:
                System.out.println("Avvio nuova configurazione partita...");
                game.setScreen(new NewGameConfigScreen(game, model, controller));
                break;
            case LOAD_GAME:
                System.out.println("Caricamento partita...");
                break;
            case SETTINGS:
                System.out.println("Apertura impostazioni...");
                game.setScreen(new SettingsScreen(game, model, controller));
                break;
            case EXIT:
                System.out.println("Chiusura gioco...");
                Gdx.app.exit();
                break;
        }
    }
}
