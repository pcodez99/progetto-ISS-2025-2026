package io.github.iss_2025_2026.model;

public class GroupHeal implements SpecialAbility {
    @Override
    public String getName() {
        return "Group Heal";
    }

    @Override
    public String getDescription() {
        return "Heals all players simultaneously";
    }

    @Override
    public void perform(Character user, Character target, int userLevel) {
        // Implementation pending combat system discussion
    }
}
