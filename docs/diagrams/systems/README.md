# Diagrammi UML di sistema

Versione minima: ogni diagramma mostra solo il pattern principale e il flusso essenziale.

## Indice

| Diagramma | Focus |
|---|---|
| [01-character-creation](01-character-creation.svg) | `CharacterFactory` crea `Player` ed `Enemy` da YAML |
| [02-collectibles-inventory](02-collectibles-inventory.svg) | `CollectibleFactory` clona collectible e li passa allo zaino |
| [03-abilities-combat](03-abilities-combat.svg) | Le abilità YAML scelgono una `AbilityStrategy` |
| [04-npc-progression](04-npc-progression.svg) | Le scelte NPC aggiornano ricompense ed evoluzione |
| [05-dialogue-ai](05-dialogue-ai.svg) | `NpcDialogueService` nasconde AI, prompt e fallback |
| [06-level-runtime](06-level-runtime.svg) | `SceneController` carica il livello e apre `LevelScreen` |
| [07-save-game-flow](07-save-game-flow.svg) | `GameState` è il memento salvato in JSON |

## Regola

- Mostrare 5-8 elementi principali.
- Non mostrare DTO, enum o metodi non indispensabili.
- Usare `<<external>>` quando il dettaglio è spiegato in un altro diagramma.
- Se un dettaglio non aiuta a capire il pattern, resta fuori dal diagramma.

## Generazione

```bash
java -jar .gradle/plantuml/plantuml.jar -tsvg docs/diagrams/systems/*.puml
```
