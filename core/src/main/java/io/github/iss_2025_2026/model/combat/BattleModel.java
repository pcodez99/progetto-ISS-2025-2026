package io.github.iss_2025_2026.model.combat;

import io.github.iss_2025_2026.model.Character;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import io.github.iss_2025_2026.model.abilities.DataDrivenAbility;
import io.github.iss_2025_2026.model.collectibles.CollectibleUseContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Logica centrale del combattimento a turni. Priva di dipendenze LibGDX.
 */
public class BattleModel {
    private static final float INITIAL_FLEE_TIMER = 10.0f;

    private final Player playerOne;
    private final Player playerTwo;
    private final List<Enemy> enemies;
    private BattlePhase phase;
    private float fleeTimer;
    private int totalXpEarned;
    private final List<String> battleLog;
    private final Random random;

    public BattleModel(Player playerOne, Player playerTwo, List<Enemy> enemies) {
        this(playerOne, playerTwo, enemies, new Random());
    }

    public BattleModel(Player playerOne, Player playerTwo, List<Enemy> enemies, Random random) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.enemies = new ArrayList<>(enemies);
        this.phase = BattlePhase.PLAYER_ONE_TURN;
        this.fleeTimer = INITIAL_FLEE_TIMER;
        this.totalXpEarned = 0;
        this.battleLog = new ArrayList<>();
        this.random = random != null ? random : new Random();
        skipDeadPlayerTurns();
    }

    public boolean canCurrentTurnPlayerAct() {
        if (phase != BattlePhase.PLAYER_ONE_TURN && phase != BattlePhase.PLAYER_TWO_TURN) {
            return false;
        }
        Player current = getCurrentTurnPlayer();
        return current != null && current.isAlive();
    }

    public BattlePhase getPhase() {
        return phase;
    }

    public float getFleeTimer() {
        return fleeTimer;
    }

    public List<String> getBattleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    public Player getCurrentTurnPlayer() {
        if (phase == BattlePhase.PLAYER_TWO_TURN) {
            return playerTwo;
        }
        return playerOne;
    }

    public void executePlayerAttack(Player attacker, Enemy target) {
        if (attacker == null || !attacker.isAlive() || target == null || !target.isAlive()) {
            return;
        }

        int damage = attacker.getBaseDamage();
        target.takeDamage(damage);
        battleLog.add(attacker.getName() + " attacca " + target.getName() + "! Danni: " + damage + " HP");

        resolveAfterPlayerAction(attacker);
    }

    public void executePlayerSpecialAbility(Player attacker) {
        if (attacker == null || !attacker.isAlive()) {
            return;
        }

        SpecialAbility ability = attacker.getAbility();
        if (ability == null) {
            resolveAfterPlayerAction(attacker);
            return;
        }

        List<Character> targets = resolveSpecialAbilityTargets(attacker);
        if (targets.isEmpty()) {
            resolveAfterPlayerAction(attacker);
            return;
        }

        if (ability instanceof DataDrivenAbility) {
            ((DataDrivenAbility) ability).performOnTargets(attacker, targets, attacker.getLevel());
        } else {
            for (Character target : targets) {
                ability.perform(attacker, target, attacker.getLevel());
            }
        }

        appendSpecialAbilityLog(attacker, ability);
        resolveAfterPlayerAction(attacker);
    }

    public void executeUseItem(Player user, Collectible item, Enemy target) {
        if (user == null || !user.isAlive() || item == null) {
            return;
        }

        List<Character> targets = buildItemTargets(user, item, target);
        item.use(new CollectibleUseContext(user, targets));
        user.getBackpack().removeItem(item);
        battleLog.add(user.getName() + " usa " + item.getName());

        resolveAfterPlayerAction(user);
    }

    public void executeEnemyTurn() {
        if (phase != BattlePhase.ENEMY_TURN) {
            return;
        }

        if (getAlivePlayers().isEmpty()) {
            updateEndState();
            return;
        }

        for (Enemy enemy : getAliveEnemies()) {
            List<Player> alivePlayers = getAlivePlayers();
            if (alivePlayers.isEmpty()) {
                break;
            }

            Player target = alivePlayers.get(random.nextInt(alivePlayers.size()));
            int damage = enemy.getBaseDamage();
            target.takeDamage(damage);
            battleLog.add(enemy.getName() + " attacca " + target.getName() + "! Danni: " + damage + " HP");
        }

        if (updateEndState()) {
            return;
        }

        advanceToNextAlivePlayerTurn();
    }

    /**
     * Salta i turni dei giocatori a 0 HP e passa al prossimo giocatore vivo o al turno nemico.
     */
    public void skipDeadPlayerTurns() {
        if (phase != BattlePhase.PLAYER_ONE_TURN && phase != BattlePhase.PLAYER_TWO_TURN) {
            return;
        }
        if (updateEndState()) {
            return;
        }

        if (phase == BattlePhase.PLAYER_ONE_TURN && !playerOne.isAlive()) {
            if (playerTwo != null && playerTwo.isAlive()) {
                phase = BattlePhase.PLAYER_TWO_TURN;
            } else {
                phase = BattlePhase.ENEMY_TURN;
            }
        }

        if (phase == BattlePhase.PLAYER_TWO_TURN && (playerTwo == null || !playerTwo.isAlive())) {
            phase = BattlePhase.ENEMY_TURN;
        }
    }

    public boolean tryFlee() {
        if (fleeTimer > 0f) {
            phase = BattlePhase.FLED;
            battleLog.add("Sei fuggito dalla battaglia!");
            return true;
        }
        return false;
    }

    public void updateFleeTimer(float delta) {
        if (delta > 0f && fleeTimer > 0f) {
            fleeTimer = Math.max(0f, fleeTimer - delta);
        }
    }

    public boolean isBattleOver() {
        if (phase == BattlePhase.VICTORY || phase == BattlePhase.DEFEAT || phase == BattlePhase.FLED) {
            return true;
        }
        return allEnemiesDefeated() || isDefeat();
    }

    public boolean isVictory() {
        return allEnemiesDefeated();
    }

    public boolean isDefeat() {
        if (playerTwo == null) {
            return !playerOne.isAlive();
        }
        return !playerOne.isAlive() && !playerTwo.isAlive();
    }

    public List<Enemy> getAliveEnemies() {
        List<Enemy> alive = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                alive.add(enemy);
            }
        }
        return alive;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        if (playerOne.isAlive()) {
            alive.add(playerOne);
        }
        if (playerTwo != null && playerTwo.isAlive()) {
            alive.add(playerTwo);
        }
        return alive;
    }

    public void awardXpToPlayers() {
        int xp = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                xp += enemy.getXpReward();
            }
        }
        totalXpEarned = xp;
        if (xp > 0) {
            battleLog.add("Vittoria! +" + xp + " XP guadagnati.");
        }
    }

    public int getTotalXpEarned() {
        return totalXpEarned;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    private void resolveAfterPlayerAction(Player actingPlayer) {
        if (actingPlayer == null || !actingPlayer.isAlive()) {
            return;
        }
        if (updateEndState()) {
            return;
        }

        if (actingPlayer == playerOne && playerTwo != null && playerTwo.isAlive()) {
            phase = BattlePhase.PLAYER_TWO_TURN;
        } else {
            phase = BattlePhase.ENEMY_TURN;
        }
    }

    private void advanceToNextAlivePlayerTurn() {
        if (playerOne.isAlive()) {
            phase = BattlePhase.PLAYER_ONE_TURN;
            return;
        }
        if (playerTwo != null && playerTwo.isAlive()) {
            phase = BattlePhase.PLAYER_TWO_TURN;
            return;
        }
        phase = BattlePhase.ENEMY_TURN;
    }

    private boolean updateEndState() {
        if (allEnemiesDefeated()) {
            phase = BattlePhase.VICTORY;
            return true;
        }
        if (isDefeat()) {
            phase = BattlePhase.DEFEAT;
            return true;
        }
        return false;
    }

    private boolean allEnemiesDefeated() {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return !enemies.isEmpty();
    }

    private List<Character> buildItemTargets(Player user, Collectible item, Enemy target) {
        List<Character> targets = new ArrayList<>();
        String effectType = item.getEffectType();
        if (effectType != null && ("HEAL".equalsIgnoreCase(effectType) || "BUFF".equalsIgnoreCase(effectType))) {
            targets.add(user);
        } else if (target != null) {
            targets.add(target);
        } else {
            targets.add(user);
        }
        return targets;
    }

    private List<Character> resolveSpecialAbilityTargets(Player attacker) {
        SpecialAbility ability = attacker.getAbility();
        if (ability == null) {
            return Collections.emptyList();
        }

        if (ability instanceof DataDrivenAbility) {
            String strategy = ((DataDrivenAbility) ability).getConfig().getStrategy();
            if ("HEAL".equalsIgnoreCase(strategy)) {
                return new ArrayList<Character>(getAlivePlayers());
            }
            if ("DAMAGE".equalsIgnoreCase(strategy)) {
                return new ArrayList<Character>(getAliveEnemies());
            }
        }

        List<Character> enemyTargets = new ArrayList<>();
        for (Enemy enemy : getAliveEnemies()) {
            enemyTargets.add(enemy);
        }
        return enemyTargets;
    }

    private void appendSpecialAbilityLog(Player attacker, SpecialAbility ability) {
        if (ability instanceof DataDrivenAbility) {
            DataDrivenAbility dataAbility = (DataDrivenAbility) ability;
            int effectAmount = dataAbility.getEffectAmount(attacker.getLevel());
            String strategy = dataAbility.getConfig().getStrategy();
            if ("HEAL".equalsIgnoreCase(strategy)) {
                battleLog.add(attacker.getName() + " usa " + ability.getName() + "! Cura: " + effectAmount + " HP");
                return;
            }
            battleLog.add(attacker.getName() + " usa " + ability.getName() + "! Danni: " + effectAmount + " HP");
            return;
        }
        battleLog.add(attacker.getName() + " usa " + ability.getName() + "!");
    }
}
