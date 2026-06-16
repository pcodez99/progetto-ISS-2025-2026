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
        String prompt = basePrompt + "\n\n" + buildPlayerContext(player)
                + "\nProfilo del giocatore: " + path
                + ". Tono richiesto: " + profile.getTone()
                + ". " + safe(profile.getPromptSuffix());
        return new DialoguePrompt(prompt.trim(), profile.getTemperature(), path);
    }

    private String buildPlayerContext(Player player) {
        if (player == null) {
            return "Personaggio giocante: sconosciuto. Evita appellativi specifici su eta, ruolo o aspetto.";
        }

        String characterId = safe(player.getCharacterId()).trim();
        String playerName = safe(player.getName()).trim();
        if (playerName.isEmpty()) {
            playerName = !characterId.isEmpty() ? characterId : "giocatore";
        }

        return "Personaggio giocante con cui stai parlando: " + playerName
                + " (characterId=" + (!characterId.isEmpty() ? characterId : "unknown") + "). "
                + playerDescription(characterId)
                + " Adegua appellativi, tono e riferimenti al personaggio: non attribuire eta, corpo o ruolo sbagliati.";
    }

    private String playerDescription(String characterId) {
        if ("nonno".equals(characterId)) {
            return "E' il Nonno, un anziano viddano in carrozzina, resistente e testardo; non chiamarlo giovane, ragazzo o giovanotto.";
        }
        if ("papa".equals(characterId)) {
            return "E' il Papa, un adulto solido e protettivo; trattalo come un padre di famiglia, non come un bambino.";
        }
        if ("mamma".equals(characterId)) {
            return "E' la Mamma, adulta pragmatica e protettiva; rivolgiti a lei rispettando il suo ruolo familiare.";
        }
        if ("bambino".equals(characterId)) {
            return "E' il Bambino, giovane e rapido; puoi usare appellativi adatti a un ragazzino senza esagerare.";
        }
        return "Il suo tipo preciso non e noto; usa appellativi neutrali.";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
