package io.github.iss_2025_2026.service;

import com.badlogic.gdx.math.Rectangle;
import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CollectibleService {
    
    public interface PickupListener {
        void onPickup(Collectible collectible, Player player);
    }

    public static class CollectibleOnMap {
        private final Collectible collectible;
        private final float x;
        private final float y;

        public CollectibleOnMap(Collectible collectible, float x, float y) {
            this.collectible = collectible;
            this.x = x;
            this.y = y;
        }

        public Collectible getCollectible() {
            return collectible;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    private final CollectibleFactory collectibleFactory;
    private final float spawnInterval;
    private final int maxCollectibles;
    private final List<CollectibleOnMap> activeCollectibles;
    private final Random random;
    private float spawnTimer;
    private PickupListener pickupListener;

    public CollectibleService(CollectibleFactory collectibleFactory, float spawnInterval, int maxCollectibles) {
        this.collectibleFactory = collectibleFactory;
        this.spawnInterval = spawnInterval;
        this.maxCollectibles = maxCollectibles;
        this.activeCollectibles = new ArrayList<>();
        this.random = new Random();
        this.spawnTimer = 0f;
    }

    public CollectibleService(CollectibleFactory collectibleFactory) {
        this(collectibleFactory, 15f, 5); // default values: spawn every 15s, max 5 items
    }

    public void setPickupListener(PickupListener pickupListener) {
        this.pickupListener = pickupListener;
    }

    public List<CollectibleOnMap> getActiveCollectibles() {
        return Collections.unmodifiableList(activeCollectibles);
    }

    public void update(float delta, List<Player> players, Rectangle viewportBounds, int levelId) {
        if (players == null || players.isEmpty() || viewportBounds == null) {
            return;
        }

        // 1. Spawning logic
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            if (activeCollectibles.size() < maxCollectibles) {
                spawnCollectibleInPOV(viewportBounds, levelId);
            }
        }
    }

    public CollectibleOnMap getClosestCollectible(Player player, float maxDistance) {
        if (player == null) {
            return null;
        }
        CollectibleOnMap closest = null;
        float minDistanceSquared = maxDistance * maxDistance;

        for (CollectibleOnMap item : activeCollectibles) {
            float dx = player.getX() - item.getX();
            float dy = player.getY() - item.getY();
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= minDistanceSquared) {
                closest = item;
                minDistanceSquared = distanceSquared;
            }
        }
        return closest;
    }

    public boolean pickUp(Player player, CollectibleOnMap item) {
        if (player == null || item == null) {
            return false;
        }
        if (activeCollectibles.contains(item)) {
            if (player.getBackpack().addItem(item.getCollectible())) {
                activeCollectibles.remove(item);
                if (pickupListener != null) {
                    pickupListener.onPickup(item.getCollectible(), player);
                }
                return true;
            }
        }
        return false;
    }

    private void spawnCollectibleInPOV(Rectangle viewportBounds, int levelId) {
        List<Collectible> all = collectibleFactory.getAllCollectibles();
        List<Collectible> levelCollectibles = new ArrayList<>();
        for (Collectible c : all) {
            if (c.canBeUsedInLevel(levelId)) {
                levelCollectibles.add(c);
            }
        }

        if (levelCollectibles.isEmpty()) {
            return;
        }

        Collectible chosen = levelCollectibles.get(random.nextInt(levelCollectibles.size()));
        
        // Pseudo-random position within viewport bounds
        float px = viewportBounds.x + random.nextFloat() * viewportBounds.width;
        float py = viewportBounds.y + random.nextFloat() * viewportBounds.height;
        
        activeCollectibles.add(new CollectibleOnMap(chosen, px, py));
    }
}
