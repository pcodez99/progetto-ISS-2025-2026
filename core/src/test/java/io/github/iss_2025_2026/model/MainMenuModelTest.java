package io.github.iss_2025_2026.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainMenuModelTest {
    @Test
    public void testInitialStatus(){
        MainMenuModel model = new MainMenuModel();
        assertNotNull(model,"Il modello dovrebbe essere istanziato");
    }
    //Test per verificare se il modello espone correttamente le 4 azioni fondamentali richieste
    @Test
    public void testMenuActionsPresence(){
        MainMenuModel model= new MainMenuModel();
        //Otteniamo la lista delle azioni disponibili
        MainMenuModel.MenuAction[] actions = model.getAvailableActions();
        // verifichiamo che ci siano le 4 azioni della US
        assertEquals(4,actions.length,"Il menu deve avere esattamente 4 opzioni principali");
    }
}
