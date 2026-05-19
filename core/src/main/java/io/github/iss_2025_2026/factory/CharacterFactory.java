package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.iss_2025_2026.model.*;
import io.github.iss_2025_2026.model.abilities.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating playable characters from YAML configuration using
 * Jackson.
 */
public class CharacterFactory {
    private Map<String, Map<String, Object>> characterData;
    // NUOVO: Registro per contenere i dati grezzi dei nemici (alieni e boss)
    private Map<String, Map<String, Object>> enemyData;
    private final Map<String, SpecialAbility> abilityRegistry;
    private final ObjectMapper mapper;

    public CharacterFactory() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.abilityRegistry = new HashMap<>();
        this.enemyData = new HashMap<>(); // NUOVO
        registerAbilities();
        loadConfigs();
        loadEnemyConfigs(); // NUOVO: Carichiamo i nemici all'avvio
    }

    private void registerAbilities() {
        AbilityLoader loader = new AbilityLoader();
        //da aggiungere abilità dei boss-----------
        String[] abilities = { "group_heal", "viddano_throw", "stone_rain", "wheelchair_jump" };

        for (String abilityFileName : abilities) {
            String path = "configs/abilities/" + abilityFileName + ".yaml";
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error("CharacterFactory", "Ability configuration file MISSING: " + path);
                continue;
            }

            try (InputStream is = Gdx.files.internal(path).read()) {
                AbilityConfiguration config = loader.loadAbility(is);
                if (config != null) {
                    abilityRegistry.put(config.getId(), new DataDrivenAbility(config));
                } else {
                    Gdx.app.error("CharacterFactory", "Ability configuration is EMPTY or INVALID: " + path);
                }
            } catch (Exception e) {
                Gdx.app.error("CharacterFactory", "CRITICAL: Error reading ability file: " + abilityFileName, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadConfigs() {
        String path = "configs/characters.yaml";
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("CharacterFactory", "CRITICAL: characters.yaml NOT FOUND at " + path);
            characterData = new HashMap<>();
            return;
        }

        try (InputStream is = Gdx.files.internal(path).read()) {
            Map<String, Object> data = mapper.readValue(is, Map.class);
            characterData = new HashMap<>();
            List<Map<String, Object>> characters = (List<Map<String, Object>>) data.get("characters");
            if (characters != null) {
                for (Map<String, Object> charMap : characters) {
                    characterData.put((String) charMap.get("id"), charMap);
                }
            } else {
                Gdx.app.error("CharacterFactory", "characters.yaml is empty or missing 'characters' list!");
            }
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "CRITICAL: Error parsing characters.yaml", e);
            characterData = new HashMap<>();
        }
    }

    /**
     * NUOVO METODO: Legge il file enemies.yaml dal disco
     */
    @SuppressWarnings("unchecked")
    private void loadEnemyConfigs() {
        String path = "configs/characters.yaml";
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("CharacterFactory", "CRITICAL: enemies.yaml NOT FOUND at " + path);
            enemyData = new HashMap<>();
            return;
        }

        try (InputStream is = Gdx.files.internal(path).read()) {
            Map<String, Object> data = mapper.readValue(is, Map.class);
            enemyData = new HashMap<>();
            List<Map<String, Object>> enemies = (List<Map<String, Object>>) data.get("enemies");
            if (enemies != null) {
                for (Map<String, Object> enemyMap : enemies) {
                    enemyData.put((String) enemyMap.get("id"), enemyMap);
                }
                Gdx.app.log("CharacterFactory", "Nemici caricati con successo dallo YAML!");
            } else {
                Gdx.app.error("CharacterFactory", "enemies.yaml is empty or missing 'enemies' list!");
            }
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "CRITICAL: Error parsing enemies.yaml", e);
            enemyData = new HashMap<>();
        }
    }

    /**
     * Creates a new Player instance based on the provided character ID
     *
     * @param id The character identifier (e.g., "nonno", "papa")
     * @return A configured Player instance
     */
    public Player createPlayer(String id) {
        Map<String, Object> data = characterData.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Character ID not found in configuration: " + id);
        }

        String name = (String) data.getOrDefault("name", "Unknown");
        int maxHp = (int) data.getOrDefault("maxHp", 100);
        int baseDamage = (int) data.getOrDefault("baseDamage", 10);
        int maxHpGrowth = (int) data.getOrDefault("maxHpGrowth", 10);
        int damageGrowth = (int) data.getOrDefault("damageGrowth", 2);
        String abilityId = (String) data.get("abilityId");

        SpecialAbility ability = abilityRegistry.get(abilityId);
        if (ability == null && abilityId != null) {
            Gdx.app.error("CharacterFactory", "WARNING: Ability '" + abilityId + "' not found for character '" + id
                    + "'. Player will have NO special ability!");
        }

        Player player = new Player(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth, 1);
        player.setCharacterId(id);
        return player;
    }
    /**
     * NUOVO METODO: Crea un'istanza di Enemy (sia Alieno normale che Boss) partendo dall'ID dello YAML
     * * @param id L'identificativo del nemico (es. "alieno_base", "boss_alieno")
     * @return Un'istanza configurata di Enemy
     */
    public Enemy createEnemy(String id) {
        Map<String, Object> data = enemyData.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Enemy ID not found in configuration: " + id);
        }

        String name = (String) data.getOrDefault("name", "Unknown Enemy");
        int maxHp = (int) data.getOrDefault("maxHp", 50);
        int baseDamage = (int) data.getOrDefault("baseDamage", 5);
        int xpReward = (int) data.getOrDefault("xpReward", 15);
        boolean isBoss = (boolean) data.getOrDefault("isBoss", false); // Capisce se è un boss o no
        String abilityId = (String) data.get("abilityId"); // Può essere nullo o "NONE" per gli alieni normali

        // Recuperiamo l'abilità dal registro centrale (la stessa mappa usata per i player!)
        SpecialAbility ability = null;
        if (abilityId != null && !abilityId.equalsIgnoreCase("NONE")) {
            ability = abilityRegistry.get(abilityId);
            if (ability == null) {
                Gdx.app.error("CharacterFactory", "WARNING: Ability '" + abilityId + "' not found for enemy '" + id + "'");
            }
        }

        // Se l'abilità era nulla, l'alieno normale non avrà abilità speciali e farà solo attacchi base.
        Enemy enemy = new Enemy(name, id, maxHp, baseDamage, xpReward, isBoss);
        enemy.setSpecialAbility(ability);

        return enemy;
    }

    public SpecialAbility getAbility(String abilityId) {
        return abilityRegistry.get(abilityId);
    }

    /**
     * @return All loaded character configurations
     */
    public Map<String, Map<String, Object>> getCharacterData() {
        return characterData;
    }
    // NUOVO GETTER
    public Map<String, Map<String, Object>> getEnemyData() {
        return enemyData;
    }
}
