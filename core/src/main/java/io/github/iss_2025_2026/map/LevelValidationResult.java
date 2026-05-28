package io.github.iss_2025_2026.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelValidationResult {
    private final List<String> errors;

    public LevelValidationResult(List<String> errors) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public String toUserMessage() {
        if (isValid()) {
            return "Tutte le mappe e gli asset dei livelli sono validi.";
        }

        StringBuilder builder = new StringBuilder("Avvio bloccato: mappe o asset runtime non validi.");
        for (String error : errors) {
            builder.append(System.lineSeparator()).append("- ").append(error);
        }
        return builder.toString();
    }
}
