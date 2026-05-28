package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Character;

/**
 * Factory Method contract for a single Character subtype.
 */
public interface CharacterTypeFactory<T extends Character> {
    T create(String id);
}
