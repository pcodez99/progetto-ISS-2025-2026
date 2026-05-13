package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainMenuModelTest {
    @Test
    public void testInitialStatus() {
        MainMenuModel model = new MainMenuModel();
        assertNotNull(model, "The model should be instantiated");
    }

    // Test to verify if the model correctly exposes the 4 fundamental actions required
    @Test
    public void testMenuActionsPresence() {
        MainMenuModel model = new MainMenuModel();
        // Get the list of available actions
        MainMenuModel.MenuAction[] actions = model.getAvailableActions();
        // Verify that there are the 4 actions
        assertEquals(4, actions.length, "The menu must have exactly 4 main options");
    }
}
