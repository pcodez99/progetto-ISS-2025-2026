package io.github.iss_2025_2026.factory;

import io.github.iss_2025_2026.model.Character;

/** Builder comune dei personaggi di gioco. */
public interface CharacterBuilder {
    CharacterBuilder reset();

    CharacterBuilder id(String id);

    CharacterBuilder name(String name);

    CharacterBuilder maxHp(int maxHp);

    CharacterBuilder baseDamage(int baseDamage);

    Character build();
}
