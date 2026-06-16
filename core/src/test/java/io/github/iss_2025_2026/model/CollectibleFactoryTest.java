package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.CollectibleFactory;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectibleFactoryTest {

    private CollectibleFactory factory;

    @BeforeEach
    void setUp() {
        Collectible pozione = new Collectible(
                "SMALL_POTION",
                "Pozione piccola",
                "Ripristina una piccola quantita di salute",
                "HEAL",
                false,
                20);
        Collectible bomba = new Collectible(
                "SMALL_BOMB",
                "Bomba piccola",
                "Infligge danni a tutti i bersagli",
                "DAMAGE",
                true,
                15);

        factory = new CollectibleFactory(Arrays.asList(pozione, bomba));
    }

    @Test
    void restituisceLaPozioneDalCatalogoMock() {
        Collectible pozione = factory.getCollectible("SMALL_POTION");

        assertNotNull(pozione);
        assertEquals("SMALL_POTION", pozione.getId());
        assertEquals("Pozione piccola", pozione.getName());
        assertEquals("HEAL", pozione.getEffectType());
        assertEquals(20, pozione.getEffectValue());
        assertFalse(pozione.getAoe());
    }

    @Test
    void restituisceLaBombaDalCatalogoMock() {
        Collectible bomba = factory.getCollectible("SMALL_BOMB");

        assertNotNull(bomba);
        assertEquals("DAMAGE", bomba.getEffectType());
        assertEquals(15, bomba.getEffectValue());
        assertTrue(bomba.getAoe());
    }

    @Test
    void restituisceNullPerIdInesistente() {
        Collectible oggettoFalso = factory.getCollectible("SPADA_LASER_INVENTATA");

        assertNull(oggettoFalso);
    }

    @Test
    void restituisceCopieIndipendentiDalPrototipoMock() {
        Collectible primaPozione = factory.getCollectible("SMALL_POTION");
        Collectible secondaPozione = factory.getCollectible("SMALL_POTION");

        assertNotNull(primaPozione);
        assertNotNull(secondaPozione);
        assertNotSame(primaPozione, secondaPozione);

        primaPozione.setName("Nome modificato");

        assertEquals("Pozione piccola", secondaPozione.getName());
        assertEquals("Pozione piccola", factory.getCollectible("SMALL_POTION").getName());
    }
}
