package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.MainMenuModel;

/**
 * Controller per il Main Menu.
 * Gestisce l'input dell'utente e coordina le azioni tra Model e View.
 */
public class MainMenuController {

    public MainMenuController() {
    }

    /**
     * Esegue l'azione corrispondente alla selezione del menu.
     * 
     * @param action L'azione scelta dall'utente.
     */
    public void handleMenuAction(MainMenuModel.MenuAction action) {
        switch (action) {
            case NEW_GAME:
                System.out.println("Avvio nuova partita...");
                // Qui andrà la logica per cambiare Screen
                break;
            case LOAD_GAME:
                System.out.println("Caricamento partita...");
                break;
            case SETTINGS:
                System.out.println("Apertura impostazioni...");
                break;
            case EXIT:
                System.out.println("Chiusura gioco...");
                Gdx.app.exit();
                break;
        }
    }
}
