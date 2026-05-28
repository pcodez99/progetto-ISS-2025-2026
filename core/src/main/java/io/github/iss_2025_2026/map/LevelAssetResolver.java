package io.github.iss_2025_2026.map;

import java.io.IOException;
import java.io.InputStream;

public interface LevelAssetResolver {
    boolean exists(String internalPath);

    InputStream open(String internalPath) throws IOException;
}
