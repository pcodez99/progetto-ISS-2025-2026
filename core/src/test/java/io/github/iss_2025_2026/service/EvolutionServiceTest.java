package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.EvolutionPath;
import io.github.iss_2025_2026.model.EvolutionResult;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.SpecialAbility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EvolutionServiceTest {
    private final EvolutionService service = new EvolutionService();

    @Test
    public void altruisticChoicesUnlockAltruisticAbility() {
        Player player = createPlayer("mamma");
        Npc npc = createNpc("zia_pina", 12, -10);

        service.applyNpcChoice(player, npc, ChoiceEventType.HELP_NPC);
        EvolutionResult result = service.applyNpcChoice(player, npc, ChoiceEventType.COMPLETE_NPC_QUEST);

        assertTrue(result.hasNewUnlocks());
        assertTrue(player.isAbilitySlotUnlocked(AbilitySlot.ALTRUISTIC));
        assertEquals(AbilitySlot.ALTRUISTIC, player.getSelectedAbilitySlot());
        assertEquals(EvolutionPath.ALTRUISTIC, player.getEvolutionState().getDominantPath());
        assertEquals(24, player.getEvolutionState().getAltruismScore());
    }

    @Test
    public void egoisticChoicesUnlockEgoisticAbility() {
        Player player = createPlayer("mamma");
        Npc npc = createNpc("zio_toto", 10, -15);

        service.applyNpcChoice(player, npc, ChoiceEventType.STEAL_ITEM);
        EvolutionResult result = service.applyNpcChoice(player, npc, ChoiceEventType.SELFISH_REWARD);

        assertTrue(result.hasNewUnlocks());
        assertTrue(player.isAbilitySlotUnlocked(AbilitySlot.EGOISTIC));
        assertEquals(AbilitySlot.EGOISTIC, player.getSelectedAbilitySlot());
        assertEquals(EvolutionPath.EGOISTIC, player.getEvolutionState().getDominantPath());
        assertEquals(23, player.getEvolutionState().getEgoismScore());
    }

    @Test
    public void npcTrustTracksTheRelativePlayerChoices() {
        Player player = createPlayer("papa");
        Npc npc = createNpc("zyrko_ingegnere_alieno", 10, -10);

        service.applyNpcChoice(player, npc, ChoiceEventType.HELP_NPC);
        service.applyNpcChoice(player, npc, ChoiceEventType.LIE);

        assertEquals(4, player.getEvolutionState().getNpcTrust("zyrko_ingegnere_alieno"));
    }

    private Player createPlayer(String characterId) {
        Player player = new Player("Test", 100, 10, new MockAbility("base"), 10, 2, 1);
        player.setCharacterId(characterId);
        player.setAbilitySlot(AbilitySlot.ALTRUISTIC, new MockAbility("altruistic"));
        player.setAbilitySlot(AbilitySlot.EGOISTIC, new MockAbility("egoistic"));
        return player;
    }

    private Npc createNpc(String id, int altruismReward, int altruismPenalty) {
        Npc npc = new Npc();
        npc.setId(id);
        npc.setName(id);
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.QUEST_GIVER);
        npc.setAltruismReward(altruismReward);
        npc.setAltruismPenalty(altruismPenalty);
        return npc;
    }

    private static class MockAbility implements SpecialAbility {
        private final String name;

        private MockAbility(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return name;
        }

        @Override
        public void perform(io.github.iss_2025_2026.model.Characters user,
                io.github.iss_2025_2026.model.Characters target, int userLevel) {
        }
    }
}
