package io.github.iss_2025_2026.factory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.Npc;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NpcFactory {
    private static final Logger LOGGER = Logger.getLogger(NpcFactory.class.getName());
    private static final String DEFAULT_PATH = "configs/npcs.yaml";
    private static final String ALL_LEVELS = "all_levels";

    private final Map<String, Npc> npcCatalog;
    private final ObjectMapper mapper;

    public NpcFactory() {
        this(DEFAULT_PATH);
    }

    NpcFactory(String path) {
        this.npcCatalog = new LinkedHashMap<>();
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadNpcConfigs(path);
    }

    private void loadNpcConfigs(String path) {
        try (InputStream inputStream = NpcFactory.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOGGER.severe("CRITICAL ERROR: File " + path + " non trovato nel classpath.");
                return;
            }

            NpcCatalog catalog = mapper.readValue(inputStream, NpcCatalog.class);
            List<Npc> npcs = catalog.getNpcs();
            for (Npc npc : npcs) {
                if (isValidNpc(npc)) {
                    npcCatalog.put(npc.getId(), npc.copy());
                }
            }
            LOGGER.info(npcCatalog.size() + " NPC caricati con successo dallo YAML.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il parsing del file YAML: " + path, exception);
        }
    }

    public Npc getNpc(String id) {
        Optional<Npc> npc = findNpc(id);
        if (!npc.isPresent()) {
            LOGGER.warning("Attenzione: richiesto NPC inesistente con ID: " + id);
            return null;
        }
        return npc.get();
    }

    public Optional<Npc> findNpc(String id) {
        Npc prototype = npcCatalog.get(id);
        if (prototype == null) {
            return Optional.empty();
        }
        return Optional.of(prototype.copy());
    }

    public Npc requireNpc(String id) {
        Optional<Npc> npc = findNpc(id);
        if (!npc.isPresent()) {
            throw new IllegalArgumentException("NPC non trovato: " + id);
        }
        return npc.get();
    }

    public List<Npc> getAllNpcs() {
        List<Npc> npcs = new ArrayList<>();
        for (Npc npc : npcCatalog.values()) {
            npcs.add(npc.copy());
        }
        return npcs;
    }

    public List<Npc> getNpcsForLevel(String levelId) {
        List<Npc> npcs = new ArrayList<>();
        for (Npc npc : npcCatalog.values()) {
            if (matchesLevel(npc, levelId)) {
                npcs.add(npc.copy());
            }
        }
        return npcs;
    }

    private boolean matchesLevel(Npc npc, String levelId) {
        if (npc == null || npc.getLevelId() == null) {
            return false;
        }
        return npc.getLevelId().equals(levelId) || ALL_LEVELS.equals(npc.getLevelId());
    }

    private boolean isValidNpc(Npc npc) {
        if (npc == null) {
            LOGGER.warning("NPC ignorato: voce YAML nulla.");
            return false;
        }
        if (isBlank(npc.getId())) {
            LOGGER.warning("NPC ignorato: id mancante.");
            return false;
        }
        if (isBlank(npc.getName())) {
            LOGGER.warning("NPC ignorato: nome mancante per ID " + npc.getId());
            return false;
        }
        if (isBlank(npc.getLevelId())) {
            LOGGER.warning("NPC ignorato: livello mancante per ID " + npc.getId());
            return false;
        }
        if (npc.getType() == null) {
            LOGGER.warning("NPC ignorato: tipo mancante per ID " + npc.getId());
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class NpcCatalog {
        private List<Npc> npcs = new ArrayList<>();

        private List<Npc> getNpcs() {
            return npcs != null ? npcs : Collections.<Npc>emptyList();
        }

        @SuppressWarnings("unused")
        public void setNpcs(List<Npc> npcs) {
            this.npcs = npcs;
        }
    }
}
