package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.model.CharacterSelectionModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;

public class CharacterSelectionController {
    private final CharacterSelectionModel model;
    private final NewGameConfigModel config;

    public CharacterSelectionController(Main game, CharacterSelectionModel model, NewGameConfigModel config) {
        this.model = model;
        this.config = config;
    }

    public void selectNext() {
        model.nextCharacter();
    }

    public void selectPrevious() {
        model.previousCharacter();
    }

    public void confirmSelection() {
        String characterId = (String) model.getSelectedCharacter().get("id");
        System.out.println("Partita avviata: " + config.getGameName() + " in modalità " + config.getGameMode());
        System.out.println("Personaggio scelto: " + characterId);

        // Qui andrà la logica per inizializzare il GameModel reale e cambiare Screen
        // verso il Gioco
        // Per ora stampiamo e potremmo tornare al menu o restare fermi
    }
}
