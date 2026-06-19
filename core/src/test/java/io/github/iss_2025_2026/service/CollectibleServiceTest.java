package io.github.iss_2025_2026.service;

import com.badlogic.gdx.math.Rectangle;
import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.collectibles.CollectibleUseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CollectibleServiceTest {

    private CollectibleFactory factory;
    private CollectibleService service;

    @BeforeEach
    void setUp() {
        Collectible l1Potion = new Collectible("level_1_potion", "Cavuliceddi", "Verdura", "HEAL", false, 20);
        Collectible l2Potion = new Collectible("level_2_potion", "Larva", "Larva", "HEAL", false, 20);
        Collectible l1Bomb = new Collectible("level_1_bomb", "Molotov", "Bomba", "DAMAGE", true, 15);

        factory = new CollectibleFactory(Arrays.asList(l1Potion, l2Potion, l1Bomb));
        // Crea il servizio con spawn interval = 1.0 secondi e cap = 3
        service = new CollectibleService(factory, 1.0f, 3);
    }

    @Test
    void testLevelConstraintsOnUse() {
        Player player = new Player("Player 1", 100, 10, null);
        Collectible l1Potion = factory.getCollectible("level_1_potion");
        Collectible l2Potion = factory.getCollectible("level_2_potion");

        // Utilizzo nel livello 1
        CollectibleUseContext contextLevel1 = new CollectibleUseContext(player, Collections.singletonList(player), 1);
        assertDoesNotThrow(() -> l1Potion.use(contextLevel1));
        assertThrows(IllegalStateException.class, () -> l2Potion.use(contextLevel1));

        // Utilizzo nel livello 2
        CollectibleUseContext contextLevel2 = new CollectibleUseContext(player, Collections.singletonList(player), 2);
        assertDoesNotThrow(() -> l2Potion.use(contextLevel2));
        assertThrows(IllegalStateException.class, () -> l1Potion.use(contextLevel2));
    }

    @Test
    void testSpawningOnlyForCurrentLevel() {
        Rectangle pov = new Rectangle(100, 100, 400, 300);
        Player player = new Player("Player 1", 100, 10, null);
        player.setX(150);
        player.setY(150);

        // Nel livello 1, spawniamo un oggetto (attiva lo spawn dopo 1.1s > spawnInterval)
        service.update(1.1f, Arrays.asList(player), pov, 1);

        List<CollectibleService.CollectibleOnMap> active = service.getActiveCollectibles();
        assertEquals(1, active.size());
        assertTrue(active.get(0).getCollectible().getId().startsWith("level_1_"));
        
        // Verifica che sia spawnato entro il POV
        float x = active.get(0).getX();
        float y = active.get(0).getY();
        assertTrue(pov.contains(x, y));
    }

    @Test
    void testCollectiblePickup() {
        Rectangle pov = new Rectangle(100, 100, 400, 300);
        Player player = new Player("Player 1", 100, 10, null);
        // Imposta zaino vuoto
        player.getBackpack().setCapacity(5);
        player.getBackpack().setItems(new java.util.ArrayList<>());

        // Forza spawn di un collectible
        service.update(1.1f, Arrays.asList(player), pov, 1);
        assertEquals(1, service.getActiveCollectibles().size());

        CollectibleService.CollectibleOnMap spawned = service.getActiveCollectibles().get(0);
        
        // Sposta il giocatore lontano: non deve trovare alcun collectible vicino
        player.setX(spawned.getX() + 200f);
        player.setY(spawned.getY() + 200f);
        assertNull(service.getClosestCollectible(player, 60f));

        // Sposta il giocatore vicino: deve rilevarlo
        player.setX(spawned.getX() + 10f);
        player.setY(spawned.getY() + 10f);
        CollectibleService.CollectibleOnMap closest = service.getClosestCollectible(player, 60f);
        assertNotNull(closest);
        assertEquals(spawned, closest);

        // Registra listener per la raccolta
        final boolean[] notified = {false};
        service.setPickupListener((collectible, p) -> {
            notified[0] = true;
            assertEquals(player, p);
        });

        // Esegue la raccolta esplicita
        assertTrue(service.pickUp(player, closest));
        assertEquals(0, service.getActiveCollectibles().size());
        assertEquals(1, player.getBackpack().getSize());
        assertTrue(notified[0]);
    }
}
