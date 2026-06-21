# Diagrammi per sottosistemi e Design Pattern

Questi diagrammi affiancano, senza sostituire, i diagrammi MVC presenti in `docs/diagrams`.
La suddivisione segue le responsabilità e i pattern delle slide ISS: configurazione, creazione,
modello runtime e orchestrazione sono rappresentati nello stesso contesto funzionale.

## Indice

| Diagramma | Pattern principale | Responsabilità |
|---|---|---|
| [01-character-creation](01-character-creation.svg) | Abstract Factory | Creazione coordinata di Player ed Enemy |
| [02-collectibles-inventory](02-collectibles-inventory.svg) | Prototype | Istanze runtime da archetipi YAML validati |
| [03-abilities-combat](03-abilities-combat.svg) | Strategy | Algoritmi intercambiabili per abilità |
| [04-npc-progression](04-npc-progression.svg) | Factory | NPC, regole evolutive e ricompense configurate |
| [05-dialogue-ai](05-dialogue-ai.svg) | Facade | Dialogo, AI, parsing e fallback dietro un servizio unico |
| [06-level-runtime](06-level-runtime.svg) | Facade | Caricamento e inizializzazione del livello TMX |
| [07-save-game-flow](07-save-game-flow.svg) | Memento | Snapshot e ripristino dello stato di gioco |

## Convenzioni UML

- `*--` composizione: il proprietario controlla il ciclo di vita.
- `o--` aggregazione: il collaboratore può vivere separatamente.
- `-->` dipendenza runtime.
- `..>` creazione, caricamento o uso occasionale.
- `<<external>>` classe mostrata solo per chiarire il confine del sottosistema.
- Gli artifact YAML sono infrastruttura e non dipendono dai modelli di dominio.

Ogni diagramma mantiene un solo pattern principale. Builder, Prototype, Strategy, State,
Facade e Memento sono indicati solamente dove risolvono una pressione reale del codice.

## Generazione

I file usano il motore Smetana integrato in PlantUML:

```bash
java -jar .gradle/plantuml/plantuml.jar -tsvg docs/diagrams/systems/*.puml
```
