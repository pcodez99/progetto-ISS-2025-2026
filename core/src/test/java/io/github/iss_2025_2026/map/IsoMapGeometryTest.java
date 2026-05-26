package io.github.iss_2025_2026.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

public class IsoMapGeometryTest {
    @Test
    public void objectToWorldUsesTiledIsometricProjectedCoordinates() {
        IsoMapGeometry geometry = new IsoMapGeometry(campaignMapProperties());

        Vector2 worldPosition = geometry.objectToWorld(836f, 1900f);

        assertEquals(2736f, worldPosition.x, 0.001f);
        assertEquals(596f, worldPosition.y, 0.001f);
    }

    private static TiledMap campaignMapProperties() {
        TiledMap map = new TiledMap();
        map.getProperties().put("width", 60);
        map.getProperties().put("height", 30);
        map.getProperties().put("tilewidth", 256);
        map.getProperties().put("tileheight", 128);
        return map;
    }
}
