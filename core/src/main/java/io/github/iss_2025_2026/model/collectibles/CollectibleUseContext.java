package io.github.iss_2025_2026.model.collectibles;

import io.github.iss_2025_2026.model.Characters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectibleUseContext {
    private final Characters user;
    private final List<Characters> targets;
    private final int currentLevelId;

    public CollectibleUseContext(Characters user, List<? extends Characters> targets) {
        this(user, targets, 1);
    }

    public CollectibleUseContext(Characters user, List<? extends Characters> targets, int currentLevelId) {
        this.user = user;
        this.targets = sanitizeTargets(targets);
        this.currentLevelId = currentLevelId;
    }

    public Characters getUser() {
        return user;
    }

    public int getCurrentLevelId() {
        return currentLevelId;
    }

    public List<Characters> getTargets(boolean aoe) {
        if (targets.isEmpty()) {
            return Collections.emptyList();
        }
        if (aoe) {
            return targets;
        }
        return Collections.singletonList(targets.get(0));
    }

    private List<Characters> sanitizeTargets(List<? extends Characters> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Characters> sanitizedTargets = new ArrayList<>();
        for (Characters target : targets) {
            if (target != null) {
                sanitizedTargets.add(target);
            }
        }
        return Collections.unmodifiableList(sanitizedTargets);
    }
}
