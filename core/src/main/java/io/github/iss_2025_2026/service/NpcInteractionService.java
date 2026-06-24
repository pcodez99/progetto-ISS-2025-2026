package io.github.iss_2025_2026.service;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.factory.NpcFactory;
import io.github.iss_2025_2026.map.IsoMapGeometry;
import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Risolve gli spawn NPC della mappa in punti di dialogo interattivi.
 */
public class NpcInteractionService {
    private final List<InteractionPoint> interactionPoints;
    private final float interactionRadius;

    public NpcInteractionService(List<MapObject> npcObjects, NpcFactory npcFactory, IsoMapGeometry geometry,
            int levelId, float interactionRadius) {
        this(npcObjects, npcFactory, geometry, levelCatalogId(levelId), interactionRadius);
    }

    public NpcInteractionService(List<MapObject> npcObjects, NpcFactory npcFactory, IsoMapGeometry geometry,
            String levelCatalogId, float interactionRadius) {
        this.interactionRadius = Math.max(1f, interactionRadius);
        this.interactionPoints = new ArrayList<>();

        if (npcObjects == null || npcFactory == null || geometry == null) {
            return;
        }

        List<Npc> levelNpcs = npcFactory.getNpcsForLevel(levelCatalogId);
        int fallbackIndex = 0;
        for (MapObject object : npcObjects) {
            if (!isNpcInteractionObject(object)) {
                continue;
            }

            Npc npc = resolveNpc(object, npcFactory, levelNpcs, fallbackIndex);
            fallbackIndex++;
            if (npc == null) {
                continue;
            }

            Vector2 world = geometry.objectToWorld(object);
            interactionPoints.add(new MapInteractionPoint(object, world.x, world.y, npc));
        }
    }

    private NpcInteractionService(List<InteractionPoint> interactionPoints, float interactionRadius) {
        this.interactionPoints = interactionPoints != null
                ? new ArrayList<>(interactionPoints)
                : new ArrayList<InteractionPoint>();
        this.interactionRadius = Math.max(1f, interactionRadius);
    }

    public static NpcInteractionService forTesting(float x, float y, Npc npc, float interactionRadius) {
        List<InteractionPoint> points = new ArrayList<>();
        points.add(new TestInteractionPoint(x, y, npc));
        return new NpcInteractionService(points, interactionRadius);
    }

    public NpcInteraction checkInteraction(Player player) {
        if (player == null) {
            return null;
        }

        float playerCenterX = player.getX();
        float playerCenterY = player.getY();
        float radiusSquared = interactionRadius * interactionRadius;
        InteractionPoint nearestPoint = null;
        float nearestDistanceSquared = Float.MAX_VALUE;

        for (InteractionPoint point : interactionPoints) {
            float dx = playerCenterX - point.getX();
            float dy = playerCenterY - point.getY();
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= radiusSquared && distanceSquared < nearestDistanceSquared) {
                nearestPoint = point;
                nearestDistanceSquared = distanceSquared;
            }
        }

        if (nearestPoint == null) {
            return null;
        }
        return toInteraction(nearestPoint);
    }

    public List<NpcInteraction> getInteractions() {
        List<NpcInteraction> interactions = new ArrayList<>();
        for (InteractionPoint point : interactionPoints) {
            interactions.add(toInteraction(point));
        }
        return interactions;
    }

    public int size() {
        return interactionPoints.size();
    }

    private NpcInteraction toInteraction(InteractionPoint point) {
        return new NpcInteraction(point.getMapObject(), point.getNpc().copy(), point.getX(), point.getY());
    }

    public static String levelCatalogId(int levelId) {
        int safeLevelId = Math.max(1, levelId);
        return "level_" + safeLevelId + "_campaign";
    }

    private static boolean isNpcInteractionObject(MapObject object) {
        if (object == null || !object.isVisible()) {
            return false;
        }
        if (readBooleanProperty(object, TmxMapContract.PROPERTY_COLLISION, false)) {
            return false;
        }
        if (!isBlank(readStringProperty(object, TmxMapContract.PROPERTY_NPC_ID, null))) {
            return true;
        }
        if (!isBlank(object.getName())) {
            return true;
        }
        return readBooleanProperty(object, TmxMapContract.PROPERTY_OBJECT_REQUESTED, false);
    }

    private static Npc resolveNpc(MapObject object, NpcFactory npcFactory, List<Npc> levelNpcs, int fallbackIndex) {
        String npcId = readStringProperty(object, TmxMapContract.PROPERTY_NPC_ID, null);
        Optional<Npc> explicitNpc = findNpc(npcFactory, npcId);
        if (explicitNpc.isPresent()) {
            return explicitNpc.get();
        }

        String objectName = object.getName();
        if (!TmxMapContract.LAYER_NPCS.equals(objectName)) {
            Optional<Npc> namedNpc = findNpc(npcFactory, objectName);
            if (namedNpc.isPresent()) {
                return namedNpc.get();
            }
        }

        int npcIndex = readIntProperty(object, TmxMapContract.PROPERTY_NPC_INDEX, -1);
        if (npcIndex >= 0 && npcIndex < levelNpcs.size()) {
            return levelNpcs.get(npcIndex).copy();
        }

        if (fallbackIndex >= 0 && fallbackIndex < levelNpcs.size()) {
            return levelNpcs.get(fallbackIndex).copy();
        }
        return null;
    }

    private static Optional<Npc> findNpc(NpcFactory npcFactory, String npcId) {
        if (isBlank(npcId)) {
            return Optional.empty();
        }
        return npcFactory.findNpc(npcId.trim());
    }

    private static String readStringProperty(MapObject object, String name, String fallback) {
        Object value = object.getProperties().get(name);
        if (value == null) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private static int readIntProperty(MapObject object, String name, int fallback) {
        Object value = object.getProperties().get(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static boolean readBooleanProperty(MapObject object, String name, boolean fallback) {
        Object value = object.getProperties().get(name);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private interface InteractionPoint {
        float getX();

        float getY();

        Npc getNpc();

        MapObject getMapObject();
    }

    private static final class MapInteractionPoint implements InteractionPoint {
        private final MapObject mapObject;
        private final float x;
        private final float y;
        private final Npc npc;

        private MapInteractionPoint(MapObject mapObject, float x, float y, Npc npc) {
            this.mapObject = mapObject;
            this.x = x;
            this.y = y;
            this.npc = npc;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public Npc getNpc() {
            return npc;
        }

        @Override
        public MapObject getMapObject() {
            return mapObject;
        }
    }

    private static final class TestInteractionPoint implements InteractionPoint {
        private final float x;
        private final float y;
        private final Npc npc;

        private TestInteractionPoint(float x, float y, Npc npc) {
            this.x = x;
            this.y = y;
            this.npc = npc;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public Npc getNpc() {
            return npc;
        }

        @Override
        public MapObject getMapObject() {
            return null;
        }
    }
}
