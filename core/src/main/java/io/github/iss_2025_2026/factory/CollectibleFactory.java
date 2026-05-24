package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.Collectible;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    collectibleCatalog.put(item.getId(), item);
                }
                LOGGER.info(collectibleCatalog.size() + " oggetti caricati con successo dallo YAML!");
            }

        } catch (Exception e) {
            //Logger stampa l'errore di rosso insieme a tutta la scia di dettagli tecnici (Stacktrace)
            LOGGER.log(Level.SEVERE, "Errore durante il parsing del file YAML: " + path, e);
        }
    }

    public Collectible getCollectible(String id) {
        Collectible prototype = collectibleCatalog.get(id);

        if (prototype == null) {
            LOGGER.warning("Attenzione: Richiesto oggetto inesistente con ID: " + id);
            return null;
        }

        // Restituisce una nuova istanza pulita clonando i dati del prototipo
        return new Collectible(
            prototype.getId(),
            prototype.getName(),
            prototype.getDescription(),
            prototype.getEffectType(),
            prototype.getAoe(),
            prototype.getEffectValue()
        );
    }

    public List<Collectible> getAllCollectibles() {
        return new ArrayList<>(collectibleCatalog.values());
    }
}
