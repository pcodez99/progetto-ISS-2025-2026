package io.github.iss_2025_2026.model;

public class StoneRain implements SpecialAbility {
    @Override
    public String getName() {
        return "Stone Rain";
    }

    @Override
    public String getDescription() {
        return "A rain of stones falls on the target";
    }

    @Override
    public void perform(Character user, Character target, int userLevel) {
        // Implementation pending combat system discussion
    }
}
