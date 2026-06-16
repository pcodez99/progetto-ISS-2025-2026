package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

public class NpcInteractionServiceTest {
    private static final float INTERACTION_RADIUS = 120f;

    @Test
    public void playerInsideRadiusTriggersNpcInteraction() {
        Player player = new Player("Mamma", 100, 10, null);
        player.setX(500f);
        player.setY(500f);
        NpcInteractionService service = NpcInteractionService.forTesting(
                500f, 500f, createNpc("zio_toto"), INTERACTION_RADIUS);

        NpcInteraction interaction = service.checkInteraction(player);

        assertNotNull(interaction);
        assertEquals("zio_toto", interaction.getNpc().getId());
    }

    @Test
    public void playerOutsideRadiusDoesNotTriggerNpcInteraction() {
        Player player = new Player("Mamma", 100, 10, null);
        player.setX(0f);
        player.setY(0f);
        NpcInteractionService service = NpcInteractionService.forTesting(
                500f, 500f, createNpc("zia_pina"), INTERACTION_RADIUS);

        assertNull(service.checkInteraction(player));
    }

    @Test
    public void levelCatalogIdUsesCampaignConvention() {
        assertEquals("level_1_campaign", NpcInteractionService.levelCatalogId(1));
        assertEquals("level_2_campaign", NpcInteractionService.levelCatalogId(2));
    }

    private Npc createNpc(String id) {
        Npc npc = new Npc();
        npc.setId(id);
        npc.setName(id);
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.QUEST_GIVER);
        return npc;
    }
}
