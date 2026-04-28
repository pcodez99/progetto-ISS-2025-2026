# Viddani VS Alieni

Progetto di **Ingegneria del Software** per la realizzazione di un videogioco chiamato **Viddani VS Alieni**: un **gioco di ruolo (RPG) a livelli**.

## Descrizione

In *Viddani VS Alieni* il giocatore affronta un'invasione aliena attraversando livelli progressivi, con difficolta' crescente, nemici sempre piu' forti e obiettivi specifici da completare.

Il progetto unisce:
- analisi dei requisiti;
- progettazione software;
- implementazione del gioco;
- verifica e validazione.

## Obiettivi del progetto

- Progettare un'architettura chiara, modulare e manutenibile.
- Sviluppare meccaniche RPG a livelli (progressione, sfide, ricompense).
- Applicare buone pratiche di sviluppo collaborativo (Git, issue tracking, review).
- Documentare in modo completo le scelte progettuali e implementative.

## Caratteristiche previste del gioco

- **Progressione a livelli** con aumento graduale della difficolta'.
- **Sistema di combattimento** contro diverse tipologie di alieni.
- **4 personaggi giocabili** con abilita' e stili di gioco differenti.
- **Obiettivi di livello** (es. sopravvivenza, eliminazione boss, raccolta risorse).
- **Interfaccia chiara** per menu, stato del personaggio e avanzamento.
- **Interazioni con NPC** basate su esperienza, altruismo e scelte morali.
- **Modalita' cooperativa locale a turni** per massimo 2 giocatori.

## Personaggi giocabili

Ogni personaggio ha estetica, arma e abilita' dedicate. Le scelte del giocatore influenzano il modo in cui i personaggi interagiscono tra loro e con gli NPC.

- **Il Nonno**  
  Aspetto: coppola, sedia a rotelle.  
  Arma: doppietta ("scupetta").  
  Abilita': elevata **potenza con il fucile**.

- **Il Giovane (Bambino)**  
  Aspetto: canotta bianca.  
  Arma: fionda.  
  Abilita': alta **velocita'**.

- **Il Papa'**  
  Aspetto: camicia a quadri, look rustico.  
  Arma: zappa.  
  Abilita': attacchi ravvicinati potenti con la **zappa**.

- **La Mamma**  
  Arma: ciabatta.  
  Abilita': supporto, puo' **curare gli altri personaggi con la Voltaren**.

## Modalita' di gioco

- Massimo **2 giocatori**.
- Turni alternati tra i giocatori.
- Possibilita' di scegliere personaggi e strategie diverse a ogni livello.

## NPC, altruismo e ricompense

Durante i livelli il giocatore incontra NPC con cui puo' interagire.

- Gli NPC possono regalare oggetti.
- La **rarita' degli oggetti** ricevuti e' direttamente proporzionale alla barra di **esperienza/altruismo**.
- Alcuni NPC possono proporre livelli o missioni aggiuntive.
- Il giocatore puo' anche scegliere di **rubare** gli oggetti, con possibili conseguenze su reputazione, relazioni e progressione.

## Oggetti trovabili

Il sistema oggetti includera' consumabili, potenziamenti, equipaggiamenti e ricompense rare legate all'esplorazione e alle interazioni con NPC.
La lista completa degli oggetti verra' definita nelle prossime milestone di progettazione.

## Stack tecnologico (work in progress)

- Linguaggio: **Java**
- Framework di gioco: **libGDX**
- Build tool: **Gradle**

## Struttura del repository

- `core/` - logica di gioco condivisa
- `lwjgl3/` - launcher desktop e configurazione runtime
- `assets/` - risorse grafiche, audio e file di gioco

## Come avviare il progetto

Prerequisiti:
- JDK installato
- Gradle (oppure wrapper incluso nel progetto)

Esecuzione (desktop):
1. Clonare il repository.
2. Aprire il progetto in un IDE Java (es. IntelliJ IDEA).
3. Eseguire il launcher desktop (`lwjgl3`).

## Stato del progetto

Il progetto e' in sviluppo. Le funzionalita' verranno introdotte in modo incrementale per milestone.

## Roadmap iniziale

- [ ] Definizione requisiti funzionali e non funzionali
- [ ] Progettazione architettura e diagrammi principali
- [ ] Implementazione prototipo giocabile
- [ ] Introduzione sistema livelli e progressione RPG
- [ ] Testing, bilanciamento e rifinitura

## Team e contesto

Questo repository e' sviluppato nell'ambito di un corso/progetto universitario di **Ingegneria del Software**.
