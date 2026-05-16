package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.CharacterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterSelectionModel {
    private final List<Map<String, Object>> characters;
    private int selectedIndex;

    public CharacterSelectionModel(CharacterFactory factory) {
        this.characters = new ArrayList<>(factory.getCharacterData().values());
        this.selectedIndex = 0;
    }

    public List<Map<String, Object>> getCharacters() {
        return characters;
    }

    public Map<String, Object> getSelectedCharacter() {
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

    public void nextCharacter() {
        selectedIndex = (selectedIndex + 1) % characters.size();
    }

    public void previousCharacter() {
        selectedIndex = (selectedIndex - 1 + characters.size()) % characters.size();
    }
}
