package io.github.iss_2025_2026.model;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import io.github.iss_2025_2026.factory.CharacterFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.iss_2025_2026.model.Enemy;
public class EnemyFactoryTest {
    private CharacterFactory factory;

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
        factory = new CharacterFactory();
    }

    @Test
    public void testCreateAlienBase() {
        // 1. Creiamo l'alieno base dallo YAML
        Enemy alieno = factory.createEnemy("alieno_base");

        // 2. Verifichiamo che i dati corrispondano a quelli scritti nel file .yaml
        assertNotNull(alieno, "L'alieno non dovrebbe essere nullo!");
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
        Enemy boss = factory.createEnemy("boss_alieno");

        // 2. Verifichiamo i dati del boss
        assertNotNull(boss);
        assertTrue(boss.isBoss(), "Il boss deve avere il flag isBoss a true");
        assertEquals(350, boss.getMaxHp());

        // CORRETTO: Verifichiamo che il boss ABBIA l'abilità "stone_rain" agganciata
        assertTrue(boss.hasSpecialAbility(), "Il boss dovrebbe avere un'abilità speciale");
        assertEquals("Pioggia di Pietre", boss.getSpecialAbility().getName());
    }

    @Test
    public void testInvalidEnemyId() {
        // Verifichiamo che se inseriamo un ID inventato, il sistema lanci l'eccezione corretta
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createEnemy("id_inesistente_che_fa_crashtare");
        });
    }
}
