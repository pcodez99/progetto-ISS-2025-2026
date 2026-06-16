package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.factory.EvolutionRuleFactory;
import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.CharacterEvolutionRules;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.EvolutionPath;
import io.github.iss_2025_2026.model.EvolutionResult;
import io.github.iss_2025_2026.model.EvolutionUnlockRule;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.PlayerChoiceEvent;
import io.github.iss_2025_2026.model.PlayerEvolutionState;
import java.util.ArrayList;
import java.util.List;

public class EvolutionService {
    private final EvolutionRuleFactory ruleFactory;

    public EvolutionService() {
        this(new EvolutionRuleFactory());
    }

    public EvolutionService(EvolutionRuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    public EvolutionResult applyChoice(Player player, PlayerChoiceEvent event) {
        return applyChoice(player, null, event);
    }

    public EvolutionResult applyNpcChoice(Player player, Npc npc, ChoiceEventType type) {
        String npcId = npc != null ? npc.getId() : null;
        return applyChoice(player, npc, PlayerChoiceEvent.forNpc(type, npcId));
    }

    private EvolutionResult applyChoice(Player player, Npc npc, PlayerChoiceEvent event) {
        if (player == null || event == null || event.getType() == null) {
            return new EvolutionResult(EvolutionPath.BALANCED, EvolutionPath.BALANCED, AbilitySlot.BASE,
                    new ArrayList<AbilitySlot>());
        }

        PlayerEvolutionState state = player.getEvolutionState();
        EvolutionPath previousPath = state.getDominantPath();
        applyEventToState(state, npc, event);
        List<AbilitySlot> unlockedSlots = unlockAvailableAbilities(player, state);
        selectDominantUnlockedAbility(player, state);
        return new EvolutionResult(previousPath, state.getDominantPath(), player.getSelectedAbilitySlot(),
                unlockedSlots);
    }

    private void applyEventToState(PlayerEvolutionState state, Npc npc, PlayerChoiceEvent event) {
        String npcId = resolveNpcId(npc, event);
        int altruismReward = npc != null && npc.getAltruismReward() > 0 ? npc.getAltruismReward() : 8;
        int egoismReward = npc != null && npc.getAltruismPenalty() < 0 ? Math.abs(npc.getAltruismPenalty()) : 8;

        switch (event.getType()) {
            case HELP_NPC:
            case COMPLETE_NPC_QUEST:
                state.addAltruism(altruismReward);
                state.incrementHelpedNpcs();
                state.increaseNpcTrust(npcId, altruismReward);
                return;
            case STEAL_ITEM:
                state.addEgoism(egoismReward);
                state.incrementStolenItems();
                state.increaseNpcTrust(npcId, -egoismReward);
                return;
            case LIE:
                state.addEgoism(Math.max(6, egoismReward / 2));
                state.incrementLiesTold();
                state.increaseNpcTrust(npcId, -6);
                return;
            case THREATEN:
                state.addEgoism(Math.max(8, egoismReward));
                state.incrementThreatenedNpcs();
                state.increaseNpcTrust(npcId, -8);
                return;
            case IGNORE_REQUEST:
                state.addEgoism(Math.max(4, egoismReward / 2));
                state.incrementIgnoredRequests();
                state.increaseNpcTrust(npcId, -4);
                return;
            case SPARE_ALIEN:
                state.addAltruism(12);
                state.incrementSparedAliens();
                return;
            case SELFISH_REWARD:
                state.addEgoism(8);
                state.incrementSelfishRewards();
                return;
            case REDEEM:
                state.addAltruism(10);
                state.incrementRedemptions();
                state.increaseNpcTrust(npcId, 5);
                return;
            default:
        }
    }

    private List<AbilitySlot> unlockAvailableAbilities(Player player, PlayerEvolutionState state) {
        List<AbilitySlot> unlockedSlots = new ArrayList<>();
        CharacterEvolutionRules rules = ruleFactory.getRulesForCharacter(player.getCharacterId());
        if (rules == null) {
            return unlockedSlots;
        }

        tryUnlockSlot(player, state, rules, AbilitySlot.ALTRUISTIC, unlockedSlots);
        tryUnlockSlot(player, state, rules, AbilitySlot.EGOISTIC, unlockedSlots);
        return unlockedSlots;
    }

    private void tryUnlockSlot(Player player, PlayerEvolutionState state, CharacterEvolutionRules rules,
            AbilitySlot slot, List<AbilitySlot> unlockedSlots) {
        if (state.isAbilitySlotUnlocked(slot) || player.getAbility(slot) == null) {
            return;
        }
        EvolutionUnlockRule rule = rules.getUnlockRule(slot);
        if (rule != null && rule.isSatisfiedBy(state)) {
            player.unlockAbilitySlot(slot);
            unlockedSlots.add(slot);
        }
    }

    private void selectDominantUnlockedAbility(Player player, PlayerEvolutionState state) {
        EvolutionPath path = state.getDominantPath();
        if (path == EvolutionPath.ALTRUISTIC && player.selectAbilitySlot(AbilitySlot.ALTRUISTIC)) {
            return;
        }
        if (path == EvolutionPath.EGOISTIC && player.selectAbilitySlot(AbilitySlot.EGOISTIC)) {
            return;
        }
        if (!player.isAbilitySlotUnlocked(player.getSelectedAbilitySlot())) {
            player.selectAbilitySlot(AbilitySlot.BASE);
        }
    }

    private String resolveNpcId(Npc npc, PlayerChoiceEvent event) {
        if (npc != null) {
            return npc.getId();
        }
        return event != null ? event.getNpcId() : null;
    }
}
