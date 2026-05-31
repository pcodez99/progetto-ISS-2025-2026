package io.github.iss_2025_2026.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Level01MapTest {
    private static final String LEVEL_ONE_MAP = "map/levels/1/level.tmx";

    @Test
    public void manifestDefinesTheAvailableDevRuntimeLevel() throws Exception {
        LevelCatalog catalog = loadProjectCatalog();

        assertEquals(1, catalog.getLevels().size());
        assertEquals("map/levels/1/level.tmx", catalog.requireLevel(1).getMapPath());
    }

    @Test
    public void levelOneMapUsesRuntimeFolderContract() throws Exception {
        Document document = loadProjectMapDocument(LEVEL_ONE_MAP);
        Element map = document.getDocumentElement();

        assertEquals("isometric", map.getAttribute("orientation"));
        assertTrue(Integer.parseInt(map.getAttribute("width")) > 0);
        assertTrue(Integer.parseInt(map.getAttribute("height")) > 0);
        assertNotNull(layer(document, "terreno"));
        assertNotNull(objectGroup(document, TmxMapContract.LAYER_SPAWN));
        assertNotNull(objectGroup(document, TmxMapContract.LAYER_OBSTACLES));
    }

    @Test
    public void levelOneImageSourcesAreOrganizedBySharedAndLevelAssets() throws Exception {
        Document document = loadProjectMapDocument(LEVEL_ONE_MAP);
        NodeList images = document.getElementsByTagName("image");
        boolean foundSharedTile = false;
        boolean foundLevelAsset = false;

        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);
            String source = image.getAttribute("source");
            assertFalse(source.startsWith("Isometric/"), "TMX must not reference the old map/Isometric folder.");
            assertFalse(source.startsWith("ship/"), "TMX must not reference the old map/ship folder.");
            foundSharedTile = foundSharedTile || source.startsWith("../../shared/tiles/isometric/");
            foundLevelAsset = foundLevelAsset || source.startsWith("assets/ship/");
        }

        assertTrue(foundSharedTile, "Level 1 should reference shared isometric tiles.");
        assertTrue(foundLevelAsset, "Level 1 should reference level-specific ship assets.");
    }

    @Test
    public void validatorAllowsOnlyLevelOneInDevMode(@TempDir Path tempDir) throws Exception {
        createMinimalValidLevel(tempDir);

        LevelAssetResolver resolver = LevelAssetResolvers.filesystem(tempDir);
        LevelCatalog catalog = LevelCatalog.load(resolver);
        LevelValidationResult result = new LevelAssetValidator(resolver).validate(catalog, true);

        assertTrue(result.isValid(), result.toUserMessage());
    }

    @Test
    public void validatorReportsMissingLevelsOutsideDevMode(@TempDir Path tempDir) throws Exception {
        createMinimalValidLevel(tempDir);

        LevelAssetResolver resolver = LevelAssetResolvers.filesystem(tempDir);
        LevelCatalog catalog = LevelCatalog.load(resolver);
        LevelValidationResult result = new LevelAssetValidator(resolver).validate(catalog, false);

        assertFalse(result.isValid());
        assertTrue(result.toUserMessage().contains("Avvio bloccato"));
        assertTrue(result.toUserMessage().contains("manca il livello 2"));
        assertTrue(result.toUserMessage().contains("manca il livello 3"));
    }

    @Test
    public void validatorReportsMissingMapsWithClearStartupMessage(@TempDir Path tempDir) throws Exception {
        Path mapDirectory = tempDir.resolve("map");
        Files.createDirectories(mapDirectory);
        Files.write(mapDirectory.resolve("levels.yaml"), ("levels:\n" +
                "  - id: 1\n" +
                "    name: Missing One\n" +
                "    map: map/levels/1/level.tmx\n" +
                "  - id: 2\n" +
                "    name: Missing Two\n" +
                "    map: map/levels/2/level.tmx\n" +
                "  - id: 3\n" +
                "    name: Missing Three\n" +
                "    map: map/levels/3/level.tmx\n").getBytes(StandardCharsets.UTF_8));

        LevelAssetResolver resolver = LevelAssetResolvers.filesystem(tempDir);
        LevelCatalog catalog = LevelCatalog.load(resolver);
        LevelValidationResult result = new LevelAssetValidator(resolver).validate(catalog, false);

        assertFalse(result.isValid());
        assertTrue(result.toUserMessage().contains("Avvio bloccato"));
        assertTrue(result.toUserMessage().contains("map/levels/1/level.tmx"));
        assertTrue(result.toUserMessage().contains("map/levels/2/level.tmx"));
        assertTrue(result.toUserMessage().contains("map/levels/3/level.tmx"));
    }

    @Test
    public void proceduralGeneratorHasBeenRemoved() {
        assertFalse(Files.exists(projectPath("map/kenney_isometric-miniature-farm/generate_map.py")),
                "TMX maps must be authored in Tiled, not generated by local scripts.");
        assertFalse(Files.exists(projectPath("map/generated/level_01_countryside_crash.tmx")),
                "Legacy generated TMX should not be part of the runtime map pipeline.");
        assertFalse(Files.exists(projectPath("map/kenney_isometric-miniature-farm/countryside_level.tmx")),
                "Sample/generated TMX files should not be part of the runtime map pipeline.");
    }

    private static LevelCatalog loadProjectCatalog() throws Exception {
        return LevelCatalog.load(LevelAssetResolvers.filesystem(assetsRoot()));
    }

    private static void createMinimalValidLevel(Path tempDir) throws Exception {
        Path mapDirectory = tempDir.resolve("map");
        Path levelDirectory = mapDirectory.resolve("levels/1");
        Files.createDirectories(levelDirectory);
        Files.write(mapDirectory.resolve("levels.yaml"), ("levels:\n" +
                "  - id: 1\n" +
                "    name: Dev One\n" +
                "    map: map/levels/1/level.tmx\n").getBytes(StandardCharsets.UTF_8));
        Files.write(levelDirectory.resolve("level.tmx"), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<map orientation=\"isometric\" width=\"1\" height=\"1\" tilewidth=\"256\" tileheight=\"128\">\n" +
                "  <layer name=\"terreno\" width=\"1\" height=\"1\"><data encoding=\"csv\">0</data></layer>\n" +
                "  <objectgroup name=\"Ostacoli\"/>\n" +
                "  <objectgroup name=\"Spawn\"><object name=\"Spawn\" x=\"0\" y=\"0\"><point/></object></objectgroup>\n"
                +
                "</map>\n").getBytes(StandardCharsets.UTF_8));
    }

    private static Document loadProjectMapDocument(String internalPath) throws Exception {
        Path path = assetsRoot().resolve(internalPath);
        assertTrue(Files.exists(path), "Missing map file: " + internalPath);

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
        return assetsRoot().resolve(path);
    }

    private static Path assetsRoot() {
        Path candidate = Paths.get("assets");
        if (Files.exists(candidate)) {
            return candidate;
        }
        return Paths.get("..").resolve("assets");
    }
}
