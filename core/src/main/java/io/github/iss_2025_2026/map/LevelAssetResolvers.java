package io.github.iss_2025_2026.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LevelAssetResolvers {
    private LevelAssetResolvers() {
    }

    public static LevelAssetResolver gdx() {
        return new LevelAssetResolver() {
            @Override
            public boolean exists(String internalPath) {
                return Gdx.files.internal(internalPath).exists();
            }

            @Override
            public InputStream open(String internalPath) throws IOException {
                FileHandle file = Gdx.files.internal(internalPath);
                if (!file.exists()) {
                    throw new IOException("Asset non trovato: " + internalPath);
                }
                return file.read();
            }
        };
    }

    public static LevelAssetResolver filesystem(final Path assetsRoot) {
        return new LevelAssetResolver() {
            @Override
            public boolean exists(String internalPath) {
                return Files.exists(assetsRoot.resolve(internalPath));
            }

            @Override
            public InputStream open(String internalPath) throws IOException {
                return new FileInputStream(assetsRoot.resolve(internalPath).toFile());
            }
        };
    }
}
