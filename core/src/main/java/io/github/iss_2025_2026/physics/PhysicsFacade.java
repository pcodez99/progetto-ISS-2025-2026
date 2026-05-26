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

/**
 * Facade pattern per nascondere la complessità del motore fisico Box2D.
 * Gestisce il ciclo di vita del mondo fisico e la sincronizzazione delle coordinate con il modello.
 */
public class PhysicsFacade {
    private static final float PPM = 32f;
    
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private Body playerBody;
    
    private final float playerSize;
    private final float playerYOffset;

    public PhysicsFacade(TmxLevel level, float playerSize, float playerYOffset) {
        this.playerSize = playerSize;
        this.playerYOffset = playerYOffset;
        
        Box2D.init();
        this.world = new World(new Vector2(0f, 0f), true);
        this.debugRenderer = new Box2DDebugRenderer();
        
        // Costruiamo i corpi statici per gli ostacoli della mappa
        new MapPhysicsBuilder(this.world, level.getGeometry(), PPM).createStaticBodies(level);
    }

    public void initPlayerBody(Player player) {
        if (player == null) return;
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set((player.getX() + playerSize / 2f) / PPM, (player.getY() + playerYOffset) / PPM);

        playerBody = world.createBody(bodyDef);
        playerBody.setUserData("player");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(24f / PPM, 28f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
    }

    public void update(float delta, Player player) {
        if (player == null || playerBody == null) return;

        float bodyX = playerBody.getPosition().x * PPM - playerSize / 2f;
        float bodyY = playerBody.getPosition().y * PPM - playerYOffset;

        float attemptedVelocityX = (player.getX() - bodyX) / Math.max(delta, 0.0001f);
        float attemptedVelocityY = (player.getY() - bodyY) / Math.max(delta, 0.0001f);
        
        playerBody.setLinearVelocity(attemptedVelocityX / PPM, attemptedVelocityY / PPM);
        
        world.step(Math.min(delta, 1f / 30f), 6, 2);
        
        player.setX(playerBody.getPosition().x * PPM - playerSize / 2f);
        player.setY(playerBody.getPosition().y * PPM - playerYOffset);
    }

    public void drawDebug(OrthographicCamera camera) {
        Matrix4 debugMatrix = new Matrix4(camera.combined).scale(PPM, PPM, 1f);
        debugRenderer.render(world, debugMatrix);
    }

    public void dispose() {
        debugRenderer.dispose();
        world.dispose();
    }
}
