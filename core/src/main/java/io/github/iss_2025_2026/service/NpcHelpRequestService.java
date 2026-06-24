package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.factory.CollectibleFactory;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.Collectible;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcHelpRequest;
import io.github.iss_2025_2026.model.NpcHelpRequestChoice;
import io.github.iss_2025_2026.model.NpcHelpRequestOutcome;
import io.github.iss_2025_2026.model.NpcHelpRequestResult;
import io.github.iss_2025_2026.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestra gli effetti deterministici della scelta di aiuto: inventario,
 * karma, evoluzione morale e protezione dalle ricompense duplicate.
 */
public class NpcHelpRequestService {
    private final CollectibleFactory collectibleFactory;
    private final EvolutionService evolutionService;

    public NpcHelpRequestService() {
        this(new CollectibleFactory(), new EvolutionService());
    }

    public NpcHelpRequestService(CollectibleFactory collectibleFactory, EvolutionService evolutionService) {
        if (collectibleFactory == null || evolutionService == null) {
            throw new IllegalArgumentException("Factory collectible ed EvolutionService sono obbligatori.");
        }
        this.collectibleFactory = collectibleFactory;
        this.evolutionService = evolutionService;
    }

    /** Valida in anticipo tutti i riferimenti incrociati NPC -> collectible. */
    public void validateConfiguration(List<Npc> npcs) {
        List<String> errors = new ArrayList<>();
        if (npcs == null || npcs.isEmpty()) {
            errors.add("catalogo NPC vuoto");
        } else {
            for (Npc npc : npcs) {
                validateNpc(npc, errors);
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configurazione richieste NPC non valida: " + String.join("; ", errors));
        }
    }

    public NpcHelpRequestResult resolve(Player player, Npc npc, NpcHelpRequestChoice choice) {
        if (player == null || npc == null || choice == null) {
            return NpcHelpRequestResult.failure("Impossibile risolvere la richiesta: dati mancanti.");
        }
        NpcHelpRequest request = npc.getHelpRequest();
        if (request == null) {
            return NpcHelpRequestResult.failure("Questo NPC non ha una richiesta configurata.");
        }
        if (player.getEvolutionState().hasNpcHelpOutcome(npc.getId())) {
            return NpcHelpRequestResult.failure("Hai già risposto alla richiesta di questo NPC.");
        }

        if (choice == NpcHelpRequestChoice.ACCEPT) {
            return accept(player, npc, request);
        }
        return refuse(player, npc, request);
    }

    private NpcHelpRequestResult accept(Player player, Npc npc, NpcHelpRequest request) {
        Optional<Collectible> configuredReward = collectibleFactory.findCollectible(npc.getRewardId());
        if (!configuredReward.isPresent()) {
            return NpcHelpRequestResult.failure("Ricompensa NPC non disponibile: " + npc.getRewardId());
        }

        Backpack backpack = player.getBackpack();
        if (backpack == null || backpack.getSize() >= backpack.getCapacity()) {
            return NpcHelpRequestResult.failure("Zaino pieno: libera uno spazio prima di accettare.");
        }

        Collectible reward = configuredReward.get();
        if (!backpack.addItem(reward)) {
            return NpcHelpRequestResult.failure("Non è stato possibile aggiungere la ricompensa allo zaino.");
        }

        int previousKarma = player.getKarma();
        player.modifyKarma(npc.getAltruismReward());
        evolutionService.applyNpcChoice(player, npc, ChoiceEventType.HELP_NPC);
        player.getEvolutionState().recordNpcHelpOutcome(npc.getId(), NpcHelpRequestOutcome.ACCEPTED);

        return NpcHelpRequestResult.accepted(request.getAcceptedReply(), player.getKarma() - previousKarma,
                reward.getId(), reward.getName());
    }

    private NpcHelpRequestResult refuse(Player player, Npc npc, NpcHelpRequest request) {
        int previousKarma = player.getKarma();
        player.modifyKarma(npc.getAltruismPenalty());
        evolutionService.applyNpcChoice(player, npc, ChoiceEventType.IGNORE_REQUEST);
        player.getEvolutionState().recordNpcHelpOutcome(npc.getId(), NpcHelpRequestOutcome.REFUSED);

        return NpcHelpRequestResult.refused(request.getRefusedReply(), player.getKarma() - previousKarma);
    }

    private void validateNpc(Npc npc, List<String> errors) {
        if (npc == null || isBlank(npc.getId())) {
            errors.add("NPC nullo o senza id");
            return;
        }
        NpcHelpRequest request = npc.getHelpRequest();
        if (request == null) {
            errors.add(npc.getId() + ": helpRequest mancante");
            return;
        }
        if (isBlank(request.getText()) || isBlank(request.getAcceptedReply()) || isBlank(request.getRefusedReply())) {
            errors.add(npc.getId() + ": testi helpRequest incompleti");
        }
        if (npc.getAltruismReward() <= 0 || npc.getAltruismPenalty() >= 0) {
            errors.add(npc.getId() + ": delta karma devono essere positivo/negativo");
        }
        if (isBlank(npc.getRewardId()) || !collectibleFactory.findCollectible(npc.getRewardId()).isPresent()) {
            errors.add(npc.getId() + ": rewardId inesistente " + npc.getRewardId());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
