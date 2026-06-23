package io.github.iss_2025_2026.model.combat;

import io.github.iss_2025_2026.model.Characters;
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
    private boolean itemUsedThisTurn;
    private final List<String> battleLog;
    private final Random random;

    public BattleModel(Player playerOne, Player playerTwo, List<Enemy> enemies) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.enemies = new ArrayList<>(enemies);
        this.phase = BattlePhase.PLAYER_ONE_TURN;
        this.fleeTimer = INITIAL_FLEE_TIMER;
        this.totalXpEarned = 0;
        this.itemUsedThisTurn = false;
        this.battleLog = new ArrayList<>();
        this.random = new Random();
        // Resetta l'uso delle abilità speciali per la nuova battaglia
        resetPlayersSpecialAbilityUsage();
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
        if (attacker == null || target == null || !target.isAlive()) {
            return;
        }

        // Un giocatore con 0 HP non può attaccare
        if (!attacker.isAlive()) {
            battleLog.add(attacker.getName() + " è KO e non può attaccare!");
            resolveAfterPlayerAction(attacker);
            return;
        }

        int damage = attacker.getBaseDamage();
        target.takeDamage(damage);
        battleLog.add(attacker.getName() + " attacca " + target.getName() + "! Danni: " + damage + " HP");

        resolveAfterPlayerAction(attacker);
    }

    public void executePlayerSpecialAbility(Player attacker) {
        if (attacker == null) {
            return;
        }

        // Un giocatore con 0 HP non può usare abilità speciali
        if (!attacker.isAlive()) {
            battleLog.add(attacker.getName() + " è KO e non può usare abilità!");
            resolveAfterPlayerAction(attacker);
            return;
        }

        // Controllo: il giocatore può usare l'abilità speciale solo una volta per battaglia
        if (attacker.hasUsedSpecialAbilityThisBattle()) {
            battleLog.add(" EHI, dovresti sapere che posso usare l'abilità speciale una sola volta per battaglia, ora abbiamo sprecato un turno!");
            resolveAfterPlayerAction(attacker);
            return;
        }

        SpecialAbility ability = attacker.getAbility();
        if (ability == null) {
            resolveAfterPlayerAction(attacker);
            return;
        }

        List<Characters> targets = resolveSpecialAbilityTargets(attacker);
        if (targets.isEmpty()) {
            resolveAfterPlayerAction(attacker);
            return;
        }

        if (ability instanceof DataDrivenAbility) {
            ((DataDrivenAbility) ability).performOnTargets(attacker, targets, attacker.getLevel());
        } else {
            for (Characters target : targets) {
                ability.perform(attacker, target, attacker.getLevel());
            }
        }

        appendSpecialAbilityLog(attacker, ability);

        // Segna che il giocatore ha usato l'abilità speciale in questa battaglia
        attacker.markSpecialAbilityUsed();

        resolveAfterPlayerAction(attacker);
    }

    public ItemUseResult executeUseItem(Player user, Collectible item, Enemy target) {
        ItemUseResult validationResult = validateItemUse(user, item);
        if (validationResult != ItemUseResult.USED) {
            battleLog.add(validationResult.getMessage());
            return validationResult;
        }

        List<Characters> targets = buildItemTargets(user, item, target);
        item.use(new CollectibleUseContext(user, targets));
        user.getBackpack().removeItem(item);
        battleLog.add(user.getName() + " usa " + item.getName());
        itemUsedThisTurn = true;

        updateEndState();
        return ItemUseResult.USED;
    }

    public boolean canCurrentPlayerUseItem() {
        return isPlayerTurn() && !itemUsedThisTurn;
    }

    public void executeEnemyTurn() {
        if (phase != BattlePhase.ENEMY_TURN) {
            return;
        }

        List<Player> alivePlayers = getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            updateEndState();
            return;
        }

        for (Enemy enemy : getAliveEnemies()) {
            Player target = alivePlayers.get(random.nextInt(alivePlayers.size()));
            int damage = enemy.getBaseDamage();
            target.takeDamage(damage);
            battleLog.add(enemy.getName() + " attacca " + target.getName() + "! Danni: " + damage + " HP");
        }

        if (updateEndState()) {
            return;
        }

        itemUsedThisTurn = false;
        // Se P1 è KO e P2 è vivo, salta direttamente al turno di P2
        if (!playerOne.isAlive() && playerTwo != null && playerTwo.isAlive()) {
            phase = BattlePhase.PLAYER_TWO_TURN;
        } else {
            phase = BattlePhase.PLAYER_ONE_TURN;
        }
    }

    /**
     * Salta il turno del giocatore corrente se è KO.
     * Avanza alla fase successiva (turno P2 o turno nemico).
     */
    public void skipKoPlayerTurn() {
        Player current = getCurrentTurnPlayer();
        if (current == null || current.isAlive()) {
            return; // non è KO, non saltare
        }
        battleLog.add(current.getName() + " è KO, turno saltato!");
        resolveAfterPlayerAction(current);
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
        // Rianima i giocatori KO a 1 HP se almeno un compagno è sopravvissuto
        reviveDeadPlayers();

        int xp = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                xp += enemy.getXpReward();
            }
        }
        totalXpEarned = xp;
        if (xp > 0) {
            battleLog.add("Vittoria! +" + xp + " XP guadagnati.");
            // Distribuisci l'XP equamente tra tutti i giocatori (inclusi quelli rianimati)
            List<Player> allPlayers = getAllPlayers();
            if (!allPlayers.isEmpty()) {
                int perPlayer = xp / allPlayers.size();
                int remainder = xp % allPlayers.size();
                for (int i = 0; i < allPlayers.size(); i++) {
                    Player p = allPlayers.get(i);
                    int grant = perPlayer + (i == 0 ? remainder : 0); // assegna il resto al primo
                    if (grant > 0) {
                        p.addXp(grant);
                        battleLog.add(p.getName() + " riceve " + grant + " XP");
                    }
                }
            }
        }
    }

    /**
     * Rianima i giocatori KO a 1 HP se almeno uno dei due è ancora vivo.
     * Chiamato alla fine di una battaglia vinta.
     */
    private void reviveDeadPlayers() {
        if (playerTwo == null) {
            return; // single player, niente da rianimare
        }
        boolean p1Alive = playerOne.isAlive();
        boolean p2Alive = playerTwo.isAlive();
        if (p1Alive && !p2Alive) {
            playerTwo.setHp(1);
            battleLog.add(playerTwo.getName() + " è stato rianimato con 1 HP!");
        } else if (!p1Alive && p2Alive) {
            playerOne.setHp(1);
            battleLog.add(playerOne.getName() + " è stato rianimato con 1 HP!");
        }
    }

    /**
     * Restituisce tutti i giocatori (vivi e KO).
     */
    private List<Player> getAllPlayers() {
        List<Player> all = new ArrayList<>();
        all.add(playerOne);
        if (playerTwo != null) {
            all.add(playerTwo);
        }
        return all;
    }

    public int getTotalXpEarned() {
        return totalXpEarned;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    /**
     * Verifica se la battaglia includeva almeno un boss.
     * Utilizzato per determinare se la vittoria deve innescare il completamento del livello.
     */
    public boolean wasBossBattle() {
        for (Enemy enemy : enemies) {
            if (enemy.isBoss()) {
                return true;
            }
        }
        return false;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    private void resolveAfterPlayerAction(Player actingPlayer) {
        if (updateEndState()) {
            return;
        }

        itemUsedThisTurn = false;
        if (actingPlayer == playerOne && playerTwo != null && playerTwo.isAlive()) {
            phase = BattlePhase.PLAYER_TWO_TURN;
        } else {
            phase = BattlePhase.ENEMY_TURN;
        }
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

    private ItemUseResult validateItemUse(Player user, Collectible item) {
        if (user == null || !isPlayerTurn() || user != getCurrentTurnPlayer()) {
            return ItemUseResult.INVALID_TURN;
        }
        if (item == null) {
            return ItemUseResult.INVALID_ITEM;
        }
        if (!user.getBackpack().getItems().contains(item)) {
            return ItemUseResult.NOT_OWNED;
        }
        if (itemUsedThisTurn) {
            return ItemUseResult.ALREADY_USED;
        }
        return ItemUseResult.USED;
    }

    private boolean isPlayerTurn() {
        return phase == BattlePhase.PLAYER_ONE_TURN || phase == BattlePhase.PLAYER_TWO_TURN;
    }

    private boolean allEnemiesDefeated() {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return !enemies.isEmpty();
    }

    private List<Characters> buildItemTargets(Player user, Collectible item, Enemy target) {
        List<Characters> targets = new ArrayList<>();
        String effectType = item.getEffectType();
        if (effectType != null && ("HEAL".equalsIgnoreCase(effectType) || "BUFF".equalsIgnoreCase(effectType))) {
            targets.add(user);
        } else if (item.isAoe()) {
            for (Enemy enemy : getAliveEnemies()) {
                targets.add(enemy);
            }
        } else if (target != null) {
            targets.add(target);
        } else {
            targets.add(user);
        }
        return targets;
    }

    private List<Characters> resolveSpecialAbilityTargets(Player attacker) {
        SpecialAbility ability = attacker.getAbility();
        if (ability == null) {
            return Collections.emptyList();
        }

        if (ability instanceof DataDrivenAbility) {
            String strategy = ((DataDrivenAbility) ability).getConfig().getStrategy();
            if ("HEAL".equalsIgnoreCase(strategy)) {
                return new ArrayList<Characters>(getAlivePlayers());
            }
            if ("DAMAGE".equalsIgnoreCase(strategy)) {
                return new ArrayList<Characters>(getAliveEnemies());
            }
        }

        List<Characters> enemyTargets = new ArrayList<>();
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

    /**
     * Resetta l'uso delle abilità speciali per entrambi i giocatori all'inizio di una nuova battaglia
     */
    private void resetPlayersSpecialAbilityUsage() {
        if (playerOne != null) {
            playerOne.resetSpecialAbilityUsageForBattle();
        }
        if (playerTwo != null) {
            playerTwo.resetSpecialAbilityUsageForBattle();
        }
    }
}
