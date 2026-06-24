package io.github.iss_2025_2026.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckpointPolylineGeometryTest {

    @Test
    void convertsEveryCheckpointVertexToIsometricWorldCoordinates() {
        TiledMap map = new TiledMap();
        map.getProperties().put("width", 10);
        map.getProperties().put("height", 10);
        map.getProperties().put("tilewidth", 256);
        map.getProperties().put("tileheight", 128);

        Polyline polyline = new Polyline(new float[] {0f, 0f, 100f, 0f});
        polyline.setPosition(128f, 128f);
        PolylineMapObject checkpointObject = new PolylineMapObject(polyline);
        checkpointObject.setName(TmxMapContract.CHECKPOINT_OBJECT_NAME);

        MapLayer checkpointLayer = new MapLayer();
        checkpointLayer.setName(TmxMapContract.LAYER_CHECKPOINTS);
        checkpointLayer.getObjects().add(checkpointObject);
        map.getLayers().add(checkpointLayer);

        TmxLevel level = new TmxLevel(map);
        CheckpointDefinition definition = new CheckpointDefinition(
                "cp_test",
                TmxMapContract.LAYER_CHECKPOINTS,
                TmxMapContract.CHECKPOINT_OBJECT_NAME,
                96f,
                true);

        List<Vector2> vertices = level.checkpointWorldPolyline(definition);

        assertEquals(2, vertices.size());
        assertEquals(256f, vertices.get(0).x, 0.001f);
        assertEquals(64f, vertices.get(0).y, 0.001f);
        assertEquals(356f, vertices.get(1).x, 0.001f);
        assertEquals(14f, vertices.get(1).y, 0.001f);
    }
}
