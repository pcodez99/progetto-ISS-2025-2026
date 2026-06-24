package io.github.iss_2025_2026.map;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.service.CollectibleService;
import java.util.ArrayList;
import java.util.List;

/** Converte le istanze dichiarate nel TMX in oggetti runtime. */
public final class TmxCollectibleLoader {
    private final CollectibleFactory collectibleFactory;

    public TmxCollectibleLoader(CollectibleFactory collectibleFactory) {
        if (collectibleFactory == null) {
            throw new IllegalArgumentException("La factory dei collectible non puo essere nulla.");
        }
        this.collectibleFactory = collectibleFactory;
    }

    public List<CollectibleService.CollectibleOnMap> load(TmxLevel level, int levelId) {
        if (level == null) {
            throw new IllegalArgumentException("Il livello TMX non puo essere nullo.");
        }

        List<CollectibleService.CollectibleOnMap> placements = new ArrayList<>();
        for (MapObject object : level.collectibleObjects()) {
            String collectibleId = requiredStringProperty(object, TmxMapContract.PROPERTY_COLLECTIBLE_ID);
            Collectible collectible = collectibleFactory.findCollectible(collectibleId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Collectible TMX sconosciuto '" + collectibleId + "' nell'oggetto '"
                                    + objectLabel(object) + "'."));
            if (!collectible.canBeUsedInLevel(levelId)) {
                throw new IllegalStateException("Collectible TMX '" + collectibleId
                        + "' non utilizzabile nel livello " + levelId + ".");
            }
            Vector2 worldPosition = level.getGeometry().objectToWorld(object);
            float interactionRadius = positiveFloatProperty(
                    object,
                    TmxMapContract.PROPERTY_INTERACTION_RADIUS,
                    TmxMapContract.DEFAULT_INTERACTION_RADIUS);
            placements.add(new CollectibleService.CollectibleOnMap(
                    objectLabel(object), collectible, worldPosition.x, worldPosition.y, interactionRadius));
        }
        return placements;
    }

    private static float positiveFloatProperty(MapObject object, String propertyName, float fallback) {
        Object value = object.getProperties().get(propertyName);
        if (value == null) {
            return fallback;
        }
        try {
            float parsed = value instanceof Number
                    ? ((Number) value).floatValue()
                    : Float.parseFloat(value.toString());
            if (parsed > 0f) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // Gestito con l'errore esplicito sotto.
        }
        throw new IllegalStateException("Oggetto TMX '" + objectLabel(object)
                + "' con proprieta '" + propertyName + "' non valida: " + value + ".");
    }

    private static String requiredStringProperty(MapObject object, String propertyName) {
        Object value = object.getProperties().get(propertyName);
        String text = value != null ? value.toString().trim() : "";
        if (text.isEmpty()) {
            throw new IllegalStateException("Oggetto TMX '" + objectLabel(object)
                    + "' senza proprieta obbligatoria '" + propertyName + "'.");
        }
        return text;
    }

    private static String objectLabel(MapObject object) {
        String name = object.getName();
        return name == null || name.trim().isEmpty() ? "senza nome" : name;
    }
}
