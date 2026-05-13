package io.github.iss_2025_2026.model;


/** Model for the main menu **/
public class MainMenuModel {
    // Define the possible actions for the main menu
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
