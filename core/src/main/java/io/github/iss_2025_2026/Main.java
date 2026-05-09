package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import io.github.iss_2025_2026.view.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        // Avviamo il menu
        setScreen(new MainMenuScreen());
    }

}
