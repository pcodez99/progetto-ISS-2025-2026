package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.PlayerFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CharacterSelectionModel {
    private final List<CharacterSelectionOption> characters;
    private int selectedIndex;

    public CharacterSelectionModel(PlayerFactory factory) {
        this.characters = new ArrayList<>();
        for (Map<String, Object> characterData : factory.getCharacterData().values()) {
            characters.add(toSelectionOption(characterData, factory));
        }
        characters.sort(Comparator.comparingInt(CharacterSelectionOption::getBaseDamage).reversed());
        this.selectedIndex = 0;
    }

    private CharacterSelectionOption toSelectionOption(Map<String, Object> characterData, PlayerFactory factory) {
        String abilityId = getString(characterData, "abilityId", "");
        SpecialAbility ability = factory.getAbility(abilityId);

        return new CharacterSelectionOption(
                getString(characterData, "id", ""),
                getString(characterData, "name", "Sconosciuto"),
                getString(characterData, "description", ""),
                getInt(characterData, "maxHp", 100),
                getInt(characterData, "baseDamage", 10),
                getInt(characterData, "maxHpGrowth", 10),
                getInt(characterData, "damageGrowth", 2),
                ability != null ? ability.getName() : "Abilita non disponibile",
                ability != null ? ability.getDescription() : "Questa abilita non e stata configurata.",
                getTraits(characterData));
    }

    private String getString(Map<String, Object> data, String key, String fallback) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private int getInt(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private List<String> getTraits(Map<String, Object> data) {
        Object rawTraits = data.get("traits");
        List<String> traits = new ArrayList<>();
        if (rawTraits instanceof List<?>) {
            for (Object trait : (List<?>) rawTraits) {
                if (trait != null) {
                    traits.add(String.valueOf(trait));
                }
            }
        }

        if (traits.isEmpty()) {
            traits.add("Nessuna caratteristica speciale");
        }

        return traits;
    }

    public List<CharacterSelectionOption> getCharacters() {
        return characters;
    }

    public CharacterSelectionOption getSelectedCharacter() {
        return characters.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex >= 0 && selectedIndex < characters.size()) {
            this.selectedIndex = selectedIndex;
        }
    }

    public void setSelectedCharacterById(String characterId) {
        if (characterId == null || characterId.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < characters.size(); i++) {
            if (characterId.equals(characters.get(i).getId())) {
                selectedIndex = i;
                return;
            }
        }
    }

    public void nextCharacter() {
        selectedIndex = (selectedIndex + 1) % characters.size();
    }

    public void previousCharacter() {
        selectedIndex = (selectedIndex - 1 + characters.size()) % characters.size();
    }
}
