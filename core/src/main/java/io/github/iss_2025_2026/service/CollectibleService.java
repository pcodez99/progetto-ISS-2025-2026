package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectibleService {
    private static final float DEFAULT_INTERACTION_RADIUS = 80f;
    
    public interface PickupListener {
        void onPickup(Collectible collectible, Player player);
    }

    public static class CollectibleOnMap {
        private final String placementId;
        private final Collectible collectible;
        private final float x;
        private final float y;
        private final float interactionRadius;

        public CollectibleOnMap(Collectible collectible, float x, float y) {
            this(null, collectible, x, y, DEFAULT_INTERACTION_RADIUS);
        }

        public CollectibleOnMap(String placementId, Collectible collectible, float x, float y) {
            this(placementId, collectible, x, y, DEFAULT_INTERACTION_RADIUS);
        }

        public CollectibleOnMap(String placementId, Collectible collectible, float x, float y,
                float interactionRadius) {
            this.placementId = placementId;
            this.collectible = collectible;
            this.x = x;
            this.y = y;
            if (interactionRadius <= 0f) {
                throw new IllegalArgumentException("Il raggio di interazione deve essere positivo.");
            }
            this.interactionRadius = interactionRadius;
        }

        public String getPlacementId() {
            return placementId;
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

        public float getInteractionRadius() {
            return interactionRadius;
        }
    }

    private final List<CollectibleOnMap> activeCollectibles;
    private final float playerInteractionOffsetX;
    private final float playerInteractionOffsetY;
    private PickupListener pickupListener;

    public CollectibleService(List<CollectibleOnMap> mapCollectibles) {
        this(mapCollectibles, 0f, 0f);
    }

    public CollectibleService(List<CollectibleOnMap> mapCollectibles,
            float playerInteractionOffsetX, float playerInteractionOffsetY) {
        this.activeCollectibles = new ArrayList<>();
        if (mapCollectibles != null) {
            this.activeCollectibles.addAll(mapCollectibles);
        }
        this.playerInteractionOffsetX = playerInteractionOffsetX;
        this.playerInteractionOffsetY = playerInteractionOffsetY;
    }

    public void setPickupListener(PickupListener pickupListener) {
        this.pickupListener = pickupListener;
    }

    public List<CollectibleOnMap> getActiveCollectibles() {
        return Collections.unmodifiableList(activeCollectibles);
    }

    public CollectibleOnMap getClosestCollectible(Player player) {
        if (player == null) {
            return null;
        }
        CollectibleOnMap closest = null;
        float minDistanceSquared = Float.MAX_VALUE;
        float playerInteractionX = player.getX() + playerInteractionOffsetX;
        float playerInteractionY = player.getY() + playerInteractionOffsetY;

        for (CollectibleOnMap item : activeCollectibles) {
            float dx = playerInteractionX - item.getX();
            float dy = playerInteractionY - item.getY();
            float distanceSquared = dx * dx + dy * dy;
            float radiusSquared = item.getInteractionRadius() * item.getInteractionRadius();
            if (distanceSquared <= radiusSquared && distanceSquared < minDistanceSquared) {
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

}
