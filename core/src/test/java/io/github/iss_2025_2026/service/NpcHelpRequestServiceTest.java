package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.factory.NpcFactory;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcHelpRequest;
import io.github.iss_2025_2026.model.NpcHelpRequestChoice;
import io.github.iss_2025_2026.model.NpcHelpRequestOutcome;
import io.github.iss_2025_2026.model.NpcHelpRequestResult;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NpcHelpRequestServiceTest {
    private NpcHelpRequestService service;

    @BeforeEach
    void setUp() {
        service = new NpcHelpRequestService(new CollectibleFactory(), new EvolutionService());
    }

    @Test
    void acceptingAddsKarmaRewardAndPersistentOutcomeOnlyOnce() {
        Player player = new Player("Mamma", 100, 10, null);
        Npc npc = createNpc();

        NpcHelpRequestResult first = service.resolve(player, npc, NpcHelpRequestChoice.ACCEPT);
        NpcHelpRequestResult duplicate = service.resolve(player, npc, NpcHelpRequestChoice.ACCEPT);

        assertTrue(first.isResolved());
        assertEquals(10, player.getKarma());
        assertEquals(1, player.getBackpack().getSize());
        assertEquals("SMALL_POTION", player.getBackpack().getItems().get(0).getId());
        assertEquals(NpcHelpRequestOutcome.ACCEPTED,
                player.getEvolutionState().getNpcHelpOutcome("npc_test"));
        assertEquals(10, player.getEvolutionState().getAltruismScore());
        assertFalse(duplicate.isResolved());
        assertEquals(1, player.getBackpack().getSize());
    }

    @Test
    void refusingRemovesKarmaWithoutAddingReward() {
        Player player = new Player("Mamma", 100, 10, null);

        NpcHelpRequestResult result = service.resolve(player, createNpc(), NpcHelpRequestChoice.REFUSE);

        assertTrue(result.isResolved());
        assertEquals(-8, player.getKarma());
        assertEquals(0, player.getBackpack().getSize());
        assertEquals(NpcHelpRequestOutcome.REFUSED,
                player.getEvolutionState().getNpcHelpOutcome("npc_test"));
        assertEquals(4, player.getEvolutionState().getEgoismScore());
    }

    @Test
    void fullBackpackLeavesRequestPendingWithoutPartialEffects() {
        Player player = new Player("Mamma", 100, 10, null);
        Backpack backpack = new Backpack(1);
        backpack.addItem(new CollectibleFactory().requireCollectible("SMALL_BOMB"));
        player.setBackpack(backpack);

        NpcHelpRequestResult result = service.resolve(player, createNpc(), NpcHelpRequestChoice.ACCEPT);

        assertFalse(result.isResolved());
        assertTrue(result.getFeedback().contains("Zaino pieno"));
        assertEquals(0, player.getKarma());
        assertEquals(1, player.getBackpack().getSize());
        assertNull(player.getEvolutionState().getNpcHelpOutcome("npc_test"));
    }

    @Test
    void actualNpcCatalogHasValidHelpRequestsAndRewards() {
        service.validateConfiguration(new NpcFactory().getAllNpcs());
    }

    private Npc createNpc() {
        Npc npc = new Npc();
        npc.setId("npc_test");
        npc.setName("NPC Test");
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.QUEST_GIVER);
        npc.setRewardId("SMALL_POTION");
        npc.setAltruismReward(10);
        npc.setAltruismPenalty(-8);
        NpcHelpRequest request = new NpcHelpRequest();
        request.setText("Mi aiuti?");
        request.setAcceptedReply("Grazie!");
        request.setRefusedReply("Peccato.");
        npc.setHelpRequest(request);
        return npc;
    }
}
