package io.github.iss_2025_2026.model;


/** Model for the main menu **/
public class MainMenuModel {
    // Define the possible actions for the main menu
    public enum MenuAction{
        CONTINUE_GAME,
        NEW_GAME,
        LOAD_GAME,
        SETTINGS,
        RETURN_TO_MAIN_MENU,
        EXIT
    }

    public MainMenuModel(){
    }

    public MenuAction[] getAvailableActions(){
        return getAvailableActions(false);
    }

    public MenuAction[] getAvailableActions(boolean isRunning) {
        if (isRunning) {
            return new MenuAction[] {
                    MenuAction.CONTINUE_GAME,
                    MenuAction.RETURN_TO_MAIN_MENU
            };
        }

        return new MenuAction[] {
                MenuAction.NEW_GAME,
                MenuAction.LOAD_GAME,
                MenuAction.SETTINGS,
                MenuAction.EXIT
        };
    }

}
