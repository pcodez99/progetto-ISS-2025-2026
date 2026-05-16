package io.github.iss_2025_2026.model;

/**
 * Game Model (Parte del pattern MVC).
 * Rappresenta lo stato del gioco e la logica di business.
 * Non contiene riferimenti a LibGDX per il rendering o l'input.
 */
public class GameModel {
    private String message;
    private float timer;

    public GameModel() {
        this.message = "Viddani VS Alieni - MVC Base Architecture";
        this.timer = 0;
    }

    public void update(float delta) {
        timer += delta;
    }

    public String getMessage() {
        return message;
    }

    public float getTimer() {
        return timer;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
