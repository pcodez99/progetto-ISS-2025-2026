package io.github.iss_2025_2026.model;


/** Model per il menu principale**/
public class MainMenuModel {
    //Definiamo le azioni possibili del menu principale
    public enum MenuAction{
        NEW_GAME,
        LOAD_GAME,
        SETTINGS,
        EXIT
    }

    public MainMenuModel(){
    }

    public MenuAction[] getAvailableActions(){
        return MenuAction.values();
    }

}
