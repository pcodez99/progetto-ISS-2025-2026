package io.github.iss_2025_2026.physics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import io.github.iss_2025_2026.map.MapPhysicsBuilder;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.model.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Facade pattern per nascondere la complessità del motore fisico Box2D.
 * Gestisce il ciclo di vita del mondo fisico e la sincronizzazione delle coordinate con uno o più modelli.
 */
public class PhysicsFacade {
    private static final float PPM = 32f;
    
    private final World world;
    private Box2DDebugRenderer debugRenderer;
    private final Map<Player, Body> playerBodies;
    
    private final float playerSize;
    private final float playerYOffset;

    public PhysicsFacade(TmxLevel level, float playerSize, float playerYOffset) {
        this.playerSize = playerSize;
        this.playerYOffset = playerYOffset;
        this.playerBodies = new HashMap<>();
        
        Box2D.init();
        this.world = new World(new Vector2(0f, 0f), true);
        
        // Costruiamo i corpi statici per gli ostacoli della mappa
        new MapPhysicsBuilder(this.world, level.getGeometry(), PPM).createStaticBodies(level);
    }

    public void initPlayerBody(Player player) {
        if (player == null) return;
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set((player.getX() + playerSize / 2f) / PPM, (player.getY() + playerYOffset) / PPM);

        Body body = world.createBody(bodyDef);
        body.setUserData(player);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(24f / PPM, 28f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef);
        shape.dispose();

        playerBodies.put(player, body);
    }

    public void setPlayerVelocity(Player player, float delta) {
        Body body = playerBodies.get(player);
        if (body == null) return;

        float bodyX = body.getPosition().x * PPM - playerSize / 2f;
        float bodyY = body.getPosition().y * PPM - playerYOffset;

        float attemptedVelocityX = (player.getX() - bodyX) / Math.max(delta, 0.0001f);
        float attemptedVelocityY = (player.getY() - bodyY) / Math.max(delta, 0.0001f);
        
        body.setLinearVelocity(attemptedVelocityX / PPM, attemptedVelocityY / PPM);
    }

    public void step(float delta) {
        world.step(Math.min(delta, 1f / 30f), 6, 2);
    }

    public void syncPlayerPositions() {
        for (Map.Entry<Player, Body> entry : playerBodies.entrySet()) {
            Player player = entry.getKey();
            Body body = entry.getValue();
            player.setX(body.getPosition().x * PPM - playerSize / 2f);
            player.setY(body.getPosition().y * PPM - playerYOffset);
        }
    }

    public void drawDebug(OrthographicCamera camera) {
        if (debugRenderer == null) {
            debugRenderer = new Box2DDebugRenderer();
        }
        Matrix4 debugMatrix = new Matrix4(camera.combined).scale(PPM, PPM, 1f);
        debugRenderer.render(world, debugMatrix);
    }

    public void dispose() {
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        world.dispose();
    }
}
