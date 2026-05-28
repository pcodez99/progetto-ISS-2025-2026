package io.github.iss_2025_2026.model.collectibles;

import io.github.iss_2025_2026.model.Character;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectibleUseContext {
    private final Character user;
    private final List<Character> targets;

    public CollectibleUseContext(Character user, List<Character> targets) {
        this.user = user;
        this.targets = sanitizeTargets(targets);
    }

    public Character getUser() {
        return user;
    }

    public List<Character> getTargets(boolean aoe) {
        if (targets.isEmpty()) {
            return Collections.emptyList();
        }
        if (aoe) {
            return targets;
        }
        return Collections.singletonList(targets.get(0));
    }

    private List<Character> sanitizeTargets(List<Character> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Character> sanitizedTargets = new ArrayList<>();
        for (Character target : targets) {
            if (target != null) {
                sanitizedTargets.add(target);
            }
        }
        return Collections.unmodifiableList(sanitizedTargets);
    }
}
