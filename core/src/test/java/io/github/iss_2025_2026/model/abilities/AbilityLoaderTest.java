package io.github.iss_2025_2026.model.abilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class AbilityLoaderTest {

    @Test
    public void testLoadAbilityFromYaml(@TempDir Path tempDir) throws Exception {
        // 1. ARRANGE (Preparazione)
        // Creiamo il contenuto di un finto file YAML sotto forma di stringa
        String yamlContent =
            "id: TEST_HEAL\n" +
                "name: Magic Heal\n" +
                "description: Restores the whole party.\n" +
                "strategy: HEAL\n" +
                "baseHealing: 20\n";

        // Creiamo il file fisico nella cartella temporanea di JUnit
        Path tempFile = tempDir.resolve("test_ability.yaml");
        Files.write(tempFile, yamlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Prepariamo il nostro loader
        AbilityLoader loader = new AbilityLoader();

        // 2. ACT (Esecuzione)
        // Facciamo leggere il file appena creato al nostro loader tramite un InputStream
        AbilityConfiguration config;
        try (java.io.InputStream is = Files.newInputStream(tempFile)) {
            config = loader.loadAbility(is);
        }


        // 3. ASSERT (Verifica)
        // Se Jackson ha fatto bene il suo lavoro, il config non sarà nullo
        // e conterrà esattamente i dati che abbiamo scritto nella stringa
        assertNotNull(config, "The configuration should not be null!");

        assertEquals("TEST_HEAL", config.getId(), "ID does not match");
        assertEquals("Magic Heal", config.getName(), "Name does not match");
        assertEquals("Restores the whole party.", config.getDescription(), "Description does not match");
        assertEquals("HEAL", config.getStrategy(), "Strategy does not match");
        assertEquals(20, config.getBaseHealing(), "Base healing does not match");
    }
}
