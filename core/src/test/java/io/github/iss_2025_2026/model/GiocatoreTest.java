package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GiocatoreTest {

    // 1. Creiamo un'abilità per poter istanziare il giocatore
    class AbilitaFinta implements AbilitaSpeciale {
        @Override public String getNome() { return "Azione Finta"; }
        @Override public String getDescrizione() { return "Serve solo per i test"; }
        @Override public void esegui(Personaggio u, Personaggio b, int l) { }
    }

    // 2. Creiamo un giocatore per testare i metodi della classe astratta
    class GiocatoreFinto extends Giocatore {
        public GiocatoreFinto() {
            super("Tester", 100, 10, new AbilitaFinta());
        }

        @Override
        public void saliDiLivello() {
            aumentoLivello(); // Metodo minimo per non lasciare vuoto lo scheletro
        }
    }

    @Test
    void testValoriIniziali() {
        Giocatore g = new GiocatoreFinto();

        assertEquals(1, g.getLivello(), "Il giocatore deve nascere al livello 1");
        assertEquals(0, g.getKarma(), "Il karma iniziale deve essere esattamente 0 (Neutrale)");
        assertNotNull(g.getZaino(), "Lo zaino deve essere stato creato (non null)");
        assertNotNull(g.getAbilita(), "L'abilità deve essere assegnata");
    }

    @Test
    void testSicurezzaLimitiKarma() {
        Giocatore g = new GiocatoreFinto();

        // Test: cerchiamo di sfondare il tetto massimo del lato "Altruismo"
        g.modificaKarma(100);
        assertEquals(50, g.getKarma(), "Il karma deve bloccarsi a +50 anche se aggiungo di più");

        // Test: cerchiamo di sfondare il tetto minimo del lato "Egoismo"
        g.modificaKarma(-200);
        assertEquals(-50, g.getKarma(), "Il karma deve bloccarsi a -50 anche se sottraggo troppo");
    }
}
