package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    // 1. Creiamo un'abilità per poter istanziare il giocatore
    class AbilitaFinta implements SpecialAbility {
        @Override public String getName() { return "Azione Finta"; }
        @Override public String getDescription() { return "Serve solo per i test"; }
        @Override public void use(Character user, Character target, int userLevel) { }
    }

    // 2. Creiamo un giocatore per testare i metodi della classe astratta
    class PlayerFinto extends Player {
        public PlayerFinto() {
            super("Tester", 100, 10, new AbilitaFinta());
        }

        @Override
        public void levelUp() {
        }
    }

    @Test
    void testValoriIniziali() {
        Player g = new PlayerFinto();

        assertEquals(1, g.getLevel(), "Il giocatore deve nascere al livello 1");
        assertEquals(0, g.getKarma(), "Il karma iniziale deve essere esattamente 0 (Neutrale)");
        assertNotNull(g.getBackpack(), "Lo zaino deve essere stato creato (non null)");
        assertNotNull(g.getAbility(), "L'abilità deve essere assegnata");
    }

    @Test
    void testSicurezzaLimitiKarma() {
        Player g = new PlayerFinto();

        // Test: cerchiamo di sfondare il tetto massimo del lato "Altruismo"
        g.modificaKarma(100);
        assertEquals(50, g.getKarma(), "Il karma deve bloccarsi a +50 anche se aggiungo di più");

        // Test: cerchiamo di sfondare il tetto minimo del lato "Egoismo"
        g.modificaKarma(-200);
        assertEquals(-50, g.getKarma(), "Il karma deve bloccarsi a -50 anche se sottraggo troppo");
    }
}
