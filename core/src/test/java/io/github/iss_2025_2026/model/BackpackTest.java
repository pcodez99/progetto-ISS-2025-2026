package io.github.iss_2025_2026.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class BackpackTest {

    // Creiamo un oggetto finto solo per poter riempire lo zaino nel test
    class OggettoFinto extends Collectible {
        public OggettoFinto(String nome) {
            super(nome, "Un oggetto finto per i test");
        }

        @Override
        public void use(Character target) {
            // Nei test dello zaino non ci interessa l'uso, quindi lo lasciamo vuoto
        }
    }

    @Test
    void testAggiuntaOggetto() {
        Backpack backpack = new Backpack(2); // Backpack con spazio per 2 oggetti
        Collectible pozione = new OggettoFinto("Pozione");

        boolean aggiunto = backpack.addCollectible(pozione);

        assertTrue(aggiunto, "Dovrebbe essere possibile aggiungere l'oggetto");
        assertEquals(1, backpack.getCollectiblesNumber(), "Lo backpack dovrebbe contenere esattamente 1 oggetto");
    }

    @Test
    void testCapacitaMassimaRispettata() {
        Backpack backpack = new Backpack(1); // Backpack piccolissimo, tiene 1 solo oggetto
        backpack.addCollectible(new OggettoFinto("Spada")); // Riempiamo il primo e unico posto

        // Proviamo a forzare l'inserimento di un secondo oggetto
        boolean aggiuntoExtra = backpack.addCollectible(new OggettoFinto("Scudo"));
        assertFalse(aggiuntoExtra, "Il sistema deve bloccare l'aggiunta se lo backpack è pieno");
        assertEquals(1, backpack.getCollectiblesNumber(), "Il numero di oggetti deve restare bloccato a 1");
    }

    @Test
    void testSicurezzaListaImmutabile() {
        Backpack backpack = new Backpack(5);
        backpack.addCollectible(new OggettoFinto("Pozione"));

        // Estraiamo la lista per "leggerla"
        List<Collectible> listaEstratta = backpack.getCollectibles();

        // VERIFICA DI SICUREZZA: Proviamo a craccare il gioco aggiungendo un oggetto
        // direttamente alla lista estratta, aggirando i controlli dello Backpack.
        // Il test passa SOLO se Java blocca questa operazione lanciando un'eccezione.
        assertThrows(UnsupportedOperationException.class, () -> {
            listaEstratta.add(new OggettoFinto("Oggetto Illegale"));
        }, "Il sistema deve lanciare un'eccezione se qualcuno prova a manomettere la lista dall'esterno");
    }
}
