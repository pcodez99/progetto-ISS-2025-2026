package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test TDD per EnemyEncounterService.
 * Questi test verificano il rilevamento della collisione tra player e oggetti nemici
 * letti dal layer TMX (senza richiedere LibGDX, usando stub leggeri).
 */
public class EnemyEncounterServiceTest {

    /**
     * Stub leggero di un oggetto TMX che non richiede LibGDX.
     * In produzione sarà un com.badlogic.gdx.maps.MapObject.
     */
    static class FakeEnemyObject {
        public final float x;
        public final float y;
        public final String enemyType;
        public final int enemiesNumber;
        public boolean removed;

        FakeEnemyObject(float x, float y, String enemyType, int enemiesNumber) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
            this.enemiesNumber = enemiesNumber;
            this.removed = false;
        }
    }

    private Player player;
    private static final float ENCOUNTER_RADIUS = 80f;

    @BeforeEach
    public void setUp() {
        player = new Player("Viddano", 100, 10, null);
    }

    // -------------------------------------------------------------------------
    // Rilevamento collisione
    // -------------------------------------------------------------------------

    @Test
    public void testPlayerOnEnemyCoordinatesTriggersBattle() {
        // Posiziona il player esattamente sulle coordinate del nemico
        player.setX(500f);
        player.setY(500f);

        EnemyEncounterService service = EnemyEncounterService.forTesting(
            500f, 500f, "alieno_base", 2, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);

        assertNotNull(encounter, "Deve rilevare l'incontro quando il player è sopra il nemico");
        assertFalse(encounter.getEnemies().isEmpty(), "L'incontro deve avere nemici");
    }

    @Test
    public void testPlayerNotOnEnemyCoordinatesNoBattle() {
        // Posiziona il player lontano dal nemico
        player.setX(0f);
        player.setY(0f);

        EnemyEncounterService service = EnemyEncounterService.forTesting(
            1000f, 1000f, "alieno_base", 2, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);

        assertNull(encounter, "Non deve rilevare incontri quando il player è lontano");
    }

    @Test
    public void testEncounterThresholdRespected() {
        // Posiziona il player appena dentro il raggio
        player.setX(500f);
        player.setY(500f + ENCOUNTER_RADIUS - 1f);

        EnemyEncounterService service = EnemyEncounterService.forTesting(
            500f, 500f, "alieno_base", 1, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);

        assertNotNull(encounter, "Deve rilevare l'incontro quando il player è dentro il raggio");
    }

    @Test
    public void testPlayerJustOutsideThresholdNoBattle() {
        // Posiziona il player appena fuori dal raggio
        player.setX(500f);
        player.setY(500f + ENCOUNTER_RADIUS + 1f);

        EnemyEncounterService service = EnemyEncounterService.forTesting(
            500f, 500f, "alieno_base", 1, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);

        assertNull(encounter, "Non deve rilevare l'incontro quando il player è fuori dal raggio");
    }

    // -------------------------------------------------------------------------
    // Numero di nemici
    // -------------------------------------------------------------------------

    @Test
    public void testEnemiesNumberCreatesCorrectNumberOfEnemies() {
        player.setX(500f);
        player.setY(500f);

        int expectedCount = 3;
        EnemyEncounterService service = EnemyEncounterService.forTesting(
            500f, 500f, "alieno_base", expectedCount, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);

        assertNotNull(encounter);
        assertEquals(expectedCount, encounter.getEnemies().size(),
            "Il numero di nemici deve corrispondere alla proprietà enemies_number del TMX");
    }

    // -------------------------------------------------------------------------
    // Rimozione dopo battaglia
    // -------------------------------------------------------------------------

    @Test
    public void testEncounterObjectRemovedAfterBattle() {
        player.setX(500f);
        player.setY(500f);

        EnemyEncounterService service = EnemyEncounterService.forTesting(
            500f, 500f, "alieno_base", 1, ENCOUNTER_RADIUS
        );

        EnemyEncounter encounter = service.checkEncounter(player);
        assertNotNull(encounter);

        service.removeEncounter(encounter);

        // Dopo la rimozione, lo stesso player non deve più triggerare la battaglia
        EnemyEncounter secondEncounter = service.checkEncounter(player);
        assertNull(secondEncounter, "Dopo la rimozione, non deve più esserci un incontro");
    }
}
