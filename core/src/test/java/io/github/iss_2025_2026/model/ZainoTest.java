package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ZainoTest {

    // Creiamo un oggetto finto solo per poter riempire lo zaino nel test
    class OggettoFinto extends Collezionabile {
        public OggettoFinto(String nome) {
            super(nome, "Un oggetto finto per i test");
        }

        @Override
        public void usa(Personaggio bersaglio) {
            // Nei test dello zaino non ci interessa l'uso, quindi lo lasciamo vuoto
        }
    }

    @Test
    void testAggiuntaOggetto() {
        Zaino zaino = new Zaino(2); // Zaino con spazio per 2 oggetti
        Collezionabile pozione = new OggettoFinto("Pozione");

        boolean aggiunto = zaino.aggiungiCollezionabile(pozione);

        assertTrue(aggiunto, "Dovrebbe essere possibile aggiungere l'oggetto");
        assertEquals(1, zaino.getNumeroCollezionabili(), "Lo zaino dovrebbe contenere esattamente 1 oggetto");
    }

    @Test
    void testCapacitaMassimaRispettata() {
        Zaino zaino = new Zaino(1); // Zaino piccolissimo, tiene 1 solo oggetto
        zaino.aggiungiCollezionabile(new OggettoFinto("Spada")); // Riempiamo il primo e unico posto

        // Proviamo a forzare l'inserimento di un secondo oggetto
        boolean aggiuntoExtra = zaino.aggiungiCollezionabile(new OggettoFinto("Scudo"));
        assertFalse(aggiuntoExtra, "Il sistema deve bloccare l'aggiunta se lo zaino è pieno");
        assertEquals(1, zaino.getNumeroCollezionabili(), "Il numero di oggetti deve restare bloccato a 1");
    }

    @Test
    void testSicurezzaListaImmutabile() {
        Zaino zaino = new Zaino(5);
        zaino.aggiungiCollezionabile(new OggettoFinto("Pozione"));

        // Estraiamo la lista per "leggerla"
        List<Collezionabile> listaEstratta = zaino.getCollezionabili();

        // VERIFICA DI SICUREZZA: Proviamo a craccare il gioco aggiungendo un oggetto
        // direttamente alla lista estratta, aggirando i controlli dello Zaino.
        // Il test passa SOLO se Java blocca questa operazione lanciando un'eccezione.
        assertThrows(UnsupportedOperationException.class, () -> {
            listaEstratta.add(new OggettoFinto("Oggetto Illegale"));
        }, "Il sistema deve lanciare un'eccezione se qualcuno prova a manomettere la lista dall'esterno");
    }
}
