package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.collectibles.CollectibleEffectFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollectibleFactory {

    //Uso del Logger ufficiale di Java al posto dei System.out
    private static final Logger LOGGER = Logger.getLogger(CollectibleFactory.class.getName());

    private final Map<String, Collectible> collectibleCatalog;
    private final ObjectMapper mapper;

    public CollectibleFactory() {
        this.collectibleCatalog = new HashMap<>();
        this.mapper = new ObjectMapper(new YAMLFactory());

        /**
         * Evita che il programma crashi se
         * si aggiunge un campo nel file YAML ma si dimentica di aggiungerlo nella classe Java
         */
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        loadCollectibleConfigs();
    }

    private void loadCollectibleConfigs() {
        String path = "configs/collectibles.yaml";

        // TRY-WITH-RESOURCES: Apre il file e lo CHIUDE IN AUTOMATICO alla fine del blocco
        try (InputStream is = CollectibleFactory.class.getClassLoader().getResourceAsStream(path)) {

            if (is == null) {
                LOGGER.severe("CRITICAL ERROR: File " + path + " non trovato nel Classpath/Test Resources!");
                return;
            }

            // DESERIALIZZAZIONE DIRETTA: Jackson legge il file e crea direttamente gli oggetti!
            List<Collectible> dataList = mapper.readValue(is, new TypeReference<List<Collectible>>() {});

            if (dataList != null) {
                for (Collectible item : dataList) {
                    if (isValidCollectible(item)) {
                        collectibleCatalog.put(item.getId(), item.copy());
                    }
                }
                LOGGER.info(collectibleCatalog.size() + " oggetti caricati con successo dallo YAML!");
            }

        } catch (Exception e) {
            //Logger stampa l'errore di rosso insieme a tutta la scia di dettagli tecnici (Stacktrace)
            LOGGER.log(Level.SEVERE, "Errore durante il parsing del file YAML: " + path, e);
        }
    }

    public Collectible getCollectible(String id) {
        Optional<Collectible> collectible = findCollectible(id);
        if (!collectible.isPresent()) {
            LOGGER.warning("Attenzione: Richiesto oggetto inesistente con ID: " + id);
            return null;
        }

        return collectible.get();
    }

    public Optional<Collectible> findCollectible(String id) {
        Collectible prototype = collectibleCatalog.get(id);
        if (prototype == null) {
            return Optional.empty();
        }
        return Optional.of(prototype.copy());
    }

    public Collectible requireCollectible(String id) {
        Optional<Collectible> collectible = findCollectible(id);
        if (!collectible.isPresent()) {
            throw new IllegalArgumentException("Collectible non trovato: " + id);
        }
        return collectible.get();
    }

    public List<Collectible> getAllCollectibles() {
        List<Collectible> collectibles = new ArrayList<>();
        for (Collectible item : collectibleCatalog.values()) {
            collectibles.add(item.copy());
        }
        return collectibles;
    }

    private boolean isValidCollectible(Collectible item) {
        if (item == null) {
            LOGGER.warning("Collectible ignorato: voce YAML nulla.");
            return false;
        }
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            LOGGER.warning("Collectible ignorato: id mancante.");
            return false;
        }
        if (item.getEffectValue() < 0) {
            LOGGER.warning("Collectible ignorato: effectValue negativo per ID " + item.getId());
            return false;
        }

        try {
            CollectibleEffectFactory.getStrategy(item.getEffectType());
        } catch (IllegalArgumentException exception) {
            LOGGER.warning("Collectible ignorato: " + exception.getMessage());
            return false;
        }
        return true;
    }
}
