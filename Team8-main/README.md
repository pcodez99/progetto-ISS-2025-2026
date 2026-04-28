### SIKULA - L’Ultimo Brindisi

Videogioco 2D narrativo a turni ispirato alla cultura e al folklore siciliano.  
Progetto sviluppato per il corso di **Ingegneria del Software** con focus su architettura, manutenibilità, test e metodologia **Agile/Scrum**.

- **Team:** Team 8
- **Tecnologie:** Python, Pygame

 “Un brindisi, un liquido viola, e la Sicilia reale si dissolve in una dimensione onirica.”

#### Come giocare
Si può avviare facilmente il gioco tramite il file Sikula.exe (su windows), altrimenti bisogna scaricare pygame ed eseguire il file src/main.py

#### Panoramica
**SIKULA - L’Ultimo Brindisi** racconta la storia di **Turiddu** e **Rosalia** che, durante una sagra di paese, incontrano il venditore ambulante **U Strammu** e assaggiano “**U Spiritu du Fikudinnia**”.  
Dopo il brindisi, i protagonisti si risvegliano in una dimensione simbolica: un viaggio attraverso **quattro regioni**, ciascuna legata a un **Asso** della tradizione siciliana:

- **Denari** - avidità e potere  
- **Bastoni** - natura e ostinazione  
- **Spade** - onore e conflitto  
- **Coppe** - memoria e abbandono  

Raccolti tutti e quattro gli Assi, si sblocca l’accesso all’**Etna** e alla conclusione della storia 

#### Architettura
Il progetto è strutturato secondo **MVC (Model-View-Controller)**:
- **Model**: logica di gioco (combattimento, inventario, status, progressione, condizioni di vittoria/sconfitta)
- **View**: rendering e UI (HUD, menu, log, animazioni)
- **Controller**: input e mappatura comandi per contesto (menu/esplorazione/combattimento)

#### Gameplay e struttura

Flusso di gioco: **Nuova Partita → Prologo → Hub Centrale → 4 Regioni → Ritorno all’Hub → Etna → Finale → Epilogo**

L’Hub Centrale, **L’Ombelico della Sicilia**, è lo snodo di progressione: permette di scegliere liberamente l’ordine con cui affrontare le regioni.

Ogni regione segue uno schema comune:
1. **Scelta iniziale** (influenza risorse/difficoltà/ramificazioni)
2. **Gatekeeper** (superabile in modi diversi: combattimento o oggetti)
3. **Boss finale** (meccaniche uniche)
4. **Ricompensa**: ottenimento dell’**Asso**
