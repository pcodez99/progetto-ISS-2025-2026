package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.DialoguePrompt;
import io.github.iss_2025_2026.model.EvolutionPath;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DialogueProfileServiceTest {
    private final DialogueProfileService service = new DialogueProfileService();

    @Test
    public void altruisticPlayerGetsLowerTemperaturePrompt() {
        Player player = new Player("Mamma", 100, 10, null);
        player.getEvolutionState().addAltruism(30);
        Npc npc = createNpc();

        DialoguePrompt prompt = service.buildPrompt(player, npc);

        assertEquals(EvolutionPath.ALTRUISTIC, prompt.getPath());
        assertEquals(0.55f, prompt.getTemperature(), 0.001f);
        assertTrue(prompt.getPrompt().contains("fiducioso"));
        assertTrue(prompt.getPrompt().contains("Prompt base NPC"));
    }

    @Test
    public void egoisticPlayerGetsHigherTemperaturePrompt() {
        Player player = new Player("Papa", 100, 10, null);
        player.getEvolutionState().addEgoism(30);

        DialoguePrompt prompt = service.buildPrompt(player, createNpc());

        assertEquals(EvolutionPath.EGOISTIC, prompt.getPath());
        assertEquals(0.95f, prompt.getTemperature(), 0.001f);
        assertTrue(prompt.getPrompt().contains("diffidente"));
    }

    private Npc createNpc() {
        Npc npc = new Npc();
        npc.setId("zia_pina");
        npc.setName("Zia Pina");
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.MERCHANT);
        npc.setDialoguePrompt("Prompt base NPC");
        return npc;
    }
}
