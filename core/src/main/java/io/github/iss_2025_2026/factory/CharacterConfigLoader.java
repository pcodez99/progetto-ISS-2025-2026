package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CharacterConfigLoader {
    private static final String TAG = "CharacterConfigLoader";

    private final ObjectMapper mapper;

    CharacterConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> loadIndexedList(String path, String listKey) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error(TAG, "CRITICAL: " + path + " NOT FOUND");
            return Collections.emptyMap();
        }

        try (InputStream inputStream = Gdx.files.internal(path).read()) {
            Map<String, Object> data = mapper.readValue(inputStream, Map.class);
            List<Map<String, Object>> entries = (List<Map<String, Object>>) data.get(listKey);
            if (entries == null) {
                Gdx.app.error(TAG, path + " is empty or missing '" + listKey + "' list!");
                return Collections.emptyMap();
            }

            Map<String, Map<String, Object>> indexedEntries = new HashMap<>();
            for (Map<String, Object> entry : entries) {
                indexedEntries.put((String) entry.get("id"), entry);
            }
            return Collections.unmodifiableMap(indexedEntries);
        } catch (Exception exception) {
            Gdx.app.error(TAG, "CRITICAL: Error parsing " + path, exception);
            return Collections.emptyMap();
        }
    }
}
