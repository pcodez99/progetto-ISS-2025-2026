package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.ai.AiClient;
import io.github.iss_2025_2026.service.ai.AiConfig;
import io.github.iss_2025_2026.service.ai.AiException;
import io.github.iss_2025_2026.service.ai.AiRequest;
import io.github.iss_2025_2026.service.ai.AiResponse;
import io.github.iss_2025_2026.service.ai.AiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NpcDialogueServiceTest {
    @AfterEach
    public void tearDown() {
        AiService.resetAi();
    }

    @Test
    public void getDialoguesUsesConfiguredAiService() {
        StubAiClient client = StubAiClient.success("Dialogo generato");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        List<String> dialogues = service.getDialogues(new Player("Mamma", 100, 10, null), createNpc(),
                Arrays.asList("Ciao", "Mi serve aiuto"));

        assertEquals(1, dialogues.size());
        assertEquals("Dialogo generato", dialogues.get(0));
        assertNotNull(client.lastRequest);
        assertTrue(client.lastRequest.getMessages().get(0).getContent().contains("Prompt base NPC"));
        assertTrue(client.lastRequest.getMessages().get(1).getContent().contains("Storia recente"));
    }

    @Test
    public void fallbackUsesSampleDialogueWhenAiFails() {
        AiService.setAi(new AiService(testConfig(true), StubAiClient.failure()));
        NpcDialogueService service = new NpcDialogueService();

        String dialogue = service.getOpeningDialogue(new Player("Mamma", 100, 10, null), createNpc());

        assertEquals("Dialogo statico", dialogue);
    }

    @Test
    public void reactionDialogueIncludesChoiceContext() {
        StubAiClient client = StubAiClient.success("Reazione");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        String dialogue = service.getReactionDialogue(new Player("Papa", 100, 10, null), createNpc(),
                ChoiceEventType.STEAL_ITEM);

        assertEquals("Reazione", dialogue);
        assertTrue(client.lastRequest.getMessages().get(1).getContent().contains("STEAL_ITEM"));
    }

    @Test
    public void dialogueResponseIncludesFreeUserInputAndRecentHistory() {
        StubAiClient client = StubAiClient.success("Risposta libera");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();
        Player player = new Player("Nonno", 120, 20, null);
        player.setCharacterId("nonno");

        String dialogue = service.getDialogueResponse(
                player,
                createNpc(),
                "Hai visto gli alieni?",
                Arrays.asList(
                        new DialogueTurn("Nonno", "Ciao Zia Pina", true),
                        new DialogueTurn("Zia Pina", "Prima salva i gallini.", false)));

        assertEquals("Risposta libera", dialogue);
        String prompt = client.lastRequest.getMessages().get(1).getContent();
        String systemPrompt = client.lastRequest.getMessages().get(0).getContent();
        assertTrue(systemPrompt.contains("Personaggio giocante con cui stai parlando: Nonno"));
        assertTrue(systemPrompt.contains("carrozzina"));
        assertTrue(systemPrompt.contains("giovanotto"));
        assertTrue(prompt.contains("Hai visto gli alieni?"));
        assertTrue(prompt.contains("Ciao Zia Pina"));
        assertTrue(prompt.contains("Prima salva i gallini."));
    }

    @Test
    public void dialogueDecisionParsesStructuredAiResponse() {
        StubAiClient client = StubAiClient.success("{"
                + "\"reply\":\"Ora basta, salutamu.\","
                + "\"endConversation\":true,"
                + "\"badBehavior\":true,"
                + "\"karmaDelta\":-6,"
                + "\"choiceEventType\":\"THREATEN\","
                + "\"reason\":\"Il giocatore ha minacciato l'NPC\""
                + "}");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        NpcDialogueDecision decision = service.getDialogueDecision(
                new Player("Mamma", 100, 10, null),
                createNpc(),
                "Dammi tutto o ti rompo il banco.",
                Arrays.<DialogueTurn>asList());

        assertEquals("Ora basta, salutamu.", decision.getReply());
        assertTrue(decision.isEndConversation());
        assertTrue(decision.isBadBehavior());
        assertEquals(-6, decision.getKarmaDelta());
        assertEquals(ChoiceEventType.THREATEN, decision.getChoiceEventType());
        String prompt = client.lastRequest.getMessages().get(1).getContent();
        assertTrue(prompt.contains("Rispondi SOLO con JSON valido"));
        assertTrue(prompt.contains("Eventi consentiti"));
    }

    @Test
    public void dialogueDecisionRepairsUnquotedChoiceEventType() {
        StubAiClient client = StubAiClient.success("{"
                + "\"reply\":\"Strega? Ah, lu capu t'e rimastu a casa, vecchio iettu!\","
                + "\"endConversation\":true,"
                + "\"badBehavior\":true,"
                + "\"karmaDelta\":-5,"
                + "\"choiceEventType\":THREATEN,"
                + "\"reason\":\"Il giocatore continua con atteggiamento ostile\""
                + "}");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        NpcDialogueDecision decision = service.getDialogueDecision(
                new Player("Nonno", 120, 20, null),
                createNpc(),
                "Strega!",
                Arrays.<DialogueTurn>asList());

        assertEquals("Strega? Ah, lu capu t'e rimastu a casa, vecchio iettu!", decision.getReply());
        assertTrue(decision.isEndConversation());
        assertTrue(decision.isBadBehavior());
        assertEquals(-5, decision.getKarmaDelta());
        assertEquals(ChoiceEventType.THREATEN, decision.getChoiceEventType());
    }

    @Test
    public void dialogueDecisionSanitizesMalformedStructuredOutput() {
        StubAiClient client = StubAiClient.success("{"
                + "\"reply\":\"Chiudiamola qui.\","
                + "\"endConversation\":true,"
                + "\"badBehavior\":true,"
                + "\"karmaDelta\":-4,"
                + "\"choiceEventType\":BROKEN_EVENT,"
                + "broken");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        NpcDialogueDecision decision = service.getDialogueDecision(
                new Player("Papa", 100, 10, null),
                createNpc(),
                "Insulto",
                Arrays.<DialogueTurn>asList());

        assertEquals("Chiudiamola qui.", decision.getReply());
        assertTrue(decision.isEndConversation());
        assertTrue(decision.isBadBehavior());
        assertEquals(-4, decision.getKarmaDelta());
        assertNull(decision.getChoiceEventType());
        assertFalse(decision.getReply().contains("{"));
        assertFalse(decision.getReply().contains("choiceEventType"));
    }

    @Test
    public void dialogueDecisionFallsBackToPlainTextWhenJsonIsMissing() {
        StubAiClient client = StubAiClient.success("Va bene, parliamone con calma.");
        AiService.setAi(new AiService(testConfig(true), client));
        NpcDialogueService service = new NpcDialogueService();

        NpcDialogueDecision decision = service.getDialogueDecision(
                new Player("Papa", 100, 10, null),
                createNpc(),
                "Ciao",
                Arrays.<DialogueTurn>asList());

        assertEquals("Va bene, parliamone con calma.", decision.getReply());
        assertFalse(decision.isEndConversation());
        assertFalse(decision.isBadBehavior());
        assertEquals(0, decision.getKarmaDelta());
        assertNull(decision.getChoiceEventType());
    }

    private AiConfig testConfig(boolean fallback) {
        return new AiConfig(true, "http://localhost:11434", "test-model", "/api/chat", false,
                10, 10, 0.75f, 4000, fallback);
    }

    private Npc createNpc() {
        Npc npc = new Npc();
        npc.setId("zia_pina");
        npc.setName("Zia Pina");
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.MERCHANT);
        npc.setDialoguePrompt("Prompt base NPC");
        npc.setSampleDialogue("Dialogo statico");
        npc.setInteractionOptions(Arrays.asList("Aiutare", "Rubare"));
        return npc;
    }

    private static class StubAiClient implements AiClient {
        private final String content;
        private final boolean fail;
        private AiRequest lastRequest;

        private StubAiClient(String content, boolean fail) {
            this.content = content;
            this.fail = fail;
        }

        private static StubAiClient success(String content) {
            return new StubAiClient(content, false);
        }

        private static StubAiClient failure() {
            return new StubAiClient(null, true);
        }

        @Override
        public AiResponse chat(AiRequest request, AiConfig config) throws AiException {
            this.lastRequest = request.withDefaults(config);
            if (fail) {
                throw new AiException("errore finto");
            }
            return new AiResponse(content, config.getModel(), "{}");
        }

        @Override
        public boolean isAvailable(AiConfig config) {
            return !fail;
        }
    }
}
