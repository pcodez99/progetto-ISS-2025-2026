# Pacchetto Map (Gestione dei Livelli e Geometria Isometrica)

Questo pacchetto contiene le classi responsabili del caricamento delle mappe Tiled (`.tmx`), della gestione geometrica delle proiezioni isometriche e della costruzione degli ostacoli fisici del gioco.

---

## Ruolo Architetturale (MVC)

* **Condivisione**: Il pacchetto fornisce dati geometrici utili sia per il rendering dei tiles (View) sia per la simulazione fisica Box2D (Controller/Fisica).

---

## Proiezione Geometrica Isometrica

Il gioco utilizza una mappa isometrica inclinata con assi orientati in questo modo:
* **Tasto W (Avanza ↗)**: Incrementa sia `X` che `Y` isometrici.
* **Tasto S (Indietro ↙)**: Decrementa sia `X` che `Y` isometrici.
* **Tasto A (Laterale ↖)**: Decrementa `X` e incrementa `Y` isometrici.
* **Tasto D (Laterale ↘)**: Incrementa `X` e decrementa `Y` isometrici.

La classe `IsoMapGeometry` implementa le formule di proiezione per posizionare correttamente gli oggetti fisici ed i personaggi sopra i pixel disegnati dalla mappa isometrica:
```java
// Da coordinate Tile a coordinate Mondo (Cartesiane)
public Vector2 tileToWorld(float tileX, float tileY) {
    return new Vector2(
        (tileX + tileY) * tileWidth / 2f + OFFSET_X,
        (tileY - tileX) * tileHeight / 2f + OFFSET_Y
    );
}
```
Inoltre, gestisce la correzione degli offset isometrici degli oggetti disegnati nel caricatore Tiled tramite traslazioni di precisione.
