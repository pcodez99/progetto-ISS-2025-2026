package io.github.iss_2025_2026.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.iss_2025_2026.model.Character;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

class CharacterBuilderTest {
    @Test
    void concreteBuildersImplementTheCommonConstructionSteps() {
        CharacterBuilder playerBuilder = new PlayerBuilder()
                .id("papa")
                .name("Papa")
                .maxHp(150)
                .baseDamage(17);
        CharacterBuilder enemyBuilder = new EnemyBuilder()
                .id("alieno_base")
                .name("Alieno Invasore")
                .maxHp(45)
                .baseDamage(8);

        Character player = playerBuilder.build();
        Character enemy = enemyBuilder.build();

        assertEquals(Player.class, player.getClass());
        assertEquals(Enemy.class, enemy.getClass());
    }

    @Test
    void buildResetsConcreteBuildersForTheNextProduct() {
        PlayerBuilder playerBuilder = new PlayerBuilder().name("Configurato").maxHp(220);
        EnemyBuilder enemyBuilder = new EnemyBuilder().name("Boss configurato").boss(true);

        playerBuilder.build();
        enemyBuilder.build();
        Player resetPlayer = playerBuilder.build();
        Enemy resetEnemy = enemyBuilder.build();

        assertEquals("Unknown", resetPlayer.getName());
        assertEquals(100, resetPlayer.getMaxHp());
        assertEquals("Unknown Enemy", resetEnemy.getName());
        assertFalse(resetEnemy.isBoss());
    }
}
