package io.github.iss_2025_2026.map;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Keeps all isometric TMX/world coordinate conversion in one place.
 */
public final class IsoMapGeometry {
    private final TiledMap map;
    private final int mapWidth;
    private final int mapHeight;
    private final int tileWidth;
    private final int tileHeight;
    private final Rectangle bounds;

    public IsoMapGeometry(TiledMap map) {
        this.map = map;
        MapProperties properties = map.getProperties();
        this.mapWidth = properties.get("width", Integer.class);
        this.mapHeight = properties.get("height", Integer.class);
        this.tileWidth = properties.get("tilewidth", Integer.class);
        this.tileHeight = properties.get("tileheight", Integer.class);
        this.bounds = calculateBounds();
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public float mapPropertyFloat(String name, float fallback) {
        return propertyFloat(map.getProperties(), name, fallback);
    }

    public Vector2 tileToWorld(int tileX, int tileY) {
        return new Vector2(
                (tileX + tileY) * tileWidth / 2f,
                (tileY - tileX) * tileHeight / 2f);
    }

    public Vector2 objectToWorld(MapObject object) {
        MapProperties properties = object.getProperties();
        return objectToWorld(
                propertyFloat(properties, "x"),
                propertyFloat(properties, "y"));
    }

    public Vector2 objectToWorld(float loaderObjectX, float loaderObjectY) {
        float tileX = loaderObjectX / tileHeight - 0.5f;
        float tileY = loaderObjectY / tileHeight + 0.5f;
        return tileToWorld(tileX, tileY);
    }

    private Vector2 tileToWorld(float tileX, float tileY) {
        return new Vector2(
                (tileX + tileY) * tileWidth / 2f,
                (tileY - tileX) * tileHeight / 2f);
    }

    private Rectangle calculateBounds() {
        float maxTileImageWidth = tileWidth;
        float maxTileImageHeight = tileHeight;
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getTextureRegion() != null) {
                    maxTileImageWidth = Math.max(maxTileImageWidth, tile.getTextureRegion().getRegionWidth());
                    maxTileImageHeight = Math.max(maxTileImageHeight, tile.getTextureRegion().getRegionHeight());
                }
            }
        }

        float minX = 0f;
        float maxX = (float) (mapWidth + mapHeight) * tileWidth / 2f + maxTileImageWidth;
        float minY = -(float) mapWidth * tileHeight / 2f;
        float maxY = (float) mapHeight * tileHeight / 2f + maxTileImageHeight;
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static float propertyFloat(MapProperties properties, String name, float fallback) {
        Object value = properties.get(name);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        return fallback;
    }

    private static float propertyFloat(MapProperties properties, String name) {
        Object value = properties.get(name);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        throw new IllegalArgumentException("Missing numeric TMX object property: " + name);
    }
}
