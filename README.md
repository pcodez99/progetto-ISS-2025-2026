# 👽 Viddani VS Alieni

![Viddani VS Alieni](assets/background_init.png)

Progetto di **Ingegneria del Software** per la realizzazione di un videogioco chiamato **Viddani VS Alieni**: un **gioco di ruolo (RPG) a livelli**.

---

## 📖 Descrizione

Gli alieni, provenienti dal pianeta **Proxima Centauri b** (un mondo ostile e scarsamente abitabile), stanno tentando di invadere la Terra per trovare una nuova casa. Per questo motivo si scontrano con i *Viddani*, i fieri contadini pronti a difendere il loro territorio. L'obiettivo principale del gioco è sconfiggere gli invasori spaziali e impedire loro di conquistare il pianeta.

In *Viddani VS Alieni* il giocatore affronta l'invasione attraversando livelli progressivi, con difficoltà crescente, nemici sempre più forti e obiettivi specifici da completare.

Il progetto unisce:
- 📊 Analisi dei requisiti
- 🏗️ Progettazione software
- 💻 Implementazione del gioco
- ✅ Verifica e validazione

---

## 🎯 Obiettivi del progetto

- Progettare un'architettura chiara, modulare e manutenibile.
- Sviluppare meccaniche RPG a livelli (progressione, sfide, ricompense).
- Applicare buone pratiche di sviluppo collaborativo (Git, issue tracking, review).
- Documentare in modo completo le scelte progettuali e implementative.

---

## ✨ Caratteristiche previste del gioco

- **Progressione a livelli**: ci saranno in totale **3 livelli**, con un aumento graduale della difficoltà e ambientazioni uniche:
  - **Livello 1:** La Campagna (nei campi terrestri).
  - **Livello 2:** La Navicella Aliena.
  - **Livello 3:** Il Pianeta degli Alieni (Proxima Centauri b).
- **Sistema di combattimento** contro diverse tipologie di alieni, culminante con un **boss finale per ogni livello**.
- **4 personaggi giocabili** con abilità e stili di gioco differenti.
- **Obiettivi di livello** (es. sopravvivenza, eliminazione boss, raccolta risorse).
- **Interfaccia chiara** per menu, stato del personaggio e avanzamento.
- **Interazioni con NPC** basate su esperienza, altruismo e scelte morali.
- **Modalità cooperativa locale a turni** per massimo 2 giocatori.

---

## 🧑‍🌾 Personaggi giocabili

Ogni personaggio ha estetica, arma e abilità base dedicate. Inoltre, progredendo nel gioco e salendo di livello, i personaggi potranno sviluppare e sbloccare nuove **abilità speciali**. Le scelte del giocatore influenzano il modo in cui i personaggi interagiscono tra loro e con gli NPC.

- 👴 **Il Nonno**  
  **Aspetto:** coppola, sedia a rotelle.  
  **Arma:** doppietta ("scupetta").  
  **Attacco Base:** spara con il fucile.  
  **Abilità Speciale (sviluppabile):** *Salto con la carrozzina* (un salto molto potente che genera un'onda d'urto).

