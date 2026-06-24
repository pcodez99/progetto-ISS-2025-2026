package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Enemy;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.model.combat.BattlePhase;
import io.github.iss_2025_2026.model.combat.ItemUseResult;

/**
 * Controller MVC del combattimento a turni. Coordina {@link BattleModel} e lo stato del menu.
 */
public class BattleController {
    public enum MenuState {
        MAIN_MENU,
        TARGET_SELECTION,
        INVENTORY_SELECTION
    }

    private final BattleModel model;
    private MenuState menuState;
    private boolean selectingSpecialTarget;

    public BattleController(BattleModel model) {
        this.model = model;
        this.menuState = MenuState.MAIN_MENU;
        this.selectingSpecialTarget = false;
    }

    public BattleModel getModel() {
        return model;
    }

    public MenuState getMenuState() {
        return menuState;
    }

    public void onAttackSelected() {
        selectingSpecialTarget = false;
        menuState = MenuState.TARGET_SELECTION;
    }

    public void onSpecialAbilityMenuSelected() {
        selectingSpecialTarget = true;
        menuState = MenuState.TARGET_SELECTION;
    }

    public void onMenuBack() {
        menuState = MenuState.MAIN_MENU;
        selectingSpecialTarget = false;
    }

    public void onTargetSelected(Enemy target) {
        Player currentPlayer = model.getCurrentTurnPlayer();
        model.executePlayerAttack(currentPlayer, target);
        menuState = MenuState.MAIN_MENU;
        selectingSpecialTarget = false;
    }

    public void onSpecialAbilitySelected() {
        Player currentPlayer = model.getCurrentTurnPlayer();
        model.executePlayerSpecialAbility(currentPlayer);
        menuState = MenuState.MAIN_MENU;
        selectingSpecialTarget = false;
    }

    public void onInventorySelected() {
        if (model.canCurrentPlayerUseItem()) {
            menuState = MenuState.INVENTORY_SELECTION;
        }
    }

    public ItemUseResult onItemSelected(Collectible item, Enemy target) {
        Player currentPlayer = model.getCurrentTurnPlayer();
        ItemUseResult result = model.executeUseItem(currentPlayer, item, target);
        menuState = result.isSuccess() || !model.canCurrentPlayerUseItem()
                ? MenuState.MAIN_MENU
                : MenuState.INVENTORY_SELECTION;
        return result;
    }

    public boolean onFleeAttempt() {
        return model.tryFlee();
    }

    /**
     * Salta il turno di un giocatore KO (HP <= 0).
     */
    public void onSkipKoTurn() {
        model.skipKoPlayerTurn();
        menuState = MenuState.MAIN_MENU;
    }

    public void update(float delta) {
        model.updateFleeTimer(delta);

        if (model.getPhase() == BattlePhase.ENEMY_TURN) {
            model.executeEnemyTurn();
        }
    }
}
