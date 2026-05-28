package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.CollectibleFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectibleFactoryTest {

    private CollectibleFactory factory;

    @BeforeEach
    public void setUp() {
        // Questa riga fa partire in automatico la lettura del file collectibles.yaml
        factory = new CollectibleFactory();
    }

    @Test
    public void testCaricamentoPozionePiccola() {
        //Chiede alla factory di darci la pozione usando l'ID esatto scritto nello YAML
        Collectible pozione = factory.getCollectible("SMALL_POTION");

        //Verifica che l'oggetto esista (se fallisce qui, l'ID è sbagliato o il file non è stato letto)
        assertNotNull(pozione, "ERRORE: La pozione è null! Controlla l'ID nel file YAML.");

        //Verifica che Jackson abbia mappato i dati correttamente
        assertEquals("SMALL_POTION", pozione.getId());
        assertEquals("Pozione piccola", pozione.getName());
        assertEquals("HEAL", pozione.getEffectType());
        assertEquals(20, pozione.getEffectValue());

        // Verifica boolean
        assertFalse(pozione.getAoe(), "La pozione non dovrebbe essere ad area!");
    }

    @Test
    public void testCaricamentoBombaPiccola() {
        Collectible bomba = factory.getCollectible("SMALL_BOMB");

        assertNotNull(bomba, "ERRORE: La bomba è null!");
        assertEquals("DAMAGE", bomba.getEffectType());
        assertEquals(15, bomba.getEffectValue());

        // Verifica che il true sia stato letto correttamente(dato che di default il valore è false)
        assertTrue(bomba.getAoe(), "La bomba dovrebbe essere ad area (aoe = true)");
    }

    @Test
    public void testComportamentoConIdInesistente() {
        // Chiede un oggetto inventato che sicuramente non c'è nello YAML
        Collectible oggettoFalso = factory.getCollectible("SPADA_LASER_INVENTATA");

        //DA RIVEDERE: in questo momento factory robusta che restituisce null senza far crashare il gioco
        assertNull(oggettoFalso, "La factory dovrebbe restituire null per oggetti inesistenti.");
    }

    @Test
    public void testFactoryRestituisceCopieIndipendenti() {
        Collectible primaPozione = factory.getCollectible("SMALL_POTION");
        Collectible secondaPozione = factory.getCollectible("SMALL_POTION");

        assertNotSame(primaPozione, secondaPozione);

        primaPozione.setName("Nome modificato");

        assertEquals("Pozione piccola", secondaPozione.getName());
        assertEquals("Pozione piccola", factory.getCollectible("SMALL_POTION").getName());
    }
}
