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
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Facade pattern per nascondere la complessità del motore fisico Box2D.
 * Gestisce il ciclo di vita del mondo fisico e la sincronizzazione delle coordinate con uno o più modelli.
 */
public class PhysicsFacade {
    private static final float PPM = 32f;
    private static final float PLAYER_HALF_WIDTH = 24f;
    private static final float PLAYER_HALF_HEIGHT = 28f;
    private static final short PLAYER_COLLISION_GROUP = -1;
    private static final float COLLISION_EPSILON = 0.0001f;
    
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
        shape.setAsBox(PLAYER_HALF_WIDTH / PPM, PLAYER_HALF_HEIGHT / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        // La collisione reciproca viene risolta prima dello step per bloccare entrambi,
        // evitando che due corpi dinamici si trasferiscano impulso e si trascinino.
        fixtureDef.filter.groupIndex = PLAYER_COLLISION_GROUP;
        body.createFixture(fixtureDef);
        shape.dispose();

        playerBodies.put(player, body);
    }

    public void setPlayerVelocity(Player player, float delta) {
        Body body = playerBodies.get(player);
        if (body == null) return;

        setVelocityTowardsModelPosition(body, player, delta);
    }

    /**
     * Applica congiuntamente le velocità dei due player. Se le traiettorie relative
     * entrano nell'ingombro reciproco, entrambi vengono fermati per questo step.
     */
    public void setMultiplayerVelocities(Player firstPlayer, Player secondPlayer, float delta) {
        Body firstBody = playerBodies.get(firstPlayer);
        Body secondBody = playerBodies.get(secondPlayer);
        if (firstBody == null || secondBody == null) {
            setPlayerVelocity(firstPlayer, delta);
            setPlayerVelocity(secondPlayer, delta);
            return;
        }

        float firstTargetX = modelBodyX(firstPlayer);
        float firstTargetY = modelBodyY(firstPlayer);
        float secondTargetX = modelBodyX(secondPlayer);
        float secondTargetY = modelBodyY(secondPlayer);

        if (wouldEnterPlayerFootprint(firstBody, secondBody,
                firstTargetX, firstTargetY, secondTargetX, secondTargetY)) {
            firstBody.setLinearVelocity(0f, 0f);
            secondBody.setLinearVelocity(0f, 0f);
            stopWalking(firstPlayer);
            stopWalking(secondPlayer);
            return;
        }

        setVelocityTowardsTarget(firstBody, firstTargetX, firstTargetY, delta);
        setVelocityTowardsTarget(secondBody, secondTargetX, secondTargetY, delta);
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

    private void setVelocityTowardsModelPosition(Body body, Player player, float delta) {
        setVelocityTowardsTarget(body, modelBodyX(player), modelBodyY(player), delta);
    }

    private void setVelocityTowardsTarget(Body body, float targetX, float targetY, float delta) {
        float safeDelta = Math.max(delta, 0.0001f);
        body.setLinearVelocity(
                (targetX - body.getPosition().x) / safeDelta,
                (targetY - body.getPosition().y) / safeDelta);
    }

    private float modelBodyX(Player player) {
        return (player.getX() + playerSize / 2f) / PPM;
    }

    private float modelBodyY(Player player) {
        return (player.getY() + playerYOffset) / PPM;
    }

    private boolean wouldEnterPlayerFootprint(Body firstBody, Body secondBody,
            float firstTargetX, float firstTargetY, float secondTargetX, float secondTargetY) {
        float startRelativeX = firstBody.getPosition().x - secondBody.getPosition().x;
        float startRelativeY = firstBody.getPosition().y - secondBody.getPosition().y;
        float targetRelativeX = firstTargetX - secondTargetX;
        float targetRelativeY = firstTargetY - secondTargetY;

        float relativeDeltaX = targetRelativeX - startRelativeX;
        float relativeDeltaY = targetRelativeY - startRelativeY;
        if (Math.abs(relativeDeltaX) <= COLLISION_EPSILON
                && Math.abs(relativeDeltaY) <= COLLISION_EPSILON) {
            return false;
        }

        float combinedHalfWidth = PLAYER_HALF_WIDTH * 2f / PPM;
        float combinedHalfHeight = PLAYER_HALF_HEIGHT * 2f / PPM;
        float startScore = normalizedDistanceSquared(
                startRelativeX, startRelativeY, combinedHalfWidth, combinedHalfHeight);
        float targetScore = normalizedDistanceSquared(
                targetRelativeX, targetRelativeY, combinedHalfWidth, combinedHalfHeight);
        if (targetScore > startScore + COLLISION_EPSILON) {
            return false;
        }

        return segmentIntersectsBox(
                startRelativeX, startRelativeY,
                targetRelativeX, targetRelativeY,
                combinedHalfWidth, combinedHalfHeight);
    }

    private float normalizedDistanceSquared(float x, float y, float halfWidth, float halfHeight) {
        float normalizedX = x / halfWidth;
        float normalizedY = y / halfHeight;
        return normalizedX * normalizedX + normalizedY * normalizedY;
    }

    private boolean segmentIntersectsBox(float startX, float startY, float endX, float endY,
            float halfWidth, float halfHeight) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float minimumTime = 0f;
        float maximumTime = 1f;

        if (Math.abs(deltaX) <= COLLISION_EPSILON) {
            if (Math.abs(startX) > halfWidth) {
                return false;
            }
        } else {
            float entryX = (-halfWidth - startX) / deltaX;
            float exitX = (halfWidth - startX) / deltaX;
            if (entryX > exitX) {
                float swap = entryX;
                entryX = exitX;
                exitX = swap;
            }
            minimumTime = Math.max(minimumTime, entryX);
            maximumTime = Math.min(maximumTime, exitX);
            if (minimumTime > maximumTime) {
                return false;
            }
        }

        if (Math.abs(deltaY) <= COLLISION_EPSILON) {
            return Math.abs(startY) <= halfHeight;
        }

        float entryY = (-halfHeight - startY) / deltaY;
        float exitY = (halfHeight - startY) / deltaY;
        if (entryY > exitY) {
            float swap = entryY;
            entryY = exitY;
            exitY = swap;
        }
        minimumTime = Math.max(minimumTime, entryY);
        maximumTime = Math.min(maximumTime, exitY);
        return minimumTime <= maximumTime;
    }

    private void stopWalking(Player player) {
        if (player != null && player.getState() == CharacterState.WALKING) {
            player.setState(CharacterState.IDLE);
        }
    }
}
