package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.factory.DialogueProfileFactory;
import io.github.iss_2025_2026.model.DialogueProfile;
import io.github.iss_2025_2026.model.DialoguePrompt;
import io.github.iss_2025_2026.model.EvolutionPath;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.Player;

public class DialogueProfileService {
    private final DialogueProfileFactory profileFactory;

    public DialogueProfileService() {
        this(new DialogueProfileFactory());
    }

    public DialogueProfileService(DialogueProfileFactory profileFactory) {
        this.profileFactory = profileFactory;
    }

    public DialoguePrompt buildPrompt(Player player, Npc npc) {
        EvolutionPath path = player != null ? player.getEvolutionState().getDominantPath() : EvolutionPath.BALANCED;
        DialogueProfile profile = profileFactory.getProfile(path);
        String basePrompt = npc != null && npc.getDialoguePrompt() != null ? npc.getDialoguePrompt() : "";
        String prompt = basePrompt + "\n\nProfilo del giocatore: " + path
                + ". Tono richiesto: " + profile.getTone()
                + ". " + safe(profile.getPromptSuffix());
        return new DialoguePrompt(prompt.trim(), profile.getTemperature(), path);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
