package io.github.iss_2025_2026.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.github.iss_2025_2026.service.GameProperties;

public final class GameStartupValidator {
    private GameStartupValidator() {
    }

    public static LevelValidationResult validateRuntimeAssets() {
        LevelAssetResolver resolver = LevelAssetResolvers.gdx();
        try {
            LevelCatalog catalog = LevelCatalog.load(resolver);
            boolean devMode = GameProperties.getBoolean(GameProperties.KEY_DEV_MODE, true);
            return new LevelAssetValidator(resolver).validate(catalog, devMode);
        } catch (IOException exception) {
            List<String> errors = new ArrayList<>();
            errors.add("Manifest livelli non leggibile: " + TmxMapContract.LEVELS_MANIFEST_PATH
                    + " (" + exception.getMessage() + ")");
            return new LevelValidationResult(errors);
        }
    }
}
