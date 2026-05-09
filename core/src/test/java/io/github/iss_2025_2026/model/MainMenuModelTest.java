package io.github.iss_2025_2026.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainMenuModelTest {
    @Test
    public void testInitialStatus(){
        MainMenuModel model = new MainMenuModel();
        assertNotNull(model,"Il modello dovrebbe essere istanziato");
    }
}
