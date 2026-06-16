package io.github.iss_2025_2026.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.iss_2025_2026.model.ChoiceEventType;
import io.github.iss_2025_2026.model.DialoguePrompt;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.ai.AiException;
import io.github.iss_2025_2026.service.ai.AiMessage;
import io.github.iss_2025_2026.service.ai.AiRequest;
import io.github.iss_2025_2026.service.ai.AiResponse;
import io.github.iss_2025_2026.service.ai.AiService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcDialogueService {
    private static final Logger LOGGER = Logger.getLogger(NpcDialogueService.class.getName());
    private static final int LOG_TEXT_LIMIT = 4000;
    private static final String NO_CHOICE_EVENT = "NONE";
    private static final Pattern CHOICE_EVENT_FIELD = Pattern.compile(
            "(\"choiceEventType\"\\s*:\\s*)([A-Za-z_][A-Za-z0-9_]*)");

    private final DialogueProfileService dialogueProfileService;
    private final ObjectMapper mapper = new ObjectMapper();

    public NpcDialogueService() {
        this(new DialogueProfileService());
    }

    public NpcDialogueService(DialogueProfileService dialogueProfileService) {
        this.dialogueProfileService = dialogueProfileService;
    }

    public List<String> getDialogues(Player player, Npc npc) {
        return getDialogues(player, npc, Collections.<String>emptyList());
    }

    public List<String> getDialogues(Player player, Npc npc, List<String> history) {
        String dialogue = requestDialogue(player, npc, buildOpeningUserPrompt(npc, history));
        return Collections.singletonList(dialogue);
    }

    public String getOpeningDialogue(Player player, Npc npc) {
        return requestDialogue(player, npc, buildOpeningUserPrompt(npc, Collections.<String>emptyList()));
    }

    public String getDialogueResponse(Player player, Npc npc, String userInput, List<DialogueTurn> history) {
        return requestDialogue(player, npc, buildInteractiveUserPrompt(npc, userInput, history));
    }

    public NpcDialogueDecision getDialogueDecision(Player player, Npc npc, String userInput,
            List<DialogueTurn> history) {
        String userPrompt = buildInteractiveDecisionPrompt(npc, userInput, history);
        String rawResponse = requestDialogue(player, npc, userPrompt);
        NpcDialogueDecision decision = parseDecision(rawResponse);
        LOGGER.info("[NPC AI] Decisione dialogo: reply=" + truncateForLog(decision.getReply())
                + ", endConversation=" + decision.isEndConversation()
                + ", badBehavior=" + decision.isBadBehavior()
                + ", karmaDelta=" + decision.getKarmaDelta()
                + ", choiceEventType=" + decision.getChoiceEventType()
                + ", reason=" + truncateForLog(decision.getReason()));
        return decision;
    }

    public String getReactionDialogue(Player player, Npc npc, ChoiceEventType choiceType) {
        String choice = choiceType != null ? choiceType.name() : "UNKNOWN";
        String prompt = "Il giocatore ha appena compiuto questa scelta: " + choice
                + ". Rispondi come l'NPC, con una battuta breve e coerente.";
        return requestDialogue(player, npc, prompt);
    }

    private String requestDialogue(Player player, Npc npc, String userPrompt) {
        DialoguePrompt dialoguePrompt = dialogueProfileService.buildPrompt(player, npc);
        AiRequest request = new AiRequest(
                null,
                buildMessages(dialoguePrompt, userPrompt),
                dialoguePrompt.getTemperature(),
                null);

        LOGGER.info("[NPC AI] Richiesta dialogo: player=" + safePlayerName(player)
                + ", npc=" + safeNpcId(npc)
                + ", temperature=" + dialoguePrompt.getTemperature());
        LOGGER.info("[NPC AI] System prompt: " + truncateForLog(dialoguePrompt.getPrompt()));
        LOGGER.info("[NPC AI] User prompt: " + truncateForLog(userPrompt));
        try {
            AiResponse response = AiService.getAi().chat(request);
            LOGGER.info("[NPC AI] Raw model response: " + truncateForLog(response.getRawResponse()));
            LOGGER.info("[NPC AI] Model content: " + truncateForLog(response.getContent()));
            if (response.getContent() != null && !response.getContent().trim().isEmpty()) {
                return response.getContent().trim();
            }
            LOGGER.warning("[NPC AI] Risposta AI vuota. Uso fallback statico.");
        } catch (AiException exception) {
            LOGGER.log(Level.WARNING, "[NPC AI] Errore AI durante il dialogo. Fallback statico="
                    + AiService.getAi().getConfig().isFallbackToStaticDialogue(), exception);
            if (!AiService.getAi().getConfig().isFallbackToStaticDialogue()) {
                return "Il collegamento con l'IA locale non e disponibile.";
            }
        }
        String fallback = fallbackDialogue(npc);
        LOGGER.info("[NPC AI] Fallback dialogue: " + truncateForLog(fallback));
        return fallback;
    }

    private List<AiMessage> buildMessages(DialoguePrompt dialoguePrompt, String userPrompt) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(dialoguePrompt.getPrompt()));
        messages.add(AiMessage.user(userPrompt));
        return messages;
    }

    private String buildOpeningUserPrompt(Npc npc, List<String> history) {
        StringBuilder builder = new StringBuilder();
        builder.append("Genera il prossimo dialogo dell'NPC");
        if (npc != null && npc.getName() != null) {
            builder.append(" ").append(npc.getName());
        }
        builder.append(". La risposta deve essere breve, naturale e in personaggio.");

        if (history != null && !history.isEmpty()) {
            builder.append("\nStoria recente del dialogo:");
            for (String line : history) {
                if (line != null && !line.trim().isEmpty()) {
                    builder.append("\n- ").append(line.trim());
                }
            }
        }
        if (npc != null && !npc.getInteractionOptions().isEmpty()) {
            builder.append("\nOpzioni disponibili:");
            for (String option : npc.getInteractionOptions()) {
                builder.append("\n- ").append(option);
            }
        }
        return builder.toString();
    }

    private String buildInteractiveUserPrompt(Npc npc, String userInput, List<DialogueTurn> history) {
        StringBuilder builder = new StringBuilder();
        builder.append("Continua la conversazione rispondendo solo come NPC");
        if (npc != null && npc.getName() != null) {
            builder.append(" ").append(npc.getName());
        }
        builder.append(". Usa una risposta breve, naturale e coerente con il personaggio.");

        if (history != null && !history.isEmpty()) {
            builder.append("\nStoria recente:");
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                DialogueTurn turn = history.get(i);
                if (turn == null || turn.getText() == null || turn.getText().trim().isEmpty()) {
                    continue;
                }
                String speaker = turn.isFromPlayer() ? "Giocatore" : "NPC";
                if (turn.getSpeaker() != null && !turn.getSpeaker().trim().isEmpty()) {
                    speaker = turn.getSpeaker().trim();
                }
                builder.append("\n- ").append(speaker).append(": ").append(turn.getText().trim());
            }
        }

        builder.append("\nIl giocatore ora dice: ");
        builder.append(userInput != null ? userInput.trim() : "");
        builder.append("\nRispondi senza descrivere azioni fuori dialogo.");
        return builder.toString();
    }

    private String buildInteractiveDecisionPrompt(Npc npc, String userInput, List<DialogueTurn> history) {
        StringBuilder builder = new StringBuilder(buildInteractiveUserPrompt(npc, userInput, history));
        builder.append("\n\nValuta anche gli effetti della risposta sul sistema di gioco.");
        builder.append("\nPuoi concludere la conversazione quando sarebbe naturale per l'NPC: in quel caso ");
        builder.append("metti endConversation=true e fai salutare l'NPC nella reply.");
        builder.append("\nSe il giocatore insulta, minaccia, tenta di rubare, mente o molesta l'NPC, ");
        builder.append("metti badBehavior=true, usa karmaDelta negativo e scegli l'evento coerente.");
        builder.append("\nSe il giocatore aiuta, completa una richiesta o si redime, usa karmaDelta positivo ");
        builder.append("e scegli l'evento coerente.");
        builder.append("\nSe il comportamento e neutro, usa karmaDelta=0 e choiceEventType=null.");
        builder.append("\nIl karmaDelta deve stare tra ")
                .append(NpcDialogueDecision.MIN_KARMA_DELTA)
                .append(" e ")
                .append(NpcDialogueDecision.MAX_KARMA_DELTA)
                .append(".");
        if (npc != null) {
            builder.append("\nDati NPC utili: canBecomeHostile=").append(npc.canBecomeHostile());
            if (npc.getNegativeConsequence() != null && !npc.getNegativeConsequence().trim().isEmpty()) {
                builder.append(", negativeConsequence=").append(npc.getNegativeConsequence().trim());
            }
            builder.append(", altruismReward=").append(npc.getAltruismReward());
            builder.append(", altruismPenalty=").append(npc.getAltruismPenalty());
        }
        builder.append("\nEventi consentiti per choiceEventType: ");
        builder.append(NO_CHOICE_EVENT);
        for (ChoiceEventType type : ChoiceEventType.values()) {
            builder.append(", ").append(type.name());
        }
        builder.append(".");
        builder.append("\nchoiceEventType deve essere null oppure una stringa JSON tra virgolette, ad esempio ");
        builder.append("\"THREATEN\". Non scrivere mai enum non quotati.");
        builder.append("\nRispondi SOLO con JSON valido, senza markdown e senza testo fuori dal JSON:");
        builder.append("\n{\"reply\":\"frase breve dell'NPC\",");
        builder.append("\"endConversation\":false,");
        builder.append("\"badBehavior\":false,");
        builder.append("\"karmaDelta\":0,");
        builder.append("\"choiceEventType\":null,");
        builder.append("\"reason\":\"motivo breve per log\"}");
        return builder.toString();
    }

    private NpcDialogueDecision parseDecision(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return NpcDialogueDecision.neutral("Non so cosa rispondere.");
        }

        String raw = rawResponse.trim();
        String json = extractJsonObject(raw);
        try {
            return parseDecisionJson(json, raw);
        } catch (Exception exception) {
            String repairedJson = repairDecisionJson(json);
            if (!repairedJson.equals(json)) {
                try {
                    LOGGER.warning("[NPC AI] Decisione JSON riparata prima del parsing. Raw="
                            + truncateForLog(raw));
                    return parseDecisionJson(repairedJson, raw);
                } catch (Exception repairedException) {
                    LOGGER.log(Level.WARNING, "[NPC AI] Decisione JSON riparata ancora non valida. Raw="
                            + truncateForLog(raw), repairedException);
                }
            } else {
                LOGGER.log(Level.WARNING, "[NPC AI] Decisione non in JSON valido. Raw="
                        + truncateForLog(raw), exception);
            }

            LOGGER.log(Level.WARNING,
                    "[NPC AI] Uso fallback sanitizzato per non mostrare JSON strutturato al giocatore. Raw="
                            + truncateForLog(raw),
                    exception);
            return parseMalformedDecision(raw);
        }
    }

    private NpcDialogueDecision parseDecisionJson(String json, String fallbackReply) throws Exception {
        JsonNode root = mapper.readTree(json);
        String reply = textOrDefault(root, "reply", fallbackReply);
        boolean endConversation = root.path("endConversation").asBoolean(false);
        boolean badBehavior = root.path("badBehavior").asBoolean(false);
        int karmaDelta = root.path("karmaDelta").asInt(0);
        ChoiceEventType choiceEventType = parseChoiceEvent(root.get("choiceEventType"));
        String reason = textOrDefault(root, "reason", "");
        return new NpcDialogueDecision(reply, endConversation, badBehavior, karmaDelta, choiceEventType, reason);
    }

    private String repairDecisionJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        Matcher matcher = CHOICE_EVENT_FIELD.matcher(json);
        StringBuffer repaired = new StringBuffer();
        while (matcher.find()) {
            String value = matcher.group(2);
            String replacement;
            if ("null".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
                    || "false".equalsIgnoreCase(value)) {
                replacement = matcher.group(1) + value;
            } else {
                replacement = matcher.group(1) + "\"" + value + "\"";
            }
            matcher.appendReplacement(repaired, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(repaired);
        return repaired.toString();
    }

    private NpcDialogueDecision parseMalformedDecision(String raw) {
        String reply = extractStringField(raw, "reply");
        boolean structured = looksLikeStructuredDecision(raw);
        if (reply == null || reply.trim().isEmpty()) {
            reply = structured ? "Non so cosa rispondere." : stripCodeFence(raw);
        }
        boolean endConversation = extractBooleanField(raw, "endConversation", false);
        boolean badBehavior = extractBooleanField(raw, "badBehavior", false);
        int karmaDelta = extractIntField(raw, "karmaDelta", 0);
        ChoiceEventType choiceEventType = parseChoiceEventTextOrNull(extractChoiceEventText(raw));
        String reason = extractStringField(raw, "reason");
        return new NpcDialogueDecision(reply, endConversation, badBehavior, karmaDelta, choiceEventType, reason);
    }

    private String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private boolean looksLikeStructuredDecision(String raw) {
        if (raw == null) {
            return false;
        }
        return raw.contains("\"reply\"") || raw.contains("\"endConversation\"")
                || raw.contains("\"badBehavior\"") || raw.contains("\"karmaDelta\"")
                || raw.contains("\"choiceEventType\"");
    }

    private String stripCodeFence(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            int fenceEnd = text.lastIndexOf("```");
            if (firstLineEnd >= 0 && fenceEnd > firstLineEnd) {
                return text.substring(firstLineEnd + 1, fenceEnd).trim();
            }
        }
        return text;
    }

    private String extractStringField(String raw, String field) {
        if (raw == null || field == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            return null;
        }
        String escaped = matcher.group(1);
        try {
            return mapper.readValue("\"" + escaped + "\"", String.class);
        } catch (Exception exception) {
            return escaped;
        }
    }

    private boolean extractBooleanField(String raw, String field, boolean defaultValue) {
        String value = extractScalarField(raw, field);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || ("false".equalsIgnoreCase(value) ? false : defaultValue);
    }

    private int extractIntField(String raw, String field, int defaultValue) {
        String value = extractScalarField(raw, field);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String extractChoiceEventText(String raw) {
        if (raw == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"choiceEventType\"\\s*:\\s*(?:\"([^\"]*)\"|([A-Za-z_][A-Za-z0-9_]*))");
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            return null;
        }
        String quoted = matcher.group(1);
        return quoted != null ? quoted : matcher.group(2);
    }

    private String extractScalarField(String raw, String field) {
        if (raw == null || field == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field)
                + "\"\\s*:\\s*(-?\\d+|true|false|null)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(raw);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String textOrDefault(JsonNode root, String field, String defaultValue) {
        JsonNode value = root != null ? root.get(field) : null;
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        String text = value.asText();
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        return text.trim();
    }

    private ChoiceEventType parseChoiceEvent(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        String name = value.asText();
        if (name == null || name.trim().isEmpty() || NO_CHOICE_EVENT.equalsIgnoreCase(name.trim())) {
            return null;
        }
        try {
            return parseChoiceEventText(name);
        } catch (IllegalArgumentException exception) {
            LOGGER.warning("[NPC AI] choiceEventType non valido ignorato: " + name);
            return null;
        }
    }

    private ChoiceEventType parseChoiceEventText(String name) {
        if (name == null || name.trim().isEmpty() || NO_CHOICE_EVENT.equalsIgnoreCase(name.trim())
                || "null".equalsIgnoreCase(name.trim())) {
            return null;
        }
        return ChoiceEventType.valueOf(name.trim().toUpperCase());
    }

    private ChoiceEventType parseChoiceEventTextOrNull(String name) {
        try {
            return parseChoiceEventText(name);
        } catch (IllegalArgumentException exception) {
            LOGGER.warning("[NPC AI] choiceEventType malformato ignorato nel fallback: " + name);
            return null;
        }
    }

    private String fallbackDialogue(Npc npc) {
        if (npc != null && npc.getSampleDialogue() != null && !npc.getSampleDialogue().trim().isEmpty()) {
            return npc.getSampleDialogue();
        }
        if (npc != null && npc.getName() != null && !npc.getName().trim().isEmpty()) {
            return npc.getName() + " ti osserva in silenzio.";
        }
        return "L'NPC resta in silenzio.";
    }

    private static String safePlayerName(Player player) {
        return player != null && player.getName() != null ? player.getName() : "unknown";
    }

    private static String safeNpcId(Npc npc) {
        if (npc == null) {
            return "unknown";
        }
        String id = npc.getId() != null ? npc.getId() : "no-id";
        String name = npc.getName() != null ? npc.getName() : "no-name";
        return id + " (" + name + ")";
    }

    private static String truncateForLog(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= LOG_TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, LOG_TEXT_LIMIT) + "... [truncated, length=" + text.length() + "]";
    }
}