- 👦 **Il Giovane (Bambino)**  
  **Aspetto:** canotta bianca.  
  **Arma:** fionda.  
  **Attacco Base:** spara con la fionda.  
  **Abilità Speciale (sviluppabile):** *Pioggia di pietre* (lancia dei sassi nell'area nemica).

- 👨 **Il Papà**  
  **Aspetto:** camicia a quadri, look rustico.  
  **Arma:** zappa.  
  **Attacco Base:** attacchi ravvicinati con la zappa.  
  **Abilità Speciale (sviluppabile):** *Il lancio del viddano* (lancia la zappa come un boomerang) .

- 👩 **La Mamma**  
  **Aspetto:** look da contadina, con cappello di paglia e bretelle.  
  **Arma:** ciabatta.  
  **Attacco Base:** attacchi a distanza con la ciabatta.  
  **Abilità Speciale (sviluppabile):** *Cura di gruppo* (cura tutti i giocatori contemporaneamente).

---

## 👾 I Nemici (Alieni)

Gli antagonisti principali del gioco sono gli Alieni, suddivisi in **3 classi differenti**.
**Regola d'ingaggio:** Quando il giocatore incontra un alieno sul suo cammino, **la battaglia inizia immediatamente**.
Inoltre, la regola alla base della forza dei nemici è: **più sono numerosi, meno sono potenti**. Questo costringerà i giocatori ad adattare le loro strategie in base alla quantità e al tipo di alieni presenti.
Al termine di ciascuno dei 3 livelli, è prevista una sfida decisiva contro un **boss finale**.

---

## 🎮 Modalità di gioco

- Massimo **2 giocatori**.
- Turni alternati tra i giocatori.
- Possibilità di scegliere personaggi e strategie diverse a ogni livello.

---

## 🗣️ NPC, altruismo e ricompense

Durante i livelli il giocatore incontra NPC con cui può interagire. I dialoghi con gli NPC sono guidati da un'**Intelligenza Artificiale** (utilizzando un modello open source eseguito in locale), per offrire conversazioni dinamiche, imprevedibili e caratterizzate da **divertenti scambi di battute in dialetto siciliano**.
**Regola d'ingaggio:** A differenza degli alieni, quando si incontra un NPC **prima si dialoga**; solo al termine della conversazione, e unicamente se le scelte del giocatore o la trama lo richiedono, partirà un'eventuale battaglia.

- Gli NPC possono regalare oggetti.
- Alcuni NPC possono proporre missioni secondarie o richiedere oggetti specifici.
- Il giocatore può anche scegliere di **rubare** gli oggetti, con possibili conseguenze sulla barra del carattere.
- Il catalogo NPC è data-driven in `assets/configs/npcs.yaml` e viene caricato tramite `NpcFactory`, così dialoghi, ricompense e conseguenze morali restano configurabili senza hardcodare i personaggi.
- La scheda narrativa completa degli NPC è in `docs/npcs.md`; il catalogo runtime attuale include gli NPC già pronti per il livello 1.

NPC configurati:

- **Livello 1 - La Campagna:** Zio Toto, Zia Pina, Zyrko l'Ingegnere di Proxima, Turiddu.

### ❤️ Barra Carattere: Altruismo vs Egoismo

Il gioco include una barra `Carattere` che cambia colore in base al livello, indicando rispettivamente:
- **Altruismo**, se tende ad essere piena.
- **Egoismo**, se tende ad essere vuota.

**Regola principale:**
- Se la barra `Carattere` scende **sotto i 50 punti**, diventa **rossa**. Più il livello è basso, più il colore vira da un *rosso bordeaux* verso un *rosso acceso*.
- Se la barra `Carattere` sale **sopra i 50 punti**, diventa **blu**. Più il livello è alto, più il colore vira da un **blu scuro** verso un **celeste chiaro**
- Le scelte dei giocatori (aiutare, cooperare, rubare, ecc.) influenzano direttamente il valore della barra.

### Evoluzione dei personaggi e abilita

Ogni personaggio ha tre slot abilita configurati in `assets/configs/characters.yaml`:

- `base`: abilita disponibile dall'inizio.
- `altruistic`: abilita sbloccata aiutando NPC, completando missioni e facendo scelte cooperative.
- `egoistic`: abilita sbloccata rubando, mentendo, minacciando o scegliendo ricompense egoiste.

Le soglie di sblocco sono in `assets/configs/evolution_rules.yaml`. Lo stato evolutivo e salvabile per ogni giocatore, quindi in multiplayer ogni personaggio puo crescere in modo diverso. I profili dialogo in `assets/configs/dialogue_profiles.yaml` cambiano tono e temperatura del modello in base al percorso dominante: bilanciato, altruista o egoista.

---

## 🎒 Oggetti trovabili

Il sistema oggetti includerà consumabili, potenziamenti, equipaggiamenti e ricompense rare legate all'esplorazione e alle interazioni con NPC.
Le ricompense narrative già associate agli NPC includono Gasolio Benedetto, Cannolo Rinforzante, Cassata Mistica, Metallo Alieno Forgiato, Pagnotta Spaziale, Siero Anti-Proxima, Benedizione Viddana, Cristallo di Proxima, Seme della Terra e Stella Spezzata.

---

## 🛠️ Stack tecnologico (work in progress)

- **Linguaggio:** Java ☕
- **Framework di gioco:** libGDX 🎮
- **Build tool:** Gradle 🐘

---

## ⚙️ Configurazione dinamica

Il gioco utilizza un file di configurazione **`game.properties`** nella root del progetto per impostare parametri di gioco modificabili a runtime. Le impostazioni includono:

- `max_player_distance`: distanza massima consentita tra i personaggi (default `400.0`).
- `camera_zoom`: fattore di zoom della telecamera (default `0.72`).
- `player_size`: scala di rendering dei personaggi (default `160.0`).
- `default_player_speed`: velocità base dei personaggi (default `200.0`).
- `draw_physics_debug`: visualizzare o nascondere il debug Box2D (default `true`).
- `music_volume` e `sfx_volume`: volumi audio (default `0.5`).
- `ai_enabled`: abilita o disabilita i dialoghi generati con IA locale.
- `ai_base_url`, `ai_chat_endpoint`, `ai_model`: configurano Ollama e il modello da usare.
- `ai_connect_timeout_ms`, `ai_read_timeout_ms`: timeout REST per evitare blocchi lunghi.
- `ai_default_temperature`, `ai_max_prompt_chars`, `ai_fallback_to_static_dialogue`: controllano creativita, dimensione prompt e fallback sui dialoghi statici degli NPC.

Il file viene generato al primo avvio se non presente e può essere modificato a caldo; le modifiche vengono salvate automaticamente su disco. Per personalizzare il comportamento di gioco, editare `game.properties` e riavviare il gioco.

### Servizio AI locale

I dialoghi NPC possono passare da Ollama tramite `AiService`, un Singleton configurabile con:

- `AiService.getAi()`
- `AiService.setAi(...)`
- `AiService.resetAi()`

`NpcDialogueService` usa `DialogueProfileService` per costruire prompt e temperatura in base al percorso morale del giocatore, poi invia la richiesta a Ollama. Se il servizio non risponde e il fallback e attivo, viene usato `sampleDialogue` dell'NPC.

### Voce NPC locale

Le risposte testuali degli NPC possono essere sintetizzate da Voxtral 4B. Su
macOS con 16 GB viene usata la conversione MLX 4-bit; su NVIDIA si puo usare il
checkpoint BF16 originale tramite vLLM-Omni. Il gioco chiama
`POST http://127.0.0.1:8000/v1/audio/speech` in background e continua a mostrare
subito i sottotitoli; se il server non e disponibile, il dialogo resta testuale.

Il launcher Docker e lo smoke test si trovano in `services/voxtral-tts/`.
L'esecuzione richiede Linux o WSL2, una GPU NVIDIA con 16 GB di VRAM e Docker
con NVIDIA Container Toolkit. Il compose usa un profilo a concorrenza singola
pensato per rientrare nel vincolo di memoria del progetto.

Il client TTS usa Factory Method per scegliere tra due prodotti concreti:

- `LocalVoxtralTtsClient`, creato da `LocalVoxtralTtsClientCreator`;
- `MistralApiTtsClient`, creato da `MistralApiTtsClientCreator`.

Il provider si seleziona con `tts_provider` oppure con `TTS_PROVIDER`. Per usare
direttamente l'API Mistral, la chiave viene letta esclusivamente dalla variabile
d'ambiente `MISTRAL_API_KEY`; non esistono fallback verso properties o YAML.

Per avviare caricando un `.env` locale ignorato da Git:

```bash
./scripts/run-with-env.sh
```

Se `.env` non esiste, lo script lo crea vuoto e chiede di compilare la chiave.


## 🛠️ Pipeline CI/CD

Il progetto utilizza **GitHub Actions** con due workflow separati:

- **CI** (`.github/workflows/ci.yml`): a ogni push verso `main` o `dev`, inclusi i merge su questi branch, esegue i test e crea pacchetti desktop clickabili con `jpackage`. Lo stesso controllo di test viene eseguito anche sulle Pull Request verso `main` o `dev`.
- **Release** (`.github/workflows/release.yml`): quando viene pubblicato un tag `v*` (es. `v1.0.0-alpha`) esegue i test, costruisce i pacchetti `jpackage` per Linux, Windows e macOS, e crea una GitHub Release.

Il comando base usato dalla pipeline e equivalente al formato:

```bash
jpackage \
  --type app-image \
  --name "Viddani VS Alieni" \
  --input lwjgl3/build/libs \
  --main-jar "ISS 2025-<versione>.jar" \
  --main-class io.github.iss_2025_2026.lwjgl3.Lwjgl3Launcher
```

Gli artefatti locali di partenza prodotti da Gradle si trovano in:

- `lwjgl3/build/libs/ISS 2025-<versione>.jar`: JAR usato come input da `jpackage`.
- `jpackage-output/`: cartella generata da `jpackage` con l'app clickabile e il runtime Java incluso.

Per scaricare una build non ufficiale generata da push o merge su `main`/`dev`, aprire la run su **GitHub Actions** e scaricare l'artifact:

- `viddani-vs-alieni-<branch>-linux-clickable`
- `viddani-vs-alieni-<branch>-windows-clickable`
- `viddani-vs-alieni-<branch>-macos-clickable`

Per scaricare una versione ufficiale, aprire **GitHub > Releases** e scaricare:

- `viddani-vs-alieni-<versione>-linux-clickable.zip`
- `viddani-vs-alieni-<versione>-windows-clickable.zip`
- `viddani-vs-alieni-<versione>-macos-clickable.zip`

Dopo aver estratto lo ZIP:

- Windows: aprire `Viddani VS Alieni/Viddani VS Alieni.exe`.
- macOS: aprire `Viddani VS Alieni.app`.
- Linux: eseguire `Viddani VS Alieni/bin/Viddani VS Alieni`.

---

## 📁 Struttura del repository

- `core/` - Logica di gioco condivisa, organizzata secondo il pattern MVC:
    - `model/`: Dati del gioco e logica di business.
    - `view/`: Interfaccia utente, grafica e rendering.
    - `controller/`: Gestione dell'input e comunicazione tra View e Model
- `lwjgl3/` - Launcher desktop e configurazione runtime.
- `assets/` - Risorse grafiche, audio e file di gioco.

---

## 🚀 Come avviare il progetto

### Prerequisiti
- **Java Development Kit (JDK)** installato (versione 17 o superiore consigliata).
- **Git** installato nel sistema.
- **Node.js e npm** (per la gestione dei Git Hooks con Husky).

### Esecuzione da Terminale (Desktop)
1. **Clona il repository**:
   ```bash
   git clone https://github.com/pcodez99/progetto-ISS-2025-2026.git
   ```
2. **Spostati nella directory del progetto**:
   ```bash
   cd progetto-ISS-2025-2026
   ```
3. **Installa le dipendenze npm** (necessario per attivare Husky):
   ```bash
   npm install
   ```
4. **Avvia il gioco tramite Gradle Wrapper**:
   - Su Linux/macOS:
     ```bash
     ./gradlew lwjgl3:run
     ```
   - Su Windows:
     ```cmd
     gradlew.bat lwjgl3:run
     ```

### Esecuzione tramite IDE (es. IntelliJ IDEA)
1. Apri IntelliJ IDEA.
2. Seleziona **File > Open** e scegli la cartella del progetto.
3. Attendi il caricamento e la sincronizzazione di Gradle.
4. Esegui il launcher desktop avviando la classe `Lwjgl3Launcher` presente nel modulo `lwjgl3`.

---

## 🤝 Come contribuire al progetto

Siamo felici di accettare contributi! Per contribuire al progetto, segui questi passaggi:

1. **Esegui un fork** del repository.
2. **Crea un branch** per la tua feature o bugfix (`git checkout -b feature/nuova-feature`).
3. **Effettua i tuoi commit** descrivendo chiaramente i cambiamenti (`git commit -m 'Aggiunta nuova funzionalità'`).
4. **Fai il push** del branch sul tuo fork (`git push origin feature/nuova-feature`).
5. **Apri una Pull Request** descrivendo nel dettaglio le modifiche apportate.

Assicurati che il codice rispetti le linee guida del progetto e passi tutti i test locali prima di aprire una Pull Request.

---

## 📈 Stato del progetto

Il progetto è in sviluppo. Le funzionalità verranno introdotte in modo incrementale per milestone.

---

## 🗺️ Roadmap iniziale

- [x] Definizione requisiti funzionali e non funzionali
- [x] Progettazione architettura e diagrammi principali
- [x] Setup Pipeline CI/CD (GitHub Actions)
- [ ] Implementazione prototipo giocabile
- [ ] Introduzione sistema livelli e progressione RPG
- [ ] Testing, bilanciamento e rifinitura

---

## 🎓 Team e contesto

Questo repository è sviluppato nell'ambito di un corso/progetto universitario di **Ingegneria del Software**.
