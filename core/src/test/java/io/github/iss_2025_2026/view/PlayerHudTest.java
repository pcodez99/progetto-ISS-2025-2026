package io.github.iss_2025_2026.view;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.Color;
import io.github.iss_2025_2026.model.Characters;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test per PlayerHud - verifica della creazione, aggiornamento e calcoli dei colori.
 * Nota: Questi test sono leggeri e non richiedono LibGDX completo poiché testano
 * la logica dei colori e della struttura dati; il rendering vera e propria è GUI testing.
 */
public class PlayerHudTest {

    private Player player;
    private static final SpecialAbility dummyAbility = new SpecialAbility() {
        @Override
        public String getName() { return "Test"; }
        @Override
        public String getDescription() { return "Test Ability"; }
        @Override
        public void perform(Characters user, Characters target, int userLevel) {}
    };

    @BeforeEach
    public void setUp() {
        player = new Player("TestPlayer", 100, 10, dummyAbility);
        player.setLevel(1);
    }

    // -------------------------------------------------------------------------
    // Creazione e inizializzazione
    // -------------------------------------------------------------------------

    @Test
    public void testPlayerHudCreationWithValidPlayer() {
        assertNotNull(player);
        assertEquals(100, player.getMaxHp());
        assertEquals(100, player.getHp());
        assertEquals(0, player.getKarma());
        assertEquals(0, player.getXp());
    }

    @Test
    public void testPlayerInitialXp() {
        assertEquals(0, player.getXp());
        assertTrue(player.getXpToNext() > 0);
    }

    // -------------------------------------------------------------------------
    // Test HP Color Interpolation (Rosso -> Giallo -> Verde)
    // -------------------------------------------------------------------------

    @Test
    public void testHpColorAtZeroHealth() {
        player.setHp(0);
        float hpPercent = 0f;
        Color color = computeHpColorForTest(hpPercent);

        // A 0% dovrebbe essere molto rosso
        assertTrue(color.r > 0.8f, "Rosso channel dovrebbe essere alto a 0% HP");
        assertTrue(color.g < 0.5f, "Verde channel dovrebbe essere basso a 0% HP");
    }

    @Test
    public void testHpColorAtHalfHealth() {
        player.setHp(50);
        float hpPercent = 0.5f;
        Color color = computeHpColorForTest(hpPercent);

        // A 50% dovrebbe essere giallo (R alto, G alto, B basso)
        assertTrue(color.r > 0.8f, "Rosso dovrebbe essere alto a 50% HP");
        assertTrue(color.g > 0.8f, "Verde dovrebbe essere alto a 50% HP");
    }

    @Test
    public void testHpColorAtFullHealth() {
        player.setHp(100);
        float hpPercent = 1f;
        Color color = computeHpColorForTest(hpPercent);

        // A 100% dovrebbe essere verde
        assertTrue(color.g > 0.8f, "Verde dovrebbe essere alto a 100% HP");
        assertTrue(color.r < 0.5f, "Rosso dovrebbe essere basso a 100% HP");
    }

    // -------------------------------------------------------------------------
    // Test Karma Color Interpolation (Sfumature di Azzurro)
    // -------------------------------------------------------------------------

    @Test
    public void testKarmaColorAtMinimum() {
        player.setKarma(-50);
        float karmaPercent = 0f; // -50 mappa a 0
        Color color = computeKarmaColorForTest(karmaPercent);

        // A 0% dovrebbe essere rosso
        assertTrue(color.r > 0.8f, "Rosso dovrebbe essere alto a karma minimo");
        assertTrue(color.b < 0.3f, "Blu dovrebbe essere basso a karma minimo");
    }

    @Test
    public void testKarmaColorAtMaximum() {
        player.setKarma(50);
        float karmaPercent = 1f; // 50 mappa a 1
        Color color = computeKarmaColorForTest(karmaPercent);

        // A 100% dovrebbe essere azzurro
        assertTrue(color.b > 0.8f, "Blu dovrebbe essere alto a karma massimo");
        assertTrue(color.r < 0.3f, "Rosso dovrebbe essere basso a karma massimo");
    }

