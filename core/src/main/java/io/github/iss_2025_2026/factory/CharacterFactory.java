package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating playable characters from YAML configuration
 */
public class CharacterFactory {
    private Map<String, Map<String, Object>> characterData;
    private final Map<String, SpecialAbility> abilityRegistry;

    public CharacterFactory() {
        this.abilityRegistry = new HashMap<>();
        registerAbilities();
        loadConfigs();
    }

    private void registerAbilities() {
        abilityRegistry.put("GROUP_HEAL", new GroupHeal());
        abilityRegistry.put("VIDDANO_THROW", new ViddanoThrow());
        abilityRegistry.put("STONE_RAIN", new StoneRain());
        abilityRegistry.put("WHEELCHAIR_JUMP", new WheelchairJump());
    }

    private void loadConfigs() {
        Yaml yaml = new Yaml();
        try (InputStream is = Gdx.files.internal("configs/characters.yaml").read()) {
            Map<String, List<Map<String, Object>>> data = yaml.load(is);
            characterData = new HashMap<>();
            List<Map<String, Object>> characters = data.get("characters");
            if (characters != null) {
                for (Map<String, Object> charMap : characters) {
                    characterData.put((String) charMap.get("id"), charMap);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "Error loading characters.yaml", e);
            characterData = new HashMap<>();
        }
    }

    /**
     * Creates a new Player instance based on the provided character ID
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

        return new Player(name, maxHp, baseDamage, ability, maxHpGrowth, damageGrowth);
    }
}
