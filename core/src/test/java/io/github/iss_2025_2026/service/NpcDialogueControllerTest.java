package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iss_2025_2026.model.DialogueSession;
import io.github.iss_2025_2026.model.DialogueSessionState;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
import io.github.iss_2025_2026.model.NpcHelpRequest;
import io.github.iss_2025_2026.model.NpcType;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

public class NpcDialogueControllerTest {
    @Test
    public void openCreatesInputActiveSession() {
        NpcDialogueController controller = new NpcDialogueController();

        DialogueSession session = controller.open(createPlayer(), createNpc());

        assertTrue(controller.isActive());
        assertEquals(DialogueSessionState.INPUT_ACTIVE, session.getState());
        assertEquals("zia_pina", session.getNpc().getId());
    }

    @Test
    public void blankInputIsRejectedWithoutChangingState() {
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), createNpc());

        DialogueRequestContext context = controller.submitUserInput("   ");

        assertNull(context);
        assertEquals(DialogueSessionState.INPUT_ACTIVE, controller.getActiveSession().getState());
        assertTrue(controller.getStatusMessage().contains("Scrivi qualcosa"));
    }

    @Test
    public void submitMovesSessionToWaitingAndStoresPlayerTurn() {
        NpcDialogueController controller = new NpcDialogueController(30);
        controller.open(createPlayer(), createNpc());

        DialogueRequestContext context = controller.submitUserInput("  Ciao, mi aiuti?  ");

        assertNotNull(context);
        assertTrue(controller.isWaitingForAi());
        assertEquals("Ciao, mi aiuti?", context.getUserInput());
        assertEquals(1, context.getHistory().size());
        assertTrue(context.getHistory().get(0).isFromPlayer());
    }

    @Test
    public void completeResponseIgnoresStaleSessionIds() {
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), createNpc());
        DialogueRequestContext context = controller.submitUserInput("Ciao");
        controller.close();
        controller.open(createPlayer(), createNpc());

        boolean completed = controller.completeNpcResponse(context.getSessionId(), "Risposta vecchia");

        assertFalse(completed);
        assertEquals(0, controller.getActiveSession().getHistory().size());
    }

    @Test
    public void completeResponseAddsNpcTurnAndReactivatesInput() {
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), createNpc());
        DialogueRequestContext context = controller.submitUserInput("Ciao");

        boolean completed = controller.completeNpcResponse(context.getSessionId(), "Ti aiuto io.");

        assertTrue(completed);
        assertEquals(DialogueSessionState.INPUT_ACTIVE, controller.getActiveSession().getState());
        assertEquals(2, controller.getActiveSession().getHistory().size());
        assertFalse(controller.getActiveSession().getHistory().get(1).isFromPlayer());
    }

    @Test
    public void completeDecisionCanEndConversationAfterNpcReply() {
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), createNpc());
        DialogueRequestContext context = controller.submitUserInput("Vattene");

        boolean completed = controller.completeNpcDecision(context.getSessionId(),
                new NpcDialogueDecision("Bon, chiudiamola qui.", true, true, -4, null, "maltrattato"));

        assertTrue(completed);
        assertTrue(controller.isActive());
        assertTrue(controller.isEnded());
        assertEquals(DialogueSessionState.ENDED, controller.getActiveSession().getState());
        assertEquals("Dialogo concluso. ESC chiude.", controller.getStatusMessage());
        assertEquals(2, controller.getActiveSession().getHistory().size());
    }

    @Test
    public void configuredTurnMovesConversationToPendingHelpRequest() {
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), createNpcWithRequest());
        DialogueRequestContext context = controller.submitUserInput("Dimmi pure");

        boolean completed = controller.completeNpcResponse(context.getSessionId(), "Ho un problema.");

        assertTrue(completed);
        assertTrue(controller.isHelpRequestPending());
        assertEquals(DialogueSessionState.HELP_REQUEST_PENDING, controller.getActiveSession().getState());
        assertEquals(3, controller.getActiveSession().getHistory().size());
        assertEquals("Mi aiuti?", controller.getActiveSession().getHistory().get(2).getText());
    }

    @Test
    public void completedNpcRequestIsNotOfferedAgain() {
        Npc npc = createNpcWithRequest();
        Player player = createPlayer();
        player.getEvolutionState().recordNpcHelpOutcome(npc.getId(),
                io.github.iss_2025_2026.model.NpcHelpRequestOutcome.REFUSED);
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(player, npc);
        DialogueRequestContext context = controller.submitUserInput("Ciao");

        controller.completeNpcResponse(context.getSessionId(), "Bentornato.");

        assertEquals(DialogueSessionState.INPUT_ACTIVE, controller.getActiveSession().getState());
        assertFalse(controller.isHelpRequestPending());
    }

    @Test
    public void aiCannotEndConversationBeforeConfiguredRequestTurn() {
        Npc npc = createNpcWithRequest();
        NpcHelpRequest request = npc.getHelpRequest();
        request.setTriggerAfterNpcReplies(2);
        npc.setHelpRequest(request);
        NpcDialogueController controller = new NpcDialogueController();
        controller.open(createPlayer(), npc);

        DialogueRequestContext first = controller.submitUserInput("Primo turno");
        controller.completeNpcDecision(first.getSessionId(),
                new NpcDialogueDecision("Stavo per salutarti.", true, false, 0, null, ""));
        assertEquals(DialogueSessionState.INPUT_ACTIVE, controller.getActiveSession().getState());

        DialogueRequestContext second = controller.submitUserInput("Secondo turno");
        controller.completeNpcResponse(second.getSessionId(), "Aspetta, ho una richiesta.");
        assertTrue(controller.isHelpRequestPending());
    }

    private Player createPlayer() {
        return new Player("Mamma", 100, 10, null);
    }

    private Npc createNpc() {
        Npc npc = new Npc();
        npc.setId("zia_pina");
        npc.setName("Zia Pina");
        npc.setLevelId("level_1_campaign");
        npc.setType(NpcType.MERCHANT);
        npc.setSampleDialogue("Dialogo statico");
        return npc;
    }

    private Npc createNpcWithRequest() {
        Npc npc = createNpc();
        NpcHelpRequest request = new NpcHelpRequest();
        request.setTriggerAfterNpcReplies(1);
        request.setText("Mi aiuti?");
        request.setAcceptedReply("Grazie!");
        request.setRefusedReply("Peccato.");
        npc.setHelpRequest(request);
        return npc;
    }
}
