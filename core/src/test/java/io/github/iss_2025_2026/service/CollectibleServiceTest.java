package io.github.iss_2025_2026.service;

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
    private static final float PLAYER_OFFSET_X = 80f;
    private static final float PLAYER_OFFSET_Y = 76.8f;

    private CollectibleFactory factory;
    private CollectibleService service;

    @BeforeEach
    void setUp() {
        Collectible l1Potion = new Collectible("level_1_potion", "Cavuliceddi", "Verdura", "HEAL", false, 20);
        Collectible l2Potion = new Collectible("level_2_potion", "Larva", "Larva", "HEAL", false, 20);
        Collectible l1Bomb = new Collectible("level_1_bomb", "Molotov", "Bomba", "DAMAGE", true, 15);

        factory = new CollectibleFactory(Arrays.asList(l1Potion, l2Potion, l1Bomb));
        service = new CollectibleService(Arrays.asList(
                new CollectibleService.CollectibleOnMap(
                        "cavulicieddi1", factory.requireCollectible("level_1_potion"), 150f, 150f, 60f),
                new CollectibleService.CollectibleOnMap(
                        "molotov1", factory.requireCollectible("level_1_bomb"), 350f, 250f, 60f)),
                PLAYER_OFFSET_X,
                PLAYER_OFFSET_Y);
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
    void testUsesOnlyMapConfiguredCollectibles() {
        List<CollectibleService.CollectibleOnMap> active = service.getActiveCollectibles();
        assertEquals(2, active.size());
        assertEquals("cavulicieddi1", active.get(0).getPlacementId());
        assertEquals("level_1_potion", active.get(0).getCollectible().getId());
        assertEquals(150f, active.get(0).getX());
        assertEquals(150f, active.get(0).getY());
        assertEquals(60f, active.get(0).getInteractionRadius());
    }

    @Test
    void testInteractionRadiusWorksFromEverySide() {
        Player player = new Player("Player 1", 100, 10, null);
        float[][] positionsInsideRadius = {
                {209f, 150f},
                {91f, 150f},
                {150f, 209f},
                {150f, 91f}
        };

        for (float[] position : positionsInsideRadius) {
            player.setX(position[0] - PLAYER_OFFSET_X);
            player.setY(position[1] - PLAYER_OFFSET_Y);
            assertEquals("cavulicieddi1", service.getClosestCollectible(player).getPlacementId());
        }
    }

    @Test
    void testCollectiblePickup() {
        Player player = new Player("Player 1", 100, 10, null);
        // Imposta zaino vuoto
        player.getBackpack().setCapacity(5);
        player.getBackpack().setItems(new java.util.ArrayList<>());

        assertEquals(2, service.getActiveCollectibles().size());

        CollectibleService.CollectibleOnMap spawned = service.getActiveCollectibles().get(0);
        
        // Sposta il giocatore lontano: non deve trovare alcun collectible vicino
        player.setX(spawned.getX() + 200f - PLAYER_OFFSET_X);
        player.setY(spawned.getY() + 200f - PLAYER_OFFSET_Y);
        assertNull(service.getClosestCollectible(player));

        // Sposta il giocatore vicino: deve rilevarlo
        player.setX(spawned.getX() + 10f - PLAYER_OFFSET_X);
        player.setY(spawned.getY() + 10f - PLAYER_OFFSET_Y);
        CollectibleService.CollectibleOnMap closest = service.getClosestCollectible(player);
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
        assertEquals(1, service.getActiveCollectibles().size());
        assertEquals(1, player.getBackpack().getSize());
        assertTrue(notified[0]);
    }
}
