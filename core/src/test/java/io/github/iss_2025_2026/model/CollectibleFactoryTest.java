package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.CollectibleFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectibleFactoryTest {

    private CollectibleFactory factory;

    @BeforeEach
    public void setUp() {
        // Questa riga farà partire in automatico la lettura del file collectibles.yaml
        factory = new CollectibleFactory();
    }

    @Test
    public void testCaricamentoPozionePiccola() {
        // 1. Chiediamo alla factory di darci la pozione usando l'ID esatto scritto nello YAML
        Collectible pozione = factory.getCollectible("SMALL_POTION");

        // 2. Verifichiamo che l'oggetto esista (se fallisce qui, l'ID è sbagliato o il file non è stato letto)
        assertNotNull(pozione, "ERRORE: La pozione è null! Controlla l'ID nel file YAML.");

        // 3. Verifichiamo che il robottino Jackson abbia mappato i dati correttamente
        assertEquals("SMALL_POTION", pozione.getId());
        assertEquals("Pozione piccola", pozione.getName());
        assertEquals("HEAL", pozione.getEffectType());
        assertEquals(20, pozione.getEffectValue());

        // Verifichiamo il booleano
        assertFalse(pozione.getAoe(), "La pozione non dovrebbe essere ad area!");
    }

    @Test
    public void testCaricamentoBombaPiccola() {
        Collectible bomba = factory.getCollectible("SMALL_BOMB");

        assertNotNull(bomba, "ERRORE: La bomba è null!");
        assertEquals("DAMAGE", bomba.getEffectType());
        assertEquals(15, bomba.getEffectValue());

        // Qui verifichiamo che il true sia stato letto correttamente
        assertTrue(bomba.getAoe(), "La bomba dovrebbe essere ad area (aoe = true)");
    }

    @Test
    public void testComportamentoConIdInesistente() {
        // Chiediamo un oggetto inventato che sicuramente non c'è nello YAML
        Collectible oggettoFalso = factory.getCollectible("SPADA_LASER_INVENTATA");

        // Ci aspettiamo che la factory sia robusta e restituisca null senza far crashare il gioco
        assertNull(oggettoFalso, "La factory dovrebbe restituire null per oggetti inesistenti.");
    }
}
