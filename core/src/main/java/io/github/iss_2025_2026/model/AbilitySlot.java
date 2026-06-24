package io.github.iss_2025_2026.model;

public enum AbilitySlot {
    BASE,
    ALTRUISTIC,
    EGOISTIC;

    public static AbilitySlot fromKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return BASE;
        }
        String normalized = key.trim().toUpperCase();
        if ("BASE".equals(normalized)) {
            return BASE;
        }
        if ("ALTRUISTIC".equals(normalized) || "ALTRUISTA".equals(normalized)) {
            return ALTRUISTIC;
        }
        if ("EGOISTIC".equals(normalized) || "EGOISTA".equals(normalized)) {
            return EGOISTIC;
        }
        throw new IllegalArgumentException("Slot abilita non valido: " + key);
    }
}
