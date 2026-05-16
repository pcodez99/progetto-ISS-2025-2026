package io.github.iss_2025_2026.model.abilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

/**
 * "ponte" tra dati esterni e codice, utilizza la libreria Jackson per leggere i file .yaml
 * e trasformarli in oggetti Java; utilizza il "Data-Driven" per modificare i valori
 * senza dover ricompilare il codice
 */

public class AbilityLoader {

    private final ObjectMapper mapper;

    public AbilityLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public AbilityConfiguration loadAbility(String filePath) {
        try {
            return mapper.readValue(new File(filePath), AbilityConfiguration.class);
        } catch (IOException exception) {
            System.err.println("Failed to load YAML file: " + filePath);
            exception.printStackTrace();
            return null;
        }
    }
}
