package io.github.iss_2025_2026.factory;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.model.SpecialAbility;
import io.github.iss_2025_2026.model.abilities.AbilityConfiguration;
import io.github.iss_2025_2026.model.abilities.AbilityLoader;
import io.github.iss_2025_2026.model.abilities.DataDrivenAbility;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class AbilityRegistry {
    private static final String TAG = "AbilityRegistry";
    private static final String[] DEFAULT_ABILITIES = {
            "group_heal",
            "mamma_purifying_care",
            "mamma_slipper_stun",
            "viddano_throw",
            "papa_guardian_throw",
            "papa_heavy_hit",
            "stone_rain",
            "bambino_distraction_shot",
            "bambino_critical_barrage",
            "wheelchair_jump",
            "nonno_guardian_stance",
            "nonno_ramming_charge"
    };

    private final Map<String, SpecialAbility> abilities;

    private AbilityRegistry(Map<String, SpecialAbility> abilities) {
        this.abilities = Collections.unmodifiableMap(new HashMap<>(abilities));
    }

    static AbilityRegistry loadDefault() {
        Map<String, SpecialAbility> loadedAbilities = new HashMap<>();
        AbilityLoader loader = new AbilityLoader();

        for (String abilityFileName : DEFAULT_ABILITIES) {
            String path = "configs/abilities/" + abilityFileName + ".yaml";
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error(TAG, "Ability configuration file MISSING: " + path);
                continue;
            }

            try (InputStream inputStream = Gdx.files.internal(path).read()) {
                AbilityConfiguration config = loader.loadAbility(inputStream);
                if (config != null) {
                    loadedAbilities.put(config.getId(), new DataDrivenAbility(config));
                } else {
                    Gdx.app.error(TAG, "Ability configuration is EMPTY or INVALID: " + path);
                }
            } catch (Exception exception) {
                Gdx.app.error(TAG, "CRITICAL: Error reading ability file: " + abilityFileName, exception);
            }
        }

        return new AbilityRegistry(loadedAbilities);
    }

    SpecialAbility get(String abilityId) {
        return abilities.get(abilityId);
    }
}
