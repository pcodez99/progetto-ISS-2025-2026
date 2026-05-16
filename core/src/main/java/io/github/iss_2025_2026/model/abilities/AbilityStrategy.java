package io.github.iss_2025_2026.model.abilities;

public interface AbilityStrategy {
    void execute(AbilityContext context, AbilityConfiguration config);
}
