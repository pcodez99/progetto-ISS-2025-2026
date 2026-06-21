package io.github.iss_2025_2026.model;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import io.github.iss_2025_2026.factory.EnemyFactory;
import io.github.iss_2025_2026.factory.PlayerFactory;
import io.github.iss_2025_2026.factory.YamlEnemyFactory;
import io.github.iss_2025_2026.factory.YamlPlayerFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnemyFactoryTest {
    private EnemyFactory enemyFactory;
    private PlayerFactory playerFactory;

    //Motore di gioco invisibile:
    @BeforeAll
    public static void initLibGDX(){
        //controlliamo non sia già stato inviato un'altro test
        if(Gdx.app==null){
            HeadlessApplicationConfiguration config=new HeadlessApplicationConfiguration();
            new HeadlessApplication(new ApplicationAdapter(){},config);
        }
    }

    @BeforeEach
    public void setUp() {
        // Inizializziamo la factory prima di ogni test
        enemyFactory = new YamlEnemyFactory();
        playerFactory = new YamlPlayerFactory();
    }

    @Test
    public void testCreateAlienBase() {
        // 1. Creiamo l'alieno base dallo YAML
        Enemy alieno = enemyFactory.create("alieno_base");

        // 2. Verifichiamo che i dati corrispondano a quelli scritti nel file .yaml
        assertNotNull(alieno, "L'alieno non dovrebbe essere nullo!");
        assertEquals(Enemy.class, alieno.getClass());
        assertEquals("Alieno Invasore", alieno.getName());
        assertEquals(45, alieno.getMaxHp());
        assertEquals(8, alieno.getBaseDamage());
        assertFalse(alieno.isBoss(), "L'alieno base non deve essere un boss");

        // CORRETTO: Verifichiamo che l'alieno base NON abbia abilità speciali
        assertFalse(alieno.hasSpecialAbility(), "L'alieno base non dovrebbe avere abilità");
    }

    @Test
    public void testCreateBoss() {
        // 1. Creiamo il boss dallo YAML
        Enemy boss = enemyFactory.create("boss_livello_1");

        // 2. Verifichiamo i dati del boss
        assertNotNull(boss);
        assertEquals(Enemy.class, boss.getClass());
        assertTrue(boss.isBoss(), "Il boss deve avere il flag isBoss a true");
        assertEquals(220, boss.getMaxHp());

        // CORRETTO: Verifichiamo che il boss ABBIA l'abilità "stone_rain" agganciata
        assertTrue(boss.hasSpecialAbility(), "Il boss dovrebbe avere un'abilità speciale");
        assertEquals("Pioggia di Pietre", boss.getSpecialAbility().getName());
    }

    @Test
    public void testInvalidEnemyId() {
        // Verifichiamo che se inseriamo un ID inventato, il sistema lanci l'eccezione corretta
        assertThrows(IllegalArgumentException.class, () -> {
            enemyFactory.create("id_inesistente_che_fa_crashtare");
        });
    }

    @Test
    public void createEnemyReturnsIndependentPrototypeCopies() {
        Enemy firstAlien = enemyFactory.create("alieno_base");
        Enemy secondAlien = enemyFactory.create("alieno_base");

        assertNotSame(firstAlien, secondAlien);
        firstAlien.takeDamage(20);
        firstAlien.setX(250f);

        assertEquals(25, firstAlien.getHp());
        assertEquals(45, secondAlien.getHp());
        assertEquals(0f, secondAlien.getX());
    }

    @Test
    public void createPlayerBuildsIndependentInstances() {
        Player firstPlayer = playerFactory.create("papa");
        Player secondPlayer = playerFactory.create("papa");

        assertNotSame(firstPlayer, secondPlayer);
        assertEquals(Player.class, firstPlayer.getClass());
        assertNotSame(firstPlayer.getBackpack(), secondPlayer.getBackpack());

        firstPlayer.modifyKarma(15);
        firstPlayer.getBackpack().addItem(new Collectible("test", "Test", "Oggetto test", "HEAL", false, 1));

        assertEquals(15, firstPlayer.getKarma());
        assertEquals(0, secondPlayer.getKarma());
        assertEquals(1, firstPlayer.getBackpack().getSize());
        assertEquals(0, secondPlayer.getBackpack().getSize());
    }

    @Test
    public void enemyYamlRepresentsReadmeContract() {
        Map<String, Map<String, Object>> enemies = enemyFactory.getEnemyData();

        assertEquals(6, enemies.size(), "Il README prevede 3 classi di alieni e 3 boss finali.");
        assertEquals(3, enemies.values().stream().filter(enemy -> !bool(enemy.get("isBoss"))).count());
        assertEquals(3, enemies.values().stream().filter(enemy -> bool(enemy.get("isBoss"))).count());

        Map<String, Object> sciame = enemies.get("alieno_sciame");
        Map<String, Object> base = enemies.get("alieno_base");
        Map<String, Object> guardiano = enemies.get("alieno_guardiano");
        assertNotNull(sciame);
        assertNotNull(base);
        assertNotNull(guardiano);

        assertEquals("SCIAME", sciame.get("enemyClass"));
        assertEquals("INVASORE", base.get("enemyClass"));
        assertEquals("GUARDIANO", guardiano.get("enemyClass"));
        assertTrue(bool(encounter(sciame).get("immediateBattle")));
        assertTrue(bool(encounter(base).get("immediateBattle")));
        assertTrue(bool(encounter(guardiano).get("immediateBattle")));

        assertTrue(intValue(encounter(sciame).get("maxGroupSize")) > intValue(encounter(base).get("maxGroupSize")));
        assertTrue(intValue(encounter(base).get("maxGroupSize")) > intValue(encounter(guardiano).get("maxGroupSize")));
        assertTrue(intValue(sciame.get("maxHp")) < intValue(base.get("maxHp")));
        assertTrue(intValue(base.get("maxHp")) < intValue(guardiano.get("maxHp")));
        assertTrue(intValue(sciame.get("baseDamage")) < intValue(base.get("baseDamage")));
        assertTrue(intValue(base.get("baseDamage")) < intValue(guardiano.get("baseDamage")));

        assertEquals(1, intValue(enemies.get("boss_livello_1").get("levelId")));
        assertEquals(2, intValue(enemies.get("boss_livello_2").get("levelId")));
        assertEquals(3, intValue(enemies.get("boss_livello_3").get("levelId")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> encounter(Map<String, Object> enemy) {
        return (Map<String, Object>) enemy.get("encounter");
    }

    private static boolean bool(Object value) {
        return Boolean.TRUE.equals(value);
    }

    private static int intValue(Object value) {
        return ((Number) value).intValue();
    }
}
