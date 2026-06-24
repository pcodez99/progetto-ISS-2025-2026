package io.github.iss_2025_2026.model.combat;

/** Esito dell'uso di un oggetto durante il turno del giocatore. */
public enum ItemUseResult {
    USED(true, "Oggetto usato. Puoi ancora attaccare."),
    INVALID_TURN(false, "Puoi usare un oggetto soltanto durante il tuo turno."),
    INVALID_ITEM(false, "Oggetto non valido."),
    NOT_OWNED(false, "L'oggetto non e presente nello zaino."),
    ALREADY_USED(false, "Hai gia usato un oggetto in questo turno.");

    private final boolean success;
    private final String message;

    ItemUseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
