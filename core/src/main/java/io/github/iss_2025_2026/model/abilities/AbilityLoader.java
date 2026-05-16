package io.github.iss_2025_2026.model.abilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.io.IOException;

public class AbilityLoader {

    private final ObjectMapper mapper;

    public AbilityLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AbilityConfiguration loadAbility(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, AbilityConfiguration.class);
        } catch (IOException exception) {
            System.err.println("Failed to load YAML from stream");
            exception.printStackTrace();
            return null;
        }
    }
}

