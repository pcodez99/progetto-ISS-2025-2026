package io.github.iss_2025_2026.model;

import io.github.iss_2025_2026.factory.NpcFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NpcFactoryTest {
    private NpcFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new NpcFactory();
    }

    @Test
    public void testCaricamentoZioToto() {
        Npc npc = factory.getNpc("zio_toto");

        assertNotNull(npc);
        assertEquals("zio_toto", npc.getId());
        assertEquals("Zio Toto u Tratturista", npc.getName());
        assertEquals("level_1_campaign", npc.getLevelId());
        assertEquals(NpcType.QUEST_GIVER, npc.getType());
        assertEquals("STRENGTH_ELIXIR", npc.getRewardId());
        assertEquals(10, npc.getAltruismReward());
        assertEquals(-15, npc.getAltruismPenalty());
        assertNotNull(npc.getHelpRequest());
        assertEquals(1, npc.getHelpRequest().getTriggerAfterNpcReplies());
    }

    @Test
    public void testCatalogoCompletoContieneNpcAggiornati() {
        List<Npc> npcs = factory.getAllNpcs();

        assertEquals(4, npcs.size());
        assertNotNull(factory.getNpc("zyrko_ingegnere_alieno"));
        assertNotNull(factory.getNpc("turiddu_spaventapasseri"));
        assertTrue(npcs.stream().allMatch(npc -> npc.getHelpRequest() != null));
    }

    @Test
    public void testNpcPerLivelloUsaCatalogoCorrente() {
        List<Npc> levelOneNpcs = factory.getNpcsForLevel("level_1_campaign");

        assertEquals(2, levelOneNpcs.size());
        assertTrue(containsNpc(levelOneNpcs, "zio_toto"));
        assertFalse(containsNpc(levelOneNpcs, "zyrko_ingegnere_alieno"));
    }

    @Test
    public void testNpcPerLivelloMantieneOrdineYaml() {
        List<Npc> levelOneNpcs = factory.getNpcsForLevel("level_1_campaign");

        assertEquals(2, levelOneNpcs.size());
        assertEquals("zio_toto", levelOneNpcs.get(0).getId());
        assertEquals("zia_pina", levelOneNpcs.get(1).getId());

        List<Npc> levelTwoNpcs = factory.getNpcsForLevel("level_2_campaign");
        assertEquals(2, levelTwoNpcs.size());
        assertEquals("zyrko_ingegnere_alieno", levelTwoNpcs.get(0).getId());
        assertEquals("turiddu_spaventapasseri", levelTwoNpcs.get(1).getId());
    }

    @Test
    public void testFactoryRestituisceCopieIndipendenti() {
        Npc first = factory.getNpc("zia_pina");
        Npc second = factory.getNpc("zia_pina");

        assertNotSame(first, second);
        first.setName("Nome modificato");

        assertEquals("Zia Pina a Cannolara", second.getName());
        assertEquals("Zia Pina a Cannolara", factory.getNpc("zia_pina").getName());
    }

    @Test
    public void testComportamentoConIdInesistente() {
        assertNull(factory.getNpc("npc_inventato"));
        assertFalse(factory.findNpc("npc_inventato").isPresent());
    }

    private boolean containsNpc(List<Npc> npcs, String id) {
        for (Npc npc : npcs) {
            if (id.equals(npc.getId())) {
                return true;
            }
        }
        return false;
    }
}