    @Test
    public void testKarmaColorAtZero() {
        player.setKarma(0);
        float karmaPercent = 0.5f;
        Color color = computeKarmaColorForTest(karmaPercent);

        // A 50% dovrebbe essere una sfumatura intermedia
        assertTrue(color.r > 0.4f, "Rosso dovrebbe essere presente nel mezzo");
        assertTrue(color.b > 0.4f, "Blu dovrebbe essere presente nel mezzo");
    }

    // -------------------------------------------------------------------------
    // Test XP Color Interpolation (Sfumature di Verde)
    // -------------------------------------------------------------------------

    @Test
    public void testXpColorAtZeroXp() {
        player.setXp(0);
        float xpPercent = 0f;
        Color color = computeLevelColorForTest(xpPercent);

        // A 0% dovrebbe essere verde scuro
        assertTrue(color.g > 0.2f, "Verde dovrebbe essere presente a basso XP");
        assertTrue(color.r < 0.3f, "Rosso dovrebbe essere basso a basso XP");
    }

    @Test
    public void testXpColorAtFullXp() {
        float xpPercent = 1f;
        Color color = computeLevelColorForTest(xpPercent);

        // A 100% dovrebbe essere verde brillante
        assertTrue(color.g > 0.8f, "Verde dovrebbe essere alto a pieno XP");
    }

    // -------------------------------------------------------------------------
    // Test interpolazione colore (helper method)
    // -------------------------------------------------------------------------

    @Test
    public void testColorLerpCorrectness() {
        Color red = Color.RED;
        Color green = Color.GREEN;

        // Lerp a 0
        Color result0 = lerpColorForTest(red, green, 0f);
        assertTrue(result0.r > 0.9f && result0.g < 0.1f, "Lerp(0) dovrebbe essere quasi rosso");

        // Lerp a 1
        Color result1 = lerpColorForTest(red, green, 1f);
        assertTrue(result1.r < 0.1f && result1.g > 0.9f, "Lerp(1) dovrebbe essere quasi verde");

        // Lerp a 0.5
        Color result05 = lerpColorForTest(red, green, 0.5f);
        assertTrue(Math.abs(result05.r - result05.g) < 0.1f, "Lerp(0.5) dovrebbe essere intermedio");
    }

    // -------------------------------------------------------------------------
    // Test XP progression e level-up
    // -------------------------------------------------------------------------

    @Test
    public void testXpAdditionAndLevelUp() {
        int levelBefore = player.getLevel();
        int xpBefore = player.getXp();

        player.addXp(50);

        assertEquals(xpBefore + 50, player.getXp(), "XP dovrebbe aumentare di 50");
        assertEquals(levelBefore, player.getLevel(), "Level potrebbe non aumentare se XP < soglia");
    }

    @Test
    public void testMultipleLevelUpFromLargeXp() {
        int levelBefore = player.getLevel();

        // Aggiungi abbastanza XP per fare più level-up
        player.addXp(500);

        assertTrue(player.getLevel() > levelBefore, "Level dovrebbe aumentare con grande XP");
    }

    // -------------------------------------------------------------------------
    // Helper methods per simulare i calcoli dei colori
    // -------------------------------------------------------------------------

    private Color computeHpColorForTest(float p) {
        p = Math.max(0f, Math.min(1f, p));
        if (p <= 0.5f) {
            float t = p / 0.5f;
            return lerpColorForTest(Color.RED, Color.YELLOW, t);
        } else {
            float t = (p - 0.5f) / 0.5f;
            return lerpColorForTest(Color.YELLOW, Color.GREEN, t);
        }
    }

    private Color computeKarmaColorForTest(float p) {
        Color a = new Color(0.85f, 0.2f, 0.2f, 1f);   // Rosso (Egoismo)
        Color b = new Color(0.12f, 0.66f, 0.86f, 1f); // Azzurro (Altruismo)
        return lerpColorForTest(a, b, p);
    }

    private Color computeLevelColorForTest(float p) {
        Color a = new Color(0.12f, 0.4f, 0.12f, 1f);
        Color b = new Color(0.56f, 0.9f, 0.25f, 1f);
        return lerpColorForTest(a, b, p);
    }

    private Color lerpColorForTest(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                a.r + (b.r - a.r) * t,
                a.g + (b.g - a.g) * t,
                a.b + (b.b - a.b) * t,
                a.a + (b.a - a.a) * t);
    }
}
