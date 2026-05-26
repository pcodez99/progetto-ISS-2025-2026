package io.github.iss_2025_2026.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iss_2025_2026.map.TmxMapContract;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Level01MapTest {
    private static final String MAP_FILE = "assets/map/campagna.tmx";
    private static final String MAP_DIRECTORY = "assets/map";

    @Test
    public void mapLoadsAndHasCorrectMetadata() throws Exception {
        Document document = loadMapDocument();
        Element map = document.getDocumentElement();
        assertEquals("isometric", map.getAttribute("orientation"), "The map orientation should be isometric.");
        assertEquals("60", map.getAttribute("width"), "Map width should be 60.");
        assertEquals("30", map.getAttribute("height"), "Map height should be 30.");
        assertEquals("256", map.getAttribute("tilewidth"), "Tile width should be 256.");
        assertEquals("128", map.getAttribute("tileheight"), "Tile height should be 128.");
    }

    @Test
    public void hasSpawnLayerAndSpawnPoints() throws Exception {
        Document document = loadMapDocument();
        Element spawnLayer = objectGroup(document, TmxMapContract.LAYER_SPAWN);
        NodeList objects = spawnLayer.getElementsByTagName("object");
        assertTrue(objects.getLength() >= 1, "There should be at least one spawn point object.");

        boolean foundSpawn = false;
        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            String name = object.getAttribute("name");
            if (TmxMapContract.isPlayerSpawnName(name)) {
                foundSpawn = true;
                assertNotNull(object.getAttribute("x"), "Spawn point must have x coordinate.");
                assertNotNull(object.getAttribute("y"), "Spawn point must have y coordinate.");
            }
        }
        assertTrue(foundSpawn, "Should contain a supported player spawn object.");
    }

    @Test
    public void hasOstacoliLayerAndObstacles() throws Exception {
        Document document = loadMapDocument();
        Element obstaclesLayer = objectGroup(document, TmxMapContract.LAYER_OBSTACLES);
        NodeList objects = obstaclesLayer.getElementsByTagName("object");
        assertTrue(objects.getLength() >= 5, "There should be at least 5 obstacle objects.");

        boolean foundCollisionObject = false;
        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            boolean hasPolygon = object.getElementsByTagName("polygon").getLength() > 0;
            boolean hasPolyline = object.getElementsByTagName("polyline").getLength() > 0;
            boolean hasUnsupportedShape = object.getElementsByTagName("ellipse").getLength() > 0
                    || object.getElementsByTagName("capsule").getLength() > 0
                    || object.getElementsByTagName("point").getLength() > 0
                    || object.getElementsByTagName("text").getLength() > 0;
            boolean hasRectangle = !hasPolygon && !hasPolyline && !hasUnsupportedShape;
            boolean hasCollision = hasBooleanProperty(object, TmxMapContract.PROPERTY_COLLISION, true);
            if (hasCollision) {
                foundCollisionObject = true;
                assertTrue(hasRectangle || hasPolygon || hasPolyline,
                        "Collidable obstacle object must be a rectangle, polygon or polyline.");
            }
        }
        assertTrue(foundCollisionObject, "There should be at least one collision=true obstacle.");
    }

    @Test
    public void hasExpectedTiledLayers() throws Exception {
        Document document = loadMapDocument();
        assertNotNull(layer(document, "terreno"), "Map should have 'terreno' tiled layer.");
        assertNotNull(layer(document, "oggetti"), "Map should have 'oggetti' tiled layer.");
    }

    @Test
    public void referencedImagesExistNextToTmx() throws Exception {
        Document document = loadMapDocument();
        NodeList images = document.getElementsByTagName("image");
        Path mapDirectory = Paths.get(MAP_DIRECTORY);
        if (!Files.exists(mapDirectory)) {
            mapDirectory = Paths.get("..").resolve(MAP_DIRECTORY);
        }

        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);
            String source = image.getAttribute("source");
            if (source == null || source.trim().isEmpty()) {
                continue;
            }
            assertTrue(Files.exists(mapDirectory.resolve(source)),
                    "Missing TMX image source: " + source);
        }
    }

    @Test
    public void proceduralGeneratorHasBeenRemoved() {
        assertFalse(Files.exists(projectPath("assets/map/kenney_isometric-miniature-farm/generate_map.py")),
                "TMX maps must be authored in Tiled, not generated by local scripts.");
        assertFalse(Files.exists(projectPath("assets/map/generated/level_01_countryside_crash.tmx")),
                "Legacy generated TMX should not be part of the runtime map pipeline.");
        assertFalse(Files.exists(projectPath("assets/map/kenney_isometric-miniature-farm/countryside_level.tmx")),
                "Sample/generated TMX files should not be part of the runtime map pipeline.");
    }

    private static Document loadMapDocument() throws Exception {
        Path path = Paths.get(MAP_FILE);
        if (!Files.exists(path)) {
            path = Paths.get("..").resolve(MAP_FILE);
        }
        assertTrue(Files.exists(path), "Missing map file: " + MAP_FILE);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(path.toFile());
        document.getDocumentElement().normalize();
        return document;
    }

    private static Element objectGroup(Document document, String groupName) {
        NodeList groups = document.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (groupName.equals(group.getAttribute("name"))) {
                return group;
            }
        }
        throw new AssertionError("Missing objectgroup: " + groupName);
    }

    private static Element layer(Document document, String layerName) {
        NodeList layers = document.getElementsByTagName("layer");
        for (int i = 0; i < layers.getLength(); i++) {
            Element layer = (Element) layers.item(i);
            if (layerName.equals(layer.getAttribute("name"))) {
                return layer;
            }
        }
        throw new AssertionError("Missing layer: " + layerName);
    }

    private static Path projectPath(String path) {
        Path candidate = Paths.get(path);
        if (Files.exists(Paths.get("assets"))) {
            return candidate;
        }
        return Paths.get("..").resolve(path);
    }

    private static boolean hasBooleanProperty(Element object, String propertyName, boolean expectedValue) {
        NodeList properties = object.getElementsByTagName("property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (propertyName.equals(property.getAttribute("name"))) {
                return Boolean.toString(expectedValue).equals(property.getAttribute("value"));
            }
        }
        return false;
    }
}
