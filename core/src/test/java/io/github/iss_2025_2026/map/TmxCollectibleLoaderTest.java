package io.github.iss_2025_2026.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.service.CollectibleService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class TmxCollectibleLoaderTest {

    @Test
    void loadsCatalogPrototypeAtTmxWorldPosition() {
        TmxLevel level = levelWithCollectible("cavulicieddi1", "level_1_potion");
        CollectibleFactory factory = new CollectibleFactory(Collections.singletonList(
                new Collectible("level_1_potion", "Cavuliceddi", "Verdura", "HEAL", false, 20)));

        List<CollectibleService.CollectibleOnMap> result = new TmxCollectibleLoader(factory).load(level, 1);

        assertEquals(1, result.size());
        assertEquals("cavulicieddi1", result.get(0).getPlacementId());
        assertEquals("level_1_potion", result.get(0).getCollectible().getId());
        assertEquals(256f, result.get(0).getX());
        assertEquals(64f, result.get(0).getY());
        assertEquals(72f, result.get(0).getInteractionRadius());
    }

    @Test
    void rejectsUnknownCatalogId() {
        TmxLevel level = levelWithCollectible("misterioso", "unknown_item");
        CollectibleFactory factory = new CollectibleFactory(Collections.emptyList());

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new TmxCollectibleLoader(factory).load(level, 1));

        assertTrue(error.getMessage().contains("unknown_item"));
        assertTrue(error.getMessage().contains("misterioso"));
    }

    @Test
    void rejectsCollectibleConfiguredForAnotherLevel() {
        TmxLevel level = levelWithCollectible("larva1", "level_2_potion");
        CollectibleFactory factory = new CollectibleFactory(Collections.singletonList(
                new Collectible("level_2_potion", "Larva", "Cibo", "HEAL", false, 20)));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new TmxCollectibleLoader(factory).load(level, 1));

        assertTrue(error.getMessage().contains("livello 1"));
    }

    private static TmxLevel levelWithCollectible(String name, String collectibleId) {
        TiledMap map = new TiledMap();
        map.getProperties().put("width", 10);
        map.getProperties().put("height", 10);
        map.getProperties().put("tilewidth", 256);
        map.getProperties().put("tileheight", 128);

        MapLayer layer = new MapLayer();
        layer.setName(TmxMapContract.LAYER_COLLECTIBLES);
        MapObject object = new MapObject();
        object.setName(name);
        object.getProperties().put("x", 128f);
        object.getProperties().put("y", 128f);
        object.getProperties().put(TmxMapContract.PROPERTY_COLLECTIBLE_ID, collectibleId);
        object.getProperties().put(TmxMapContract.PROPERTY_INTERACTION_RADIUS, 72f);
        layer.getObjects().add(object);
        map.getLayers().add(layer);
        return new TmxLevel(map);
    }
}
