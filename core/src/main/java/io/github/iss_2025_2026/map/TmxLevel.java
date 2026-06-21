package io.github.iss_2025_2026.map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime view of a Tiled-authored level. No procedural map data is created here.
 */
public final class TmxLevel {
    private final TiledMap map;
    private final IsoMapGeometry geometry;

    public TmxLevel(TiledMap map) {
        this.map = map;
        this.geometry = new IsoMapGeometry(map);
    }

    public TiledMap getMap() {
        return map;
    }

    public IsoMapGeometry getGeometry() {
        return geometry;
    }

    public Vector2 playerSpawnWorldPosition() {
        MapLayer spawnLayer = requireLayer(TmxMapContract.LAYER_SPAWN);
        for (MapObject object : spawnLayer.getObjects()) {
            if (!object.isVisible()) {
                continue;
            }
            if (TmxMapContract.isPlayerSpawnName(object.getName())) {
                return geometry.objectToWorld(object);
            }
        }
        throw new IllegalStateException("TMX layer '" + TmxMapContract.LAYER_SPAWN
                + "' must contain a visible object named '" + TmxMapContract.SPAWN_OBJECT_NAME + "'.");
    }

    public List<Vector2> checkpointWorldPolyline(CheckpointDefinition checkpoint) {
        if (checkpoint == null) {
            throw new IllegalArgumentException("La definizione del checkpoint non puo essere nulla.");
        }

        MapLayer checkpointLayer = requireLayer(checkpoint.getLayer());
        MapObject firstVisibleCheckpointLine = null;
        for (MapObject object : checkpointLayer.getObjects()) {
            if (!object.isVisible()) {
                continue;
            }
            if (checkpoint.getObjectName().equals(object.getName())) {
                return worldPolyline(object, checkpoint.getLayer());
            }
            if (firstVisibleCheckpointLine == null) {
                firstVisibleCheckpointLine = object;
            }
        }

        if (firstVisibleCheckpointLine != null) {
            return worldPolyline(firstVisibleCheckpointLine, checkpoint.getLayer());
        }

        throw new IllegalStateException("TMX layer '" + checkpoint.getLayer()
                + "' must contain a visible checkpoint line.");
    }

    private List<Vector2> worldPolyline(MapObject object, String layerName) {
        if (!(object instanceof PolylineMapObject)) {
            throw new IllegalStateException("TMX checkpoint object '" + object.getName()
                    + "' in layer '" + layerName + "' must be a polyline.");
        }

        float[] vertices = ((PolylineMapObject) object).getPolyline().getTransformedVertices();
        if (vertices.length < 4) {
            throw new IllegalStateException("TMX checkpoint polyline '" + object.getName()
                    + "' must contain at least two vertices.");
        }

        List<Vector2> worldVertices = new ArrayList<>();
        for (int index = 0; index < vertices.length; index += 2) {
            worldVertices.add(geometry.objectToWorld(vertices[index], vertices[index + 1]));
        }
        return Collections.unmodifiableList(worldVertices);
    }

    public List<MapObject> physicsObjects() {
        List<MapObject> objects = new ArrayList<>();
        addPhysicsObjects(objects, TmxMapContract.LAYER_OBSTACLES, true);
        addPhysicsObjects(objects, TmxMapContract.LAYER_RIGID_BODIES, false);
        return Collections.unmodifiableList(objects);
    }

    public List<MapObject> enemyObjects() {
        List<MapObject> objects = new ArrayList<>();
        MapLayer layer = map.getLayers().get(TmxMapContract.LAYER_ENEMIES);
        if (layer == null) {
            return Collections.unmodifiableList(objects);
        }
        for (MapObject object : layer.getObjects()) {
            if (object.isVisible()) {
                objects.add(object);
            }
        }
        return Collections.unmodifiableList(objects);
    }

    public List<MapObject> npcObjects() {
        List<MapObject> objects = new ArrayList<>();
        MapLayer layer = map.getLayers().get(TmxMapContract.LAYER_NPCS);
        if (layer == null) {
            return Collections.unmodifiableList(objects);
        }
        for (MapObject object : layer.getObjects()) {
            if (object.isVisible()) {
                objects.add(object);
            }
        }
        return Collections.unmodifiableList(objects);
    }

    /** Restituisce i collectible piazzati in Tiled, senza crearne di procedurali. */
    public List<MapObject> collectibleObjects() {
        List<MapObject> objects = new ArrayList<>();
        MapLayer layer = requireLayer(TmxMapContract.LAYER_COLLECTIBLES);
        for (MapObject object : layer.getObjects()) {
            if (object.isVisible()) {
                objects.add(object);
            }
        }
        return Collections.unmodifiableList(objects);
    }

    private void addPhysicsObjects(List<MapObject> target, String layerName, boolean requireCollisionProperty) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) {
            return;
        }
        for (MapObject object : layer.getObjects()) {
            if (!object.isVisible()) {
                continue;
            }
            if (!requireCollisionProperty || propertyBoolean(object, TmxMapContract.PROPERTY_COLLISION, false)) {
                target.add(object);
            }
        }
    }

    private MapLayer requireLayer(String layerName) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) {
            throw new IllegalStateException("Missing required TMX object layer: " + layerName);
        }
        return layer;
    }

    private static boolean propertyBoolean(MapObject object, String name, boolean fallback) {
        Object value = object.getProperties().get(name);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }
}
