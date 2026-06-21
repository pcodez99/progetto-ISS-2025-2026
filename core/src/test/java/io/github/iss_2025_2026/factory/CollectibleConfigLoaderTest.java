package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.config.CollectibleCatalog;
import io.github.iss_2025_2026.config.CollectibleVisualConfig;
import io.github.iss_2025_2026.config.CollectibleVisualType;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectibleConfigLoaderTest {

    @Test
    void caricaVisualStaticiEAnimatiDalCatalogoReale() {
        CollectibleCatalog catalog = CollectibleConfigLoader.loadDefault();
        Map<String, CollectibleVisualConfig> visuals = catalog.getVisualConfigs();

        assertEquals(9, catalog.getCollectibles().size());
        assertEquals(CollectibleVisualType.STATIC, visuals.get("level_1_potion").getType());

        CollectibleVisualConfig molotov = visuals.get("level_1_bomb");
        assertEquals(CollectibleVisualType.ANIMATED, molotov.getType());
        assertEquals(256, molotov.getFrameWidth());
        assertEquals(256, molotov.getFrameHeight());
        assertEquals(0.08f, molotov.getFrameDuration(), 0.0001f);
    }

    @Test
    void rifiutaUnaConfigurazioneAnimataSenzaDimensioniValide() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CollectibleConfigLoader.load("configs/invalid-collectibles.yaml"));

        assertTrue(exception.getMessage().contains("dimensioni dei frame non valide"));
        assertTrue(exception.getMessage().contains("frameDuration deve essere maggiore di zero"));
    }
}
