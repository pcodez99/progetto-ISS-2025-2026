package io.github.iss_2025_2026.model;

public class ViddanoThrow implements SpecialAbility {
    @Override
    public String getName() {
        return "Viddano Throw";
    }

    @Override
    public String getDescription() {
        return "Throws a Viddano at the target";
    }

    @Override
    public void perform(Character user, Character target, int userLevel) {
        // Implementation pending combat system discussion
    }
}
