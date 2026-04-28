"""
Scripts Registry - Narrative & Logic Director.
Updated: Viridor Narrative Implementation (Intro, Sphinx Riddle, Nonno Ciccio).
Updated: Finale Logic flow (Giufa -> Carretto -> Etna) -> FIXED FADE LOCK.
"""
from src.model.script_actions import GameScript, ScriptAction, ActionType
from src.model.items.item_ids import ItemIds
from src.model.states.base_state import StateID

class ScriptsRegistry:
    @staticmethod
    def get_script(script_id: str, game_model) -> GameScript:
        
        # Helper per ottenere il nome del personaggio attivo
        active_player = game_model.gamestate.get_active_player()
        p_name = active_player.name if active_player else "Turiddu"

        # =====================================================================
        # 1. HUB & INTRO
        # =====================================================================
        if script_id == "intro_hub_arrival":
            return GameScript("hub_arrival", [
                ScriptAction.wait(1.0),
                ScriptAction.show_dialogue("Turiddu", "Ahi... la testa. Mi sento come se mi avesse investito l'autobus per Palermo."),
                ScriptAction.show_dialogue("Rosalia", "(Si massaggia la faccia) Mamma mia... Mi sento come se avessi fatto serata fino alle sei del mattino... ma senza la parte divertente."),
                ScriptAction.show_dialogue("Turiddu", "Ok, fermi tutti. Perché il cielo è viola? E perché quei fichi d'india sono alti come palazzi?"),
                ScriptAction.show_dialogue("Rosalia", "Turiddu, chiama qualcuno. Subito."),
                ScriptAction.show_dialogue("Turiddu", "(Guarda il telefono) Niente campo. Zero tacche. E la batteria segna 'Pupo%'. Ma che mi significa?"),
                ScriptAction.show_dialogue("Rosalia", "Ehm...? Non vorrei dire, ma c'è un signore che ci fissa da quando abbiamo aperto gli occhi."),
                ScriptAction.show_dialogue("Sistema", "OBIETTIVO: Andate a parlare con Giufa."),
                ScriptAction.set_flag("seen_hub_intro", True)
            ])

        if script_id == "giufa_hub_talk":
            if game_model.get_flag("met_giufa"):
                aces = game_model.get_ace_count()
                if aces == 4:
                    return GameScript("giufa_final", [
                        ScriptAction.show_dialogue("Giufa", f"Mbare {p_name}, incredibile! Hai tutti e 4 gli Assi!"),
                        ScriptAction.show_dialogue("Giufa", "Ma ascolta bene. Non è ancora finita."),
                        ScriptAction.show_dialogue("Giufa", "Ora dovete affrontare l'Oste Eterno. È la sfida più difficile di tutte."),
                        ScriptAction.show_dialogue("Giufa", "Vi servirà tutto ciò che avete raccolto. Ogni oggetto, ogni potere."),
                        ScriptAction.show_dialogue("Giufa", "Il Carretto ora è pronto. Andate da lui, toccatelo, e vi porterà nel cuore del vulcano."),
                        ScriptAction.set_flag("carretto_ready", True),
                        ScriptAction.show_dialogue("Sistema", "OBIETTIVO: Interagisci con il Carretto per andare all'Etna.")
                    ])
                else:
                    return GameScript("giufa_reminder", [
                        ScriptAction.show_dialogue("Giufa", f"Forza picciotti, mancano {4-aces} Assi!"),
                        ScriptAction.show_dialogue("Giufa", "L'Oro è nel Palazzo, le Spine nel bosco, il Vino a sinistra, il Ferro nel fuoco.")
                    ])

            game_model.set_flag("met_giufa", True)
            return GameScript("giufa_intro", [
                ScriptAction.show_dialogue("Giufa", "Belli freschi! Belli svegli! O forse state ancora dormendo con gli occhi aperti?"),
                ScriptAction.show_dialogue("Rosalia", "Scusi? Lei chi è? E dove siamo finiti?"),
                ScriptAction.show_dialogue("Giufa", "Siete nell'Ombelico, gioia. Il centro di tutto. Io sono Giufa."),
                ScriptAction.show_dialogue("Rosalia", "Giufa? Come quello delle storie che mi raccontava mia nonna? Quello... un po' tonto?"),
                ScriptAction.show_dialogue("Giufa", "(Ride di gusto) Tua nonna la sapeva lunga! Lo scemo del villaggio sa sempre tutto, perché nessuno si preoccupa di nascondersi da lui."),
                ScriptAction.show_dialogue("Turiddu", "Senta signor Giufa, è tutto molto affascinante, ma noi dobbiamo tornare alla macchina. Da che parte è il parcheggio?"),
                ScriptAction.show_dialogue("Giufa", "Il parcheggio! Bedda matri, che fissazione. Qui l'unica cosa che si parcheggia è quella bestia lì."),
                ScriptAction.show_dialogue("Narratore", "(Indica il centro della piazza)"),
                ScriptAction.show_dialogue("Sistema", "OBIETTIVO: Esamina il Carretto al centro.")
            ])

        if script_id == "interact_carretto":
            # LOGICA FINALE: Se Giufà ha dato l'ok (4 assi presi), si parte per l'Etna
            if game_model.get_flag("carretto_ready"):
                return GameScript("carretto_departure", [
                    ScriptAction.show_dialogue("Narratore", "I 4 Assi brillano intensamente. Il gatto gigante apre un occhio e sbadiglia."),
                    ScriptAction.show_dialogue("Turiddu", "Ok, il gatto è sveglio. Il Carretto vibra. Ci siamo?"),
                    ScriptAction.show_dialogue("Rosalia", "Andiamo a prenderci la nostra libertà. All'Etna!"),
                    # FIX: Uso DIALOGO BLOCANTE invece di WAIT per evitare softlock su timer
                    ScriptAction.show_dialogue("Sistema", "Si parte..."), 
                    ScriptAction.change_room("etna_entry", "bottom")
                ])

            if game_model.get_flag("seen_carretto_intro"):
                return GameScript("carretto_look", [
                    ScriptAction.show_dialogue("Rosalia", "Il gatto dorme ancora. Dobbiamo trovare gli Assi."),
                    ScriptAction.show_dialogue("Sistema", "Trovate i 4 assi per attivare il Carretto.")
                ])
            
            game_model.set_flag("seen_carretto_intro", True)
            return GameScript("carretto_intro", [
                ScriptAction.show_dialogue("Turiddu", "Aspetta... ma quello è un gatto? È gigante! Sembra una Seicento pelosa parcheggiata sul carretto!"),
                ScriptAction.show_dialogue("Rosalia", "Sta dormendo proprio sopra i sedili. E quel carretto... sembra un rottame. È tutto grigio."),
                ScriptAction.show_dialogue("Giufa", "Perspicace il piccotto. Il Carretto è nudo. Senza i colori dei Quattro Assi, il gatto non si sveglia e il carretto non parte."),
                ScriptAction.show_dialogue("Giufa", "Se volete tornare a casa, dovete andare sull'Etna. E per andare sull'Etna, dovete spostare il gatto. Chiaro, no?"),
                ScriptAction.show_dialogue("Turiddu", "Chiarissimo. Dobbiamo fare i fattorini per un personaggio delle fiabe e spostare un gatto Godzilla. Un martedì sera qualunque..."),
                ScriptAction.show_dialogue("Giufa", "Esatto! I Baroni hanno rubato gli Assi. L'Oro è lassù nel Palazzo. Le Spine sono a destra. Il Vino è a sinistra e il Ferro è laggiù nel fuoco."),
                ScriptAction.show_dialogue("Giufa", "Fate voi. Io aspetto qui. Tanto primura un cinn’è."),
                ScriptAction.show_dialogue("Sistema", "NUOVA MISSIONE: Recupera i 4 Assi dai Boss delle Regioni.")
            ])

        # =====================================================================
        # 2. REGIONE AURION (DENARI)
        # =====================================================================
        
        if script_id == "intro_aurion_entry":
            if not game_model.get_flag("visited_aurion_entry"):
                return GameScript("intro_aurion", [
                    ScriptAction.show_dialogue("Narratore", "Benvenuti ad AURION. Dove il silenzio è d'oro, ma le parole costano care."),
                    ScriptAction.show_dialogue("Sistema", "Scegliete una porta (Attenzione: potete sceglierne solo una):"),
                    ScriptAction.show_dialogue("Sistema", "• SINISTRA: 'Profumo intenso e familiare... sembra roba fritta.'"),
                    ScriptAction.show_dialogue("Sistema", "• CENTRO: 'Un sacco di iuta pesante. Qualcosa luccica all'interno.'"),
                    ScriptAction.show_dialogue("Sistema", "• DESTRA: 'Una cartellina rossa abbandonata. Sembra importante.'"),
                    ScriptAction.set_flag("visited_aurion_entry", True)
                ])
            return GameScript("noop", [])

        # LOGICA PORTE (LOCK) AURION
        def get_path_lock_script(region_flag_start, path_flag, target_room, spawn_id):
            already_chosen = game_model.get_flag(region_flag_start)
            correct_path = game_model.get_flag(path_flag)
            if not already_chosen or correct_path:
                return GameScript("move_ok", [ScriptAction.change_room(target_room, spawn_id)])
            else:
                return GameScript("move_blocked", [ScriptAction.show_dialogue("Narratore", "La porta è sigillata. Hai già fatto la tua scelta altrove.")])

        if script_id == "enter_door_arancina": return get_path_lock_script("aurion_starter_received", "aurion_path_arancina", "aurion_vault_arancina", "bottom")
        if script_id == "enter_door_monete": return get_path_lock_script("aurion_starter_received", "aurion_path_monete", "aurion_vault_monete", "bottom")
        if script_id == "enter_door_dossier": return get_path_lock_script("aurion_starter_received", "aurion_path_dossier", "aurion_vault_dossier", "bottom")
        
        # PICKUP AURION
        if script_id == "pickup_arancina": 
            msg = game_model.aurion.make_choice(0)
            return GameScript("pk_arancina", [
                ScriptAction.show_dialogue(p_name, "Che buon profumo? È... celestiale."),
                ScriptAction.show_dialogue("Narratore", "(Si avvicina al carrello e solleva il coperchio)"),
                ScriptAction.show_dialogue(p_name, "Arancine! Calde! Appena fritte!"),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_monete": 
            msg = game_model.aurion.make_choice(1)
            return GameScript("pk_monete", [
                ScriptAction.show_dialogue(p_name, "Qualcuno ha lasciato un sacco aperto, peserà 20 chili!"),
                ScriptAction.show_dialogue(p_name, "Sono monete d'oro antico. Incredibile."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_dossier": 
            msg = game_model.aurion.make_choice(2)
            return GameScript("pk_dossier", [
                ScriptAction.show_dialogue(p_name, "C'è un fascicolo con scritto 'RISERVATO'."),
                ScriptAction.show_dialogue(p_name, "'Don Tanino - Lista dei favori e dei debiti - Anno 1992'."),
                ScriptAction.show_dialogue(p_name, "Wow, qui c'è roba che farebbe tremare mezza Sicilia."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])

        # --- GATEKEEPER AURION (DELEGATO AL MODELLO) ---
        if script_id == "aurion_gate":
            res = game_model.aurion.resolve_gatekeeper()
            
            # Intro Dialogues (Preservati)
            intro_actions = [
                ScriptAction.show_dialogue("Guardia", "Alt. Zona riservata al Don. Niente appuntamento, niente udienza."),
                ScriptAction.show_dialogue("Guardia", "E niente udienza significa che ve ne tornate a casuccia. O volete un paio di scarpe di cemento?"),
                ScriptAction.show_dialogue(p_name, "Devo passare. Devo parlare con Don Tanino. È urgente."),
                ScriptAction.show_dialogue("Guardia", "(Si aggiusta la cravatta) Tutto è urgente. Ma il Don sta facendo la controra. E quando il Don riposa, non riceve manco il Papa."),
                ScriptAction.show_dialogue(p_name, "(Tra sè e sè) Se attacco rissa mi fa a fettine. Devo usare la testa... o quello che ho trovato nella stanza.")
            ]
            
            # Aggiungi risultato dal modello
            actions = intro_actions + [
                ScriptAction.show_dialogue("Sistema", res["msg"])
            ]
            
            if res["outcome"] == "skip":
                actions.append(ScriptAction.change_room("aurion_boss_room", "bottom"))
            else:
                actions.append(ScriptAction.start_combat(res["encounter_id"]))
                
            return GameScript("aurion_gate_res", actions)

        if script_id == "start_boss_aurion": 
            return GameScript("boss_intro_aur", [
                ScriptAction.show_dialogue("Don Tanino", "Sai... sentivo puzza di povertà fin dalle scale."),
                ScriptAction.show_dialogue("Narratore", "(Si gira completamente. Sorride mostrando denti d'oro.)"),
                ScriptAction.show_dialogue("Don Tanino", "Benvenuto nel mio umile impero. Non capita spesso di vedere facce nuove ad Aurion. Di solito, chi entra qui... lavora per me. O mi deve dei soldi."),
                ScriptAction.show_dialogue(p_name, "Non sono qui per lavorare, Don Tanino. E non ti devo niente. Voglio solo una cosa."),
                ScriptAction.show_dialogue(p_name, "L'Asso di Denari. Sappiamo che ce l'hai tu. Dammelo e me ne vado senza fare danni."),
                ScriptAction.show_dialogue("Don Tanino", "(Ride di gusto, battendo la mano sulla scrivania) Ah! L'Asso! Il mio tesssoro! Sentitelo! Entra in casa mia, con le scarpe sporche, e chiedono il pezzo più pregiato della collezione!"),
                ScriptAction.show_dialogue(p_name, "Ci serve per il Carretto. Per tornare a casa. Non abbiamo tempo per le sceneggiate."),
                ScriptAction.show_dialogue("Narratore", "(Don Tanino si alza. La musica diventa un jazz frenetico.)"),
                ScriptAction.show_dialogue("Don Tanino", "Il tempo è denaro, signorina. E voi me ne state facendo perdere troppo. Volete l'oro? Bene..."),
                ScriptAction.show_dialogue("Narratore", "(Tira fuori carte che brillano di rosso)"),
                ScriptAction.show_dialogue("Don Tanino", "...Vediamo se sei in grado di battermi a scopa!"),
                ScriptAction.change_state(StateID.SCOPA)
            ])

        # =====================================================================
        # 3. REGIONE VIRIDOR (BASTONI)
        # =====================================================================
        
        if script_id == "intro_viridor_entry":
            if not game_model.get_flag("visited_viridor_entry"):
                return GameScript("intro_viridor", [
                    ScriptAction.show_dialogue(p_name, "(Si toglie una spina dalla maglietta) Ahia! Ma che è, filo spinato? Questa vegetazione è... aggressiva."),
                    ScriptAction.show_dialogue("Narratore", "VIRIDOR. Qui la terra è dura come la testa di chi la lavora. Rispetto... o spine."),
                    ScriptAction.show_dialogue("Sistema", "Scegliete una porta (Attenzione: potete sceglierne solo una):"),
                    ScriptAction.show_dialogue("Sistema", "• SINISTRA: 'Tra le spine di un cespuglio spiccano frutti colorati e invitanti.'"),
                    ScriptAction.show_dialogue("Sistema", "• CENTRO: 'Su una pietra antica c'è una bottiglia che riflette la luce.'"),
                    ScriptAction.show_dialogue("Sistema", "• DESTRA: 'Vicino a un muretto crollato spunta un attrezzo di metallo arrugginito.'"),
                    ScriptAction.set_flag("visited_viridor_entry", True)
                ])
            return GameScript("noop", [])

        # LOGICA PORTE
        if script_id == "enter_door_figs": return get_path_lock_script("viridor_starter_received", "viridor_path_figs", "viridor_vault_figs", "bottom")
        if script_id == "enter_door_water": return get_path_lock_script("viridor_starter_received", "viridor_path_water", "viridor_vault_water", "bottom")
        if script_id == "enter_door_shears": return get_path_lock_script("viridor_starter_received", "viridor_path_shears", "viridor_vault_shears", "bottom")
        
        # PICKUP
        if script_id == "pickup_figs":
            msg = game_model.viridor.make_choice(0)
            return GameScript("pk_figs", [
                ScriptAction.show_dialogue(p_name, "Ok, non resisto. Guarda quei colori tra le pale! Devono essere dolcissimi."),
                ScriptAction.show_dialogue("Narratore", "(Si avvicina con cautela al cespuglio e raccoglie i frutti)"),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_water":
            msg = game_model.viridor.make_choice(1)
            return GameScript("pk_water", [
                ScriptAction.show_dialogue(p_name, "Guarda qui. Sembra un'edicola votiva improvvisata. C'è scritto 'Per le anime in pena'."),
                ScriptAction.show_dialogue("Narratore", "(Esamina l'ampolla sulla pietra)"),
                ScriptAction.show_dialogue(p_name, "Acqua benedetta? O almeno... acqua vecchia di dieci anni. Però emana una strana luce."),
                ScriptAction.show_dialogue(p_name, "In questi posti il sacro e il profano si mescolano sempre. Magari funziona contro i mostri... o contro la sfortuna."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_shears":
            msg = game_model.viridor.make_choice(2)
            return GameScript("pk_shears", [
                ScriptAction.show_dialogue(p_name, "Sembrano cesoie da potatura. Sono enormi e arrugginite. Quasi un'arma da film horror."),
                ScriptAction.show_dialogue("Narratore", "(Impugna le cesoie, pesano come una spada)"),
                ScriptAction.show_dialogue("Sistema", msg)
            ])

        # --- GATEKEEPER VIRIDOR (DELEGATO AL MODELLO) ---
        if script_id == "viridor_gate":
            res = game_model.viridor.resolve_gatekeeper()
            
            intro_actions = [
                ScriptAction.show_dialogue("Sphinx", "Miao! Altolà. Questo ponte è vecchio e regge solo chi conosce la verità della terra."),
                ScriptAction.show_dialogue(p_name, "Ci risiamo. Sentiamo l'indovinello."),
                ScriptAction.show_dialogue("Sphinx", "(Fissa il viaggiatore negli occhi) Non servono poemi. La domanda è una sola. Portatemi... 'Il dolce dentro le spine'."),
                ScriptAction.show_dialogue(p_name, "Il dolce dentro le spine? È una metafora? Intendi la gentilezza in un mondo crudele?"),
                ScriptAction.show_dialogue("Sphinx", "Sono un gatto, non un filosofo. Ho fame. Se non avete la risposta, tornate indietro.")
            ]
            
            actions = intro_actions + [
                ScriptAction.show_dialogue("Sistema", res["msg"])
            ]
            
            if res["outcome"] == "skip":
                actions.append(ScriptAction.change_room("viridor_boss_room", "bottom"))
            else:
                actions.append(ScriptAction.start_combat(res["encounter_id"]))
            
            return GameScript("viridor_gate_res", actions)

        if script_id == "start_boss_viridor": 
            return GameScript("boss_intro_vir", [
                ScriptAction.show_dialogue("Nonno Ciccio", "Itivinni! Questa non è terra per turisti!"),
                ScriptAction.show_dialogue(p_name, "Non sono un turista, sono un... viaggiatore. Mi manda Giufa. Mi serve l'Asso di Bastoni."),
                ScriptAction.show_dialogue("Nonno Ciccio", "(Sputa per terra) L'Asso? Il Bastone del comando? Quello serve per guidare il gregge! E voi siete peggio delle pecore smarrite. Non avete rispetto per la terra!"),
                ScriptAction.show_dialogue("Nonno Ciccio", "Se volete l'asso dovrai battermi nella mia specialità, Cucù!"),
                ScriptAction.change_state(StateID.CUCU)
            ])

        # =====================================================================
        # 4. REGIONE FERRUM (SPADE)
        # =====================================================================
        
        if script_id == "intro_ferrum_entry":
            if not game_model.get_flag("visited_ferrum_entry"):
                return GameScript("intro_ferrum", [
                    ScriptAction.show_dialogue(p_name, "(Sventola la mano davanti alla faccia) Miii che cavuru… Sembra di stare a Palermo a mezzogiorno a Ferragosto."),
                    ScriptAction.show_dialogue(p_name, "Sembra una fonderia a cielo aperto. Guarda quei fiumi... non è acqua, è metallo fuso."),
                    ScriptAction.show_dialogue("Narratore", "FERRUM. Qui si forgiano gli eroi... o si sciolgono gli stolti."),
                    ScriptAction.show_dialogue("Sistema", "Scegliete una porta (Attenzione: potete sceglierne solo una):"),
                    ScriptAction.show_dialogue("Sistema", "• SINISTRA: 'Una montagna di rottami che gocciola un liquido nero e viscoso.'"),
                    ScriptAction.show_dialogue("Sistema", "• CENTRO: 'Su una rastrelliera c'è appeso un pezzo di metallo enorme.'"),
                    ScriptAction.show_dialogue("Sistema", "• DESTRA: 'Su un banco ci sono pezzi di legno colorati che stonano col grigio.'"),
                    ScriptAction.set_flag("visited_ferrum_entry", True)
                ])
            return GameScript("noop", [])

        if script_id == "enter_door_oil": return get_path_lock_script("ferrum_starter_received", "ferrum_path_oil", "ferrum_vault_oil", "bottom")
        if script_id == "enter_door_shield": return get_path_lock_script("ferrum_starter_received", "ferrum_path_shield", "ferrum_vault_shield", "bottom")
        if script_id == "enter_door_head": return get_path_lock_script("ferrum_starter_received", "ferrum_path_head", "ferrum_vault_head", "bottom")

        if script_id == "pickup_oil": 
            msg = game_model.ferrum.make_choice(0)
            return GameScript("pk_oil", [
                ScriptAction.show_dialogue(p_name, "Che macello. Sembra che abbiano buttato via mezza zona industriale qui."),
                ScriptAction.show_dialogue("Narratore", "(Sposta una lamiera arrugginita)"),
                ScriptAction.show_dialogue(p_name, "C'è una tanica ancora piena. Puzza di grasso vecchio."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_shield": 
            msg = game_model.ferrum.make_choice(1)
            return GameScript("pk_shield", [
                ScriptAction.show_dialogue(p_name, "Finalmente qualcosa che sembra utile! Miii quantu pisa! È uno Scudo Torre, è alto quanto me!"),
                ScriptAction.show_dialogue(p_name, "È ferro battuto. Chi lo usava doveva essere un gigante. Ti proteggerà da... beh, praticamente da tutto."),
                ScriptAction.show_dialogue(p_name, "Se riesco a tenerlo in mano senza spezzarmi la schiena, sì. Lo prendo."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_head": 
            msg = game_model.ferrum.make_choice(2)
            return GameScript("pk_head", [
                ScriptAction.show_dialogue(p_name, "Ci sono delle bambole su questo banco!"),
                ScriptAction.show_dialogue(p_name, "Aspetta non sono bambole. Sono Pupi. Pupi Siciliani. Ma sono tutti smontati."),
                ScriptAction.show_dialogue("Narratore", "Un Pupo senza testa è solo legno. Una testa senza corpo è solo chiacchiere."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])

        # --- GATEKEEPER FERRUM (DELEGATO AL MODELLO) ---
        if script_id == "ferrum_gate":
            res = game_model.ferrum.resolve_gatekeeper()
            
            intro_actions = [
                ScriptAction.show_dialogue("Narratore", "(Rumore metallico: SCREEECH! CLANG!)"),
                ScriptAction.show_dialogue("Golem", "INTRUSI... RILEVATI. PROTOCOLLO... SCHIACCIAMENTO."),
                ScriptAction.show_dialogue(p_name, "Ma è un Golem di scarti. Perde pezzi mentre cammina. È instabile!")
            ]
            
            actions = intro_actions + [
                ScriptAction.show_dialogue("Sistema", res["msg"])
            ]
            
            # Check Recruitment
            if "flags" in res and "recruit_guest" in res["flags"]: 
                pass # Already handled by model logic usually
            
            if res["outcome"] == "skip":
                actions.append(ScriptAction.change_room("ferrum_boss_room", "bottom"))
            else:
                actions.append(ScriptAction.start_combat(res["encounter_id"]))
            
            return GameScript("ferrum_gate_res", actions)

        if script_id == "start_boss_ferrum": 
            return GameScript("boss_intro_fer", [
                ScriptAction.show_dialogue("Cavaliere Peppino", "Chi osa calpestare il sacro suolo della fucina? Sei un guerriero? O solo scarti da rifondere?"),
                ScriptAction.show_dialogue(p_name, "Sono un viaggiatore. Cerco l'Asso di Spade. Dammi la carta e nessuno si farà male... più o meno."),
                ScriptAction.show_dialogue("Narratore", "(Sguaina la spada: SHIIING!)"),
                ScriptAction.show_dialogue("Cavaliere Peppino", "L'Asso? L'Asso è l'onore! E l'onore si guadagna col sangue e col ferro! Non lo cederò a un... turista in bermuda!"),
                ScriptAction.show_dialogue(p_name, "Ancora con questa storia del turista. Ma ce l'avete tutti con me?"),
                ScriptAction.show_dialogue("Cavaliere Peppino", "In guardia! Preparati a giocare con me a briscola, ma ti avviso non sarà facile!"),
                ScriptAction.change_state(StateID.BRISCOLA)
            ])

        # =====================================================================
        # 5. REGIONE VINALIA (COPPE)
        # =====================================================================
        
        if script_id == "intro_vinalia_entry":
            if not game_model.get_flag("visited_vinalia_entry"):
                return GameScript("intro_vinalia", [
                    ScriptAction.show_dialogue(p_name, "(Ride senza motivo) Perché il pavimento è morbido? Sembra di camminare sui marshmallow."),
                    ScriptAction.show_dialogue(p_name, "Questa intera zona è una gigantesca distilleria a cielo aperto. Respirare qui equivale a farsi tre shot di vodka."),
                    ScriptAction.show_dialogue(p_name, "Devo restare concentrato. Prendo l'asso di Coppe e esco prima di iniziare a vedere gli elefanti rosa."),
                    ScriptAction.show_dialogue("Narratore", "VINALIA. Dove ogni sorso è un ricordo perso. Bevete responsabilmente... o dormite per sempre."),
                    ScriptAction.show_dialogue("Sistema", "Scegliete una porta (Attenzione: potete sceglierne solo una):"),
                    ScriptAction.show_dialogue("Sistema", "• SINISTRA: 'Una festa dove tutti dormono. C'è un fiasco che non finisce mai.'"),
                    ScriptAction.show_dialogue("Sistema", "• CENTRO: 'Che puzza! Vino andato a male... anzi, aceto fortissimo.'"),
                    ScriptAction.show_dialogue("Sistema", "• DESTRA: 'Un suono ritmico (Boing-Boing) proviene da una teca d'oro.'"),
                    ScriptAction.set_flag("visited_vinalia_entry", True)
                ])
            return GameScript("noop", [])

        if script_id == "enter_door_wine": return get_path_lock_script("vinalia_starter_received", "vinalia_path_wine", "vinalia_vault_wine", "bottom")
        if script_id == "enter_door_vinegar": return get_path_lock_script("vinalia_starter_received", "vinalia_path_vinegar", "vinalia_vault_vinegar", "bottom")
        if script_id == "enter_door_marranzano": return get_path_lock_script("vinalia_starter_received", "vinalia_path_marranzano", "vinalia_vault_marranzano", "bottom")

        if script_id == "pickup_wine": 
            msg = game_model.vinalia.make_choice(0)
            return GameScript("pk_win", [
                ScriptAction.show_dialogue(p_name, "Guarda! Una festa! Ma... dormono tutti?"),
                ScriptAction.show_dialogue("Narratore", "(Si avvicina al tavolo)"),
                ScriptAction.show_dialogue(p_name, "C'è un fiasco che non finisce mai. Lo verso e si riempie di nuovo. Magia pura!"),
                ScriptAction.show_dialogue(p_name, "Sembra Vino Eterno. Probabilmente è quello che ha steso tutti questi tizi. Se lo prendo, potrebbe servire per... beh, per stendere qualcuno grosso."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_vinegar": 
            msg = game_model.vinalia.make_choice(1)
            return GameScript("pk_vin", [
                ScriptAction.show_dialogue(p_name, "(Si copre il naso con la maglietta) Che puzza! Qui il vino è andato a male da secoli."),
                ScriptAction.show_dialogue(p_name, "Aspetta... non è andato a male. È aceto. Aceto forte. Mio nonno lo usava per svegliarmi quando dormivo troppo la domenica."),
                ScriptAction.show_dialogue("Narratore", "(Prende una boccetta scura)"),
                ScriptAction.show_dialogue(p_name, "Questo 'Aceto Madre' è così forte che farebbe resuscitare i morti. O farebbe passare la sbronza a un elefante."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])
            
        if script_id == "pickup_marranzano": 
            msg = game_model.vinalia.make_choice(2)
            return GameScript("pk_mar", [
                ScriptAction.show_dialogue(p_name, "Ma questo boing boing? Viene da quella teca."),
                ScriptAction.show_dialogue(p_name, "È un Marranzano! Ma è d'oro! Scommetto che se lo suoni ti sentono fino a Canicattì."),
                ScriptAction.show_dialogue(p_name, "La musica tiene svegli. E rompe gli incantesimi. In un posto dove tutti dormono o sono ubriachi, fare un po' di casino potrebbe essere la salvezza."),
                ScriptAction.show_dialogue("Sistema", msg)
            ])

        # --- GATEKEEPER VINALIA (DELEGATO AL MODELLO) ---
        if script_id == "vinalia_gate":
            res = game_model.vinalia.resolve_gatekeeper()
            
            intro_actions = [
                ScriptAction.show_dialogue("Colapesce", "Ohi... ahi... la schiena... quanto pesa questa terra..."),
                ScriptAction.show_dialogue("Colapesce", "(Nota il gruppo) Chi sei? Non fare rumore... se mi distraggo... crolla tutto..."),
                ScriptAction.show_dialogue(p_name, "È Colapesce! La leggenda dice che regge la Sicilia su una colonna rotta!"),
                ScriptAction.show_dialogue("Colapesce", "Sono stanco... sono millenni che non dormo... millenni che non rido... Datemi pace...")
            ]
            
            actions = intro_actions + [
                ScriptAction.show_dialogue("Sistema", res["msg"])
            ]
            
            if res["outcome"] == "skip":
                actions.append(ScriptAction.change_room("vinalia_boss_room", "bottom"))
            else:
                actions.append(ScriptAction.start_combat(res["encounter_id"]))
            
            return GameScript("vinalia_gate_res", actions)

        if script_id == "start_boss_vinalia": 
            return GameScript("boss_intro_vin", [
                ScriptAction.show_dialogue("Zio Totò", "Benvenuto! Benvenuto! Non restare sulla porta! Qui si beve, si mangia e non si paga!"),
                ScriptAction.show_dialogue(p_name, "Zio Totò, suppongo. Voglio l'Asso di Coppe. Dammelo e me ne vado."),
                ScriptAction.show_dialogue("Zio Totò", "Andartene? Ma se sei appena arrivato! Nessuno se ne va da Vinalia. Qui i ricordi sono così belli che diventano gabbie."),
                ScriptAction.show_dialogue("Narratore", "(Versa del vino viola in calici fluttuanti)"),
                ScriptAction.show_dialogue("Zio Totò", "Un brindisi! Un brindisi all'oblio! Se non bevi con me... mi offendo!"),
                ScriptAction.show_dialogue(p_name, "Non bevo quella roba. È la stessa che ci ha portato qui. Voglio la carta!"),
                ScriptAction.show_dialogue("Zio Totò", "(Il sorriso diventa una smorfia demoniaca) Maleducati! Chi rifiuta il brindisi rifiuta l'amicizia!"),
                ScriptAction.show_dialogue("Zio Totò", "E chi rifiuta l'amicizia... se la gioca con me a sette e mezzo, pregate di non sballare!"),
                ScriptAction.change_state(StateID.SETTE_MEZZO)
            ])

        # =====================================================================
        # HUB GATES INTERACTION
        # =====================================================================
        gates_db = {
            "interact_gate_aurion": ("ace_denari", "aurion_entry", "Aurion"),
            "interact_gate_ferrum": ("ace_spade", "ferrum_entry", "Ferrum"),
            "interact_gate_vinalia": ("ace_coppe", "vinalia_entry", "Vinalia"),
            "interact_gate_viridor": ("ace_bastoni", "viridor_entry", "Viridor"),
        }

        if script_id in gates_db:
            ace_key, target_room, region_name = gates_db[script_id]
            
            # 1. LOCK GLOBALE
            if ace_key in game_model.gamestate.aces_collected:
                return GameScript("locked_global", [
                    ScriptAction.show_dialogue("Sistema", f"Il sigillo di {region_name} è spento. L'Asso è già stato recuperato.")
                ])
            
            # 2. LOCK INDIVIDUALE
            player = game_model.gamestate.get_active_player()
            if player and player.regions_completed >= 2:
                return GameScript("locked_limit", [
                    ScriptAction.show_dialogue(player.name, "Ho già conquistato 2 Regioni. Non posso sostenerne altre."),
                    ScriptAction.show_dialogue("Sistema", "Cambia personaggio (TAB) per esplorare questa regione.")
                ])
            
            # 3. Accesso Consentito
            return GameScript(f"enter_{region_name.lower()}", [
                ScriptAction.change_room(target_room, "from_hub")
            ])

        # =====================================================================
        # OTHER REGIONS (Placeholder Legacy for Compilation)
        # =====================================================================
        
        # Etna (Intro ingresso manuale se necessario, ma ora è automatico)
        if script_id == "intro_etna_entry_entry": return GameScript("noop", [])
        if script_id == "start_boss_etna": return GameScript("boss_oste_start", [ScriptAction.change_state(StateID.BOSS_OSTE)])

        return GameScript("noop", [])