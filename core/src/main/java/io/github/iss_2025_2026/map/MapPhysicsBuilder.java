package io.github.iss_2025_2026.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Builds Box2D bodies from shapes authored in Tiled object layers.
 */
public final class MapPhysicsBuilder {
    private static final String LOG_TAG = "MapPhysicsBuilder";

    private final World world;
    private final IsoMapGeometry geometry;
    private final float pixelsPerMeter;

    public MapPhysicsBuilder(World world, IsoMapGeometry geometry, float pixelsPerMeter) {
        this.world = world;
        this.geometry = geometry;
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public int createStaticBodies(TmxLevel level) {
        int bodyCount = 0;
        for (MapObject object : level.physicsObjects()) {
            if (createStaticBody(object)) {
                bodyCount++;
            }
        }
        return bodyCount;
    }

    private boolean createStaticBody(MapObject object) {
        if (object instanceof PolygonMapObject) {
            com.badlogic.gdx.math.Polygon polygon = ((PolygonMapObject) object).getPolygon();
            createStaticChain(toPhysicsVertices(polygon.getTransformedVertices()), true, object.getName());
            return true;
        }
        if (object instanceof PolylineMapObject) {
            com.badlogic.gdx.math.Polyline polyline = ((PolylineMapObject) object).getPolyline();
            createStaticChain(toPhysicsVertices(polyline.getTransformedVertices()), false, object.getName());
            return true;
        }
        if (object instanceof RectangleMapObject) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            createStaticChain(toPhysicsVertices(new float[] {
                    rect.x, rect.y,
                    rect.x + rect.width, rect.y,
                    rect.x + rect.width, rect.y + rect.height,
                    rect.x, rect.y + rect.height
            }), true, object.getName());
            return true;
        }

        if (Gdx.app != null) {
            Gdx.app.log(LOG_TAG, "Unsupported TMX physics object skipped: " + object.getClass().getSimpleName());
        }
        return false;
    }

    private float[] toPhysicsVertices(float[] vertices) {
        float[] physicsVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 2) {
            Vector2 worldPosition = geometry.objectToWorld(vertices[i], vertices[i + 1]);
            physicsVertices[i] = worldPosition.x / pixelsPerMeter;
            physicsVertices[i + 1] = worldPosition.y / pixelsPerMeter;
        }
        return physicsVertices;
    }

    private void createStaticChain(float[] physicsVertices, boolean loop, String name) {
        if (physicsVertices.length < 4) {
            return;
        }

        float originX = physicsVertices[0];
        float originY = physicsVertices[1];
        float[] localVertices = new float[physicsVertices.length];
        for (int i = 0; i < physicsVertices.length; i += 2) {
            localVertices[i] = physicsVertices[i] - originX;
            localVertices[i + 1] = physicsVertices[i + 1] - originY;
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(originX, originY);

        Body body = world.createBody(bodyDef);
        body.setUserData(name != null ? name : "obstacle");

        ChainShape shape = new ChainShape();
        if (loop && localVertices.length >= 6) {
            shape.createLoop(localVertices);
        } else {
            shape.createChain(localVertices);
        }
        body.createFixture(shape, 0f);
        shape.dispose();
    }
}
