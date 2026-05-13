package io.github.iss_2025_2026.model;

public class WheelchairJump implements SpecialAbility {
    @Override
    public String getName() {
        return "Wheelchair Jump";
    }

    @Override
    public String getDescription() {
        return "A powerful jump with the wheelchair";
    }

    @Override
    public void perform(Character user, Character target, int userLevel) {
        // Implementation pending combat system discussion
    }
}
