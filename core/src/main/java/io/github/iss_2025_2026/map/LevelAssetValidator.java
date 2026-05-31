package io.github.iss_2025_2026.map;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LevelAssetValidator {
    private final LevelAssetResolver resolver;

    public LevelAssetValidator(LevelAssetResolver resolver) {
        this.resolver = resolver;
    }

    public LevelValidationResult validate(LevelCatalog catalog) {
        return validate(catalog, false);
    }

    public LevelValidationResult validate(LevelCatalog catalog, boolean devMode) {
        List<String> errors = new ArrayList<>();
        validateExpectedLevels(catalog, devMode, errors);

        for (LevelDefinition level : catalog.getLevels()) {
            validateLevel(level, errors);
        }

        return new LevelValidationResult(errors);
    }

    private void validateExpectedLevels(LevelCatalog catalog, boolean devMode, List<String> errors) {
        if (devMode) {
            if (!catalog.hasLevel(TmxMapContract.DEFAULT_LEVEL_ID)) {
                errors.add("Manifest livelli incompleto: in dev_mode manca il livello "
                        + TmxMapContract.DEFAULT_LEVEL_ID + ".");
            }
            return;
        }

        for (int levelId = 1; levelId <= TmxMapContract.EXPECTED_LEVEL_COUNT; levelId++) {
            if (!catalog.hasLevel(levelId)) {
                errors.add("Manifest livelli incompleto: manca il livello " + levelId + ".");
            }
        }
    }

    private void validateLevel(LevelDefinition level, List<String> errors) {
        String mapPath = level.getMapPath();
        String label = "Livello " + level.getId() + " (" + level.getName() + ")";

        if (mapPath == null || mapPath.trim().isEmpty()) {
            errors.add(label + ": percorso TMX mancante nel manifest.");
            return;
        }

        if (!resolver.exists(mapPath)) {
            errors.add(label + ": mappa mancante: " + mapPath);
            return;
        }

        Document document = parseXml(mapPath, errors, label);
        if (document == null) {
            return;
        }

        validateTmxContract(document, label, errors);
        validateCheckpoints(document, level, label, errors);
        validateExternalTilesets(document, mapPath, label, errors);
        validateImages(document, mapPath, label, errors);
    }

    private void validateTmxContract(Document document, String label, List<String> errors) {
        Element map = document.getDocumentElement();
        if (!"map".equals(map.getTagName())) {
            errors.add(label + ": il file TMX non ha un elemento root <map>.");
            return;
        }
        if (!"isometric".equals(map.getAttribute("orientation"))) {
            errors.add(label + ": la mappa deve avere orientation=\"isometric\".");
        }
        if (!hasLayer(document, "terreno")) {
            errors.add(label + ": layer tile obbligatorio mancante: terreno.");
        }
        if (!hasObjectGroup(document, TmxMapContract.LAYER_OBSTACLES)) {
            errors.add(label + ": object layer obbligatorio mancante: " + TmxMapContract.LAYER_OBSTACLES + ".");
        }
        if (!hasVisibleSpawn(document)) {
            errors.add(label + ": object layer '" + TmxMapContract.LAYER_SPAWN
                    + "' deve contenere un oggetto visibile chiamato '" + TmxMapContract.SPAWN_OBJECT_NAME + "'.");
        }
    }

    private void validateExternalTilesets(Document document, String ownerPath, String label, List<String> errors) {
        NodeList tilesets = document.getElementsByTagName("tileset");
        for (int i = 0; i < tilesets.getLength(); i++) {
            Element tileset = (Element) tilesets.item(i);
            String source = tileset.getAttribute("source");
            if (source == null || source.trim().isEmpty()) {
                continue;
            }

            String tilesetPath = resolveRelative(ownerPath, source);
            if (!resolver.exists(tilesetPath)) {
                errors.add(label + ": tileset esterno mancante: " + tilesetPath);
                continue;
            }

            Document tilesetDocument = parseXml(tilesetPath, errors, label);
            if (tilesetDocument != null) {
                validateImages(tilesetDocument, tilesetPath, label, errors);
            }
        }
    }

    private void validateImages(Document document, String ownerPath, String label, List<String> errors) {
        NodeList images = document.getElementsByTagName("image");
        Set<String> checkedPaths = new HashSet<>();
        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);
            String source = image.getAttribute("source");
            if (source == null || source.trim().isEmpty()) {
                continue;
            }

            String imagePath = resolveRelative(ownerPath, source);
            if (checkedPaths.add(imagePath) && !resolver.exists(imagePath)) {
                errors.add(label + ": asset mancante: " + imagePath + " (referenziato da " + ownerPath + ")");
            }
        }
    }

    private Document parseXml(String path, List<String> errors, String label) {
        try (InputStream inputStream = resolver.open(path)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception exception) {
            errors.add(label + ": impossibile leggere XML " + path + ": " + exception.getMessage());
            return null;
        }
    }

    private boolean hasLayer(Document document, String layerName) {
        NodeList layers = document.getElementsByTagName("layer");
        for (int i = 0; i < layers.getLength(); i++) {
            Element layer = (Element) layers.item(i);
            if (layerName.equals(layer.getAttribute("name"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasObjectGroup(Document document, String groupName) {
        NodeList groups = document.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (groupName.equals(group.getAttribute("name"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisibleSpawn(Document document) {
        NodeList groups = document.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (!TmxMapContract.LAYER_SPAWN.equals(group.getAttribute("name"))) {
                continue;
            }
            NodeList objects = group.getElementsByTagName("object");
            for (int j = 0; j < objects.getLength(); j++) {
                Element object = (Element) objects.item(j);
                String visible = object.getAttribute("visible");
                if (!"0".equals(visible) && TmxMapContract.isPlayerSpawnName(object.getAttribute("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateCheckpoints(Document document, LevelDefinition level, String label, List<String> errors) {
        Set<String> checkpointIds = new HashSet<>();
        for (CheckpointDefinition checkpoint : level.getCheckpoints()) {
            if (checkpoint == null) {
                errors.add(label + ": checkpoint nullo nel manifest.");
                continue;
            }
            if (checkpoint.getId() == null || checkpoint.getId().trim().isEmpty()) {
                errors.add(label + ": checkpoint senza id nel manifest.");
                continue;
            }
            if (!checkpointIds.add(checkpoint.getId())) {
                errors.add(label + ": checkpoint duplicato nel manifest: " + checkpoint.getId() + ".");
            }
            if (!hasVisibleCheckpointLine(document, checkpoint.getLayer(), checkpoint.getObjectName())) {
                errors.add(label + ": checkpoint '" + checkpoint.getId() + "' non trovato come linea checkpoint "
                        + "nel layer TMX '" + checkpoint.getLayer() + "'.");
            }
        }
    }

    private boolean hasVisibleCheckpointLine(Document document, String groupName, String objectName) {
        boolean foundAnyVisibleLine = false;
        NodeList groups = document.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (!groupName.equals(group.getAttribute("name"))) {
                continue;
            }
            NodeList objects = group.getElementsByTagName("object");
            for (int j = 0; j < objects.getLength(); j++) {
                Element object = (Element) objects.item(j);
                String visible = object.getAttribute("visible");
                if ("0".equals(visible) || !hasPolyline(object)) {
                    continue;
                }
                foundAnyVisibleLine = true;
                if (objectName != null && objectName.equals(object.getAttribute("name"))) {
                    return true;
                }
            }
        }
        return foundAnyVisibleLine;
    }

    private boolean hasPolyline(Element object) {
        return object.getElementsByTagName("polyline").getLength() > 0;
    }

    private String resolveRelative(String ownerPath, String relativePath) {
        if (relativePath.startsWith("/")) {
            return relativePath.substring(1);
        }

        String ownerDirectory = "";
        int slashIndex = ownerPath.lastIndexOf('/');
        if (slashIndex >= 0) {
            ownerDirectory = ownerPath.substring(0, slashIndex);
        }

        return Paths.get(ownerDirectory)
                .resolve(relativePath)
                .normalize()
                .toString()
                .replace(File.separatorChar, '/');
    }
}
