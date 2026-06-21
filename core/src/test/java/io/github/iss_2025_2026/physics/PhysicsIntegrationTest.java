package io.github.iss_2025_2026.physics;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.map.TmxLevel;
import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PhysicsIntegrationTest {
    private static final float PLAYER_SIZE = 160f;
    private static final float PLAYER_Y_OFFSET = PLAYER_SIZE * 0.48f;

    @BeforeAll
    public static void init() {
        new HeadlessApplication(new ApplicationListener() {
            @Override public void create() {}
            @Override public void resize(int width, int height) {}
            @Override public void render() {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void dispose() {}
        });
    }

    @Test
    public void testTmxPhysicsCollisionResolution() {
        TiledMap map = new TiledMap();
        map.getProperties().put("width", 10);
        map.getProperties().put("height", 10);
        map.getProperties().put("tilewidth", 256);
        map.getProperties().put("tileheight", 128);

        // Add spawn layer
        MapLayer spawnLayer = new MapLayer();
        spawnLayer.setName(TmxMapContract.LAYER_SPAWN);
        MapObject spawnObj = new MapObject();
        spawnObj.setName(TmxMapContract.SPAWN_OBJECT_NAME);
        spawnObj.getProperties().put("x", 200f);
        spawnObj.getProperties().put("y", 200f);
        spawnLayer.getObjects().add(spawnObj);
        map.getLayers().add(spawnLayer);

        // Add obstacles layer
        MapLayer obstaclesLayer = new MapLayer();
        obstaclesLayer.setName(TmxMapContract.LAYER_OBSTACLES);
        
        // Add a collision rectangle at (250, 250) of size 100x100
        com.badlogic.gdx.maps.objects.RectangleMapObject wallObj = 
                new com.badlogic.gdx.maps.objects.RectangleMapObject(250f, 250f, 100f, 100f);
        wallObj.getProperties().put(TmxMapContract.PROPERTY_COLLISION, true);
        obstaclesLayer.getObjects().add(wallObj);
        map.getLayers().add(obstaclesLayer);

        TmxLevel level = new TmxLevel(map);
        assertNotNull(level);
        
        // Midpoint calculations and offsets setup
        float playerSize = 160f;
        float playerYOffset = playerSize * 0.48f;
        PhysicsFacade physicsFacade = new PhysicsFacade(level, playerSize, playerYOffset);
        
        Player player = new Player("TestHero", 100, 10, null);
        
        // Spawn player at map spawn point
        Vector2 spawn = level.playerSpawnWorldPosition();
        player.setX(spawn.x - playerSize / 2f);
        player.setY(spawn.y - playerYOffset);
        
        physicsFacade.initPlayerBody(player);
        
        // Settle coordinates
        physicsFacade.setPlayerVelocity(player, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        float initialX = player.getX();
        float initialY = player.getY();

        // 1. Move player in a free area (e.g. +10 pixels along X)
        player.setX(initialX + 10f);
        physicsFacade.setPlayerVelocity(player, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();
        
        // Position should have changed
        assertTrue(player.getX() > initialX);

        // 2. Try to move player directly inside the collidable wall object.
        // The wall is at (250, 250) in map loader coordinates. Let's map it.
        // Let's force the player's model coordinates to teleport inside the wall.
        Vector2 wallCenterWorld = level.getGeometry().objectToWorld(300f, 300f);
        player.setX(wallCenterWorld.x - playerSize / 2f);
        player.setY(wallCenterWorld.y - playerYOffset);
        
        // Physics engine should block the body and not let it settle inside the wall
        physicsFacade.setPlayerVelocity(player, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();
        
        // The synchronized player model coordinate should be pushed out/blocked by the wall, 
        // meaning it cannot equal the requested wallCenterWorld position exactly!
        float distanceToWallCenter = Vector2.dst(player.getX() + playerSize / 2f, player.getY() + playerYOffset, wallCenterWorld.x, wallCenterWorld.y);
        assertTrue(distanceToWallCenter > 10f, "Player body should be blocked from reaching the center of the wall");

        physicsFacade.dispose();
        map.dispose();
    }

    @Test
    public void multiplayerBodiesStopTogetherInsteadOfPushingEachOther() {
        TiledMap map = emptyMap();
        PhysicsFacade physicsFacade = new PhysicsFacade(new TmxLevel(map), PLAYER_SIZE, PLAYER_Y_OFFSET);
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(60f, 0f);
        physicsFacade.initPlayerBody(playerOne);
        physicsFacade.initPlayerBody(playerTwo);

        playerTwo.setX(20f);
        playerTwo.setState(CharacterState.WALKING);
        physicsFacade.setMultiplayerVelocities(playerOne, playerTwo, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(60f, playerTwo.getX(), 0.01f);
        assertEquals(CharacterState.IDLE, playerTwo.getState());

        physicsFacade.dispose();
        map.dispose();
    }

    @Test
    public void multiplayerBodiesCanMoveAwayFromEachOther() {
        TiledMap map = emptyMap();
        PhysicsFacade physicsFacade = new PhysicsFacade(new TmxLevel(map), PLAYER_SIZE, PLAYER_Y_OFFSET);
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(40f, 0f);
        physicsFacade.initPlayerBody(playerOne);
        physicsFacade.initPlayerBody(playerTwo);

        playerOne.setX(-10f);
        playerTwo.setX(50f);
        physicsFacade.setMultiplayerVelocities(playerOne, playerTwo, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        assertTrue(playerOne.getX() < 0f);
        assertTrue(playerTwo.getX() > 40f);

        physicsFacade.dispose();
        map.dispose();
    }

    @Test
    public void playerOneCannotPushPlayerTwo() {
        TiledMap map = emptyMap();
        PhysicsFacade physicsFacade = new PhysicsFacade(new TmxLevel(map), PLAYER_SIZE, PLAYER_Y_OFFSET);
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(60f, 0f);
        physicsFacade.initPlayerBody(playerOne);
        physicsFacade.initPlayerBody(playerTwo);

        playerOne.setX(40f);
        physicsFacade.setMultiplayerVelocities(playerOne, playerTwo, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(60f, playerTwo.getX(), 0.01f);

        physicsFacade.dispose();
        map.dispose();
    }

    @Test
    public void multiplayerBodiesCanMoveTogetherInTheSameDirection() {
        TiledMap map = emptyMap();
        PhysicsFacade physicsFacade = new PhysicsFacade(new TmxLevel(map), PLAYER_SIZE, PLAYER_Y_OFFSET);
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(60f, 0f);
        physicsFacade.initPlayerBody(playerOne);
        physicsFacade.initPlayerBody(playerTwo);

        playerOne.setX(10f);
        playerTwo.setX(70f);
        physicsFacade.setMultiplayerVelocities(playerOne, playerTwo, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        assertEquals(10f, playerOne.getX(), 0.01f);
        assertEquals(70f, playerTwo.getX(), 0.01f);

        physicsFacade.dispose();
        map.dispose();
    }

    @Test
    public void multiplayerBodiesCannotCrossThroughEachOtherInOneFrame() {
        TiledMap map = emptyMap();
        PhysicsFacade physicsFacade = new PhysicsFacade(new TmxLevel(map), PLAYER_SIZE, PLAYER_Y_OFFSET);
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(100f, 0f);
        physicsFacade.initPlayerBody(playerOne);
        physicsFacade.initPlayerBody(playerTwo);

        playerOne.setX(100f);
        playerTwo.setX(0f);
        physicsFacade.setMultiplayerVelocities(playerOne, playerTwo, 0.016f);
        physicsFacade.step(0.016f);
        physicsFacade.syncPlayerPositions();

        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(100f, playerTwo.getX(), 0.01f);

        physicsFacade.dispose();
        map.dispose();
    }

    private static TiledMap emptyMap() {
        TiledMap map = new TiledMap();
        map.getProperties().put("width", 10);
        map.getProperties().put("height", 10);
        map.getProperties().put("tilewidth", 256);
        map.getProperties().put("tileheight", 128);
        return map;
    }

    private static Player playerAt(float x, float y) {
        Player player = new Player("TestHero", 100, 10, null);
        player.setX(x);
        player.setY(y);
        return player;
    }
}
