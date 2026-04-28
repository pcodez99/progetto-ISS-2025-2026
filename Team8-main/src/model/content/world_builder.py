"""
World Builder - Constructs the entire game world.
FULL VERSION: Aurion, Ferrum, Viridor, Vinalia + ETNA (Finale).
Updated: FIXED HUB LAYOUT (Removed overlapping Etna trigger).
"""
import pygame
from src.model.room_data import RoomData, TriggerZone, EntityDefinition, SpawnPoint, Collider
from src.model.render_system import CameraMode

class WorldBuilder:
    @staticmethod
    def build_all(content_registry):
        # Layout Constants
        SCREEN_W = 800
        SCREEN_H = 600
        SPAWN_BOTTOM = (400, 500)

        # =========================================================================
        # 0. DEFINIZIONE LAYOUT SPECIFICI (Muri utente)
        # =========================================================================
        
        # --- AURION ---
        LAYOUT_AURION = {
            "entry": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 200)),
                Collider("wall_L", pygame.Rect(0, 0, 70, 600)),
                Collider("wall_R", pygame.Rect(730, 0, 70, 600)),
                Collider("wall_bL", pygame.Rect(0, 520, 350, 40)),
                Collider("wall_bR", pygame.Rect(450, 520, 350, 40)),
            ],
            "vault": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 160)),
                Collider("wall_L", pygame.Rect(0, 0, 80, 600)),
                Collider("wall_R", pygame.Rect(720, 0, 80, 600)),
                Collider("wall_bot", pygame.Rect(0, 520, 800, 40)),
                Collider("pedestal", pygame.Rect(350, 300, 100, 100))
            ],
            "gate": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 340)),
                Collider("wall_L", pygame.Rect(0, 0, 80, 600)),
                Collider("wall_R", pygame.Rect(720, 0, 80, 600)),
                Collider("wall_bot", pygame.Rect(0, 520, 800, 40))
            ],
            "boss": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 215)),
                Collider("wall_L", pygame.Rect(0, 0, 80, 600)),
                Collider("wall_R", pygame.Rect(720, 0, 80, 600)),
                Collider("wall_bot", pygame.Rect(0, 520, 800, 40)),
                Collider("desk", pygame.Rect(300, 285, 200, 130))

            ]
        }

        # --- FERRUM ---
        LAYOUT_FERRUM = {
            "entry": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 160)), 
                Collider("machinery_L", pygame.Rect(0, 0, 220, 600)),
                Collider("machinery_R", pygame.Rect(580, 0, 120, 600)),
                Collider("wall_bL", pygame.Rect(0, 195, 350, 600)),
                Collider("wall_bR", pygame.Rect(450, 195, 350, 600)),
            ],
            "vault": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 170)),
                Collider("pipes_L", pygame.Rect(0, 0, 100, 600)),
                Collider("pipes_R", pygame.Rect(700, 0, 100, 600)),
                Collider("wall_bL", pygame.Rect(0, 430, 320, 170)),
                Collider("wall_bR", pygame.Rect(480, 430, 310, 170)),
                Collider("anvil_base", pygame.Rect(350, 260, 80, 60))
            ],
            "gate": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 193)),
                Collider("scrap_L", pygame.Rect(0, 0, 320, 600)),
                Collider("scrap_R", pygame.Rect(480, 0, 150, 600)),
                Collider("wall_bot", pygame.Rect(0, 580, 800, 40))
            ],
            "boss": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 240)),
                Collider("wall_L", pygame.Rect(0, 0, 158, 600)), 
                Collider("wall_R", pygame.Rect(642, 0, 158, 600)),
                Collider("wall_bot", pygame.Rect(0, 500, 800, 118))
            ]
        }

        # --- VIRIDOR ---
        LAYOUT_VIRIDOR = {
            "entry": [
                Collider("brambles_top", pygame.Rect(0, 0, 800, 85)),
                Collider("trees_L", pygame.Rect(0, 0, 80, 600)),
                Collider("trees_R", pygame.Rect(720, 0, 80, 600)),
                Collider("wall_bL", pygame.Rect(0, 515, 350, 85)),
                Collider("wall_bR", pygame.Rect(450, 515, 350, 85)),
            ],
            "vault": [
                Collider("trees_top", pygame.Rect(0, 0, 800, 85)),
                Collider("trees_L", pygame.Rect(0, 0, 90, 600)),
                Collider("trees_R", pygame.Rect(710, 0, 90, 600)),
                Collider("wall_bL", pygame.Rect(50, 515, 300, 85)),
                Collider("wall_bR", pygame.Rect(450, 515, 300, 85)),
                Collider("stump", pygame.Rect(315, 232, 165, 130))
            ],
            "gate": [
                Collider("ruins_top", pygame.Rect(0, 0, 800, 300)),
                Collider("ruins_L", pygame.Rect(0, 0, 90, 600)),
                Collider("ruins_R", pygame.Rect(710, 0, 90, 600)),
                Collider("wall_bot", pygame.Rect(0, 515, 800, 85))
            ],
            "boss": [
                Collider("boss_house", pygame.Rect(270, 0, 260, 187)),
                Collider("cliff_top", pygame.Rect(0, 0, 800, 96)),
                Collider("void_L", pygame.Rect(0, 0, 90, 600)),
                Collider("void_R", pygame.Rect(710, 0, 90, 600)),
                Collider("wall_bot", pygame.Rect(0, 515, 800, 85))
            ]
        }

        # --- VINALIA ---
        LAYOUT_VINALIA = {
            "entry": [
                Collider("wall_top", pygame.Rect(0, 0, 800, 180)),
                Collider("barrels_L", pygame.Rect(0, 0, 90, 600)),
                Collider("barrels_R", pygame.Rect(710, 0, 90, 600)),
                Collider("crates_BL", pygame.Rect(0, 250, 340, 150)),
                Collider("crates_BR", pygame.Rect(450, 250, 340, 150)),
                Collider("wall_bL", pygame.Rect(0, 520, 350, 40)),
                Collider("wall_bR", pygame.Rect(450, 520, 350, 40)),
            ],
            "vault": [
                Collider("shelf_top", pygame.Rect(0, 0, 800, 160)),
                Collider("shelf_L", pygame.Rect(0, 0, 80, 600)),
                Collider("shelf_R", pygame.Rect(720, 0, 80, 600)),
                Collider("wall_bL", pygame.Rect(0, 520, 300, 40)),
                Collider("wall_bR", pygame.Rect(500, 520, 300, 40)),
                Collider("barrel_table", pygame.Rect(340, 250, 120, 160))
            ],
            "gate": [
                Collider("dark_water_top", pygame.Rect(0, 0, 800, 200)),
                Collider("pier_L", pygame.Rect(0, 0, 50, 600)),
                Collider("pier_R", pygame.Rect(750, 0, 50, 600)),
                Collider("wall_bot", pygame.Rect(0, 520, 800, 40))
            ],
            "boss": [
                Collider("table_top", pygame.Rect(0, 0, 800, 200)),
                Collider("table", pygame.Rect(200, 0, 400, 280)),
                Collider("columns_L", pygame.Rect(0, 0, 90, 600)),
                Collider("columns_R", pygame.Rect(700, 0, 90, 600)),
                Collider("wall_bot", pygame.Rect(0, 530, 800, 40))
            ]
        }

        # --- ETNA (FINALE) ---
        LAYOUT_ETNA = {
            "entry": [
                Collider("magma_top_L", pygame.Rect(0, 0, 350, 150)),
                Collider("magma_top_R", pygame.Rect(450, 0, 350, 150)),
                Collider("magma_L", pygame.Rect(0, 0, 150, 600)),
                Collider("magma_R", pygame.Rect(650, 0, 150, 600)),
                Collider("wall_bot", pygame.Rect(0, 550, 800, 50))
            ],
            "boss": [
                Collider("crater_top", pygame.Rect(0, 0, 800, 100)),
                Collider("crater_L", pygame.Rect(0, 0, 100, 600)),
                Collider("crater_R", pygame.Rect(700, 0, 100, 600)),
                Collider("crater_bot", pygame.Rect(0, 500, 800, 100)),
                Collider("altar", pygame.Rect(350, 200, 100, 100))
            ]
        }

        # =========================================================================
        # 0.1 CONFIGURAZIONI POSIZIONALI
        # =========================================================================
        REGION_CONFIGS = {
            "aurion": { "layout": LAYOUT_AURION, "door_x": [180, 360, 540], "door_y": 200, "vault_exit_y": 160, "item_pos": (400, 300), "gate_npc_pos": (400, 380), "boss_pos": (430, 220) },
            "ferrum": { "layout": LAYOUT_FERRUM, "door_x": [230, 355, 488], "door_y": 160, "vault_exit_y": 160, "item_pos": (390, 250), "gate_npc_pos": (400, 350), "boss_pos": (400, 290) },
            "viridor": { "layout": LAYOUT_VIRIDOR, "door_x": [180, 360, 540], "door_y": 90, "vault_exit_y": 90, "item_pos": (395, 260), "gate_npc_pos": (400, 350), "boss_pos": (400, 250) },
            "vinalia": { "layout": LAYOUT_VINALIA, "door_x": [160, 360, 560], "door_y": 180, "vault_exit_y": 160, "item_pos": (400, 280), "gate_npc_pos": (400, 300), "boss_pos": (400, 210) }
        }

        # =========================================================================
        # 1. HUB CENTRALE (UPDATED FIXED VERSION)
        # =========================================================================
        hub = RoomData("hub", name="Ombelico della Sicilia", width=SCREEN_W, height=SCREEN_H, 
                       camera_mode=CameraMode.FIXED, background_id="hub")
        
        # --- Spawn Points ---
        hub.spawns["default"] = SpawnPoint("default", 400, 300, "down") # Centro esatto
        hub.spawns["from_aurion"] = SpawnPoint("from_aurion", 400, 100, "down")
        hub.spawns["from_ferrum"] = SpawnPoint("from_ferrum", 400, 500, "up")
        hub.spawns["from_vinalia"] = SpawnPoint("from_vinalia", 100, 300, "right")
        hub.spawns["from_viridor"] = SpawnPoint("from_viridor", 700, 300, "left")

        # --- Colliders (Muri) ---
        hub.colliders = [
            Collider("confine_superiore", pygame.Rect(0, 0, 800, 80)),
            Collider("muro_botti", pygame.Rect(80+60, 0, 60, 205+75)),
            Collider("confine_destro", pygame.Rect(795, 0, 30, 600)),
            Collider("confine_inferiore", pygame.Rect(0, 595, 800, 5)),
            Collider("wall_top_L", pygame.Rect(0, 0, 370, 170)),
            Collider("wall_top_R", pygame.Rect(430, 0, 380, 170)),
            Collider("wall_bottom_L", pygame.Rect(0, 490, 340, 110)),
            Collider("wall_bottom_R", pygame.Rect(460, 490, 340, 110)),
            Collider("wall_left_U", pygame.Rect(0, 0, 205, 230)),
            Collider("wall_left_D", pygame.Rect(0, 210, 80, 390)),
            Collider("wall_right_U", pygame.Rect(590, 0, 210, 270)),
            Collider("wall_right_D", pygame.Rect(590, 345, 210, 255)),
        ]

        # --- Trigger Gates (Interactive Exits) ---
        hub.triggers.append(TriggerZone(id="gate_aurion", rect=pygame.Rect(380, 80, 40, 40), trigger_type="script", script_id="interact_gate_aurion", requires_confirm=False, label="Verso Aurion"))
        hub.triggers.append(TriggerZone(id="gate_ferrum", rect=pygame.Rect(340, 500, 120, 100), trigger_type="script", script_id="interact_gate_ferrum", requires_confirm=False, label="Verso Ferrum"))
        hub.triggers.append(TriggerZone(id="gate_vinalia", rect=pygame.Rect(80, 210, 60, 60), trigger_type="script", script_id="interact_gate_vinalia", requires_confirm=False, label="Verso Vinalia"))
        hub.triggers.append(TriggerZone(id="gate_viridor", rect=pygame.Rect(710, 250, 50, 100), trigger_type="script", script_id="interact_gate_viridor", requires_confirm=False, label="Verso Viridor"))
        
        # --- FIX: RIMOSSO TRIGGER INVISIBILE SOVRAPPOSTO AL CENTRO ---
        # L'accesso all'Etna ora avviene SOLO tramite il Carretto.
        
        # --- NPCs e Props ---
        # Giufà (Sinistra)
        hub.entities.append(EntityDefinition(
            entity_id="npc_giufa",
            entity_type="npc",
            x=280, y=300, 
            interaction_label="Parla con Giufà",
            script_id="giufa_hub_talk"
        ))

        # Carretto (Destra) - Spostato leggermente per essere ben visibile
        hub.entities.append(EntityDefinition(
            entity_id="obj_carretto",
            entity_type="interactable", 
            x=520, y=300,
            interaction_label="Esamina Carretto",
            script_id="interact_carretto",
            properties={"name": "Carretto", "is_carretto": True}
        ))

        content_registry.register("rooms", { "id": hub.room_id, "obj": hub })

        # =========================================================================
        # 2. ETNA REGION (FINALE)
        # =========================================================================
        etna_e = RoomData("etna_entry", name="Cratere Centrale", width=SCREEN_W, height=SCREEN_H, background_id="bg_etna_entry")
        etna_e.spawns["bottom"] = SpawnPoint("bottom", 400, 500, "up")
        etna_e.colliders = list(LAYOUT_ETNA["entry"])
        etna_e.triggers.append(TriggerZone("to_boss", pygame.Rect(350, 0, 100, 50), "exit", target_room="etna_boss_room", target_spawn="bottom", label="Affronta il Destino"))
        etna_e.triggers.append(TriggerZone("intro_etna", pygame.Rect(350, 450, 100, 100), "script", script_id="intro_etna_entry_entry", auto_trigger=True))
        content_registry.register("rooms", { "id": etna_e.room_id, "obj": etna_e })

        etna_b = RoomData("etna_boss_room", name="Tavola dell'Oste", width=SCREEN_W, height=SCREEN_H, background_id="bg_etna_hall")
        etna_b.spawns["bottom"] = SpawnPoint("bottom", 400, 450, "up")
        etna_b.colliders = list(LAYOUT_ETNA["boss"])
        etna_b.entities.append(EntityDefinition("boss_oste", "enemy", 400, 200, interaction_label="L'Oste Eterno", script_id="start_boss_etna"))
        content_registry.register("rooms", { "id": etna_b.room_id, "obj": etna_b })

        # =========================================================================
        # 3. OTHER REGIONS (HELPER BUILDER)
        # =========================================================================
        def build_region(prefix, name, item_configs, gatekeeper_cfg, boss_cfg):
            config = REGION_CONFIGS[prefix]; layout = config["layout"]
            entry_id = f"{prefix}_entry"
            entry = RoomData(entry_id, name=f"{name}: Atrio", width=SCREEN_W, height=SCREEN_H, background_id=f"bg_{prefix}_hall")
            entry.spawns["bottom"] = SpawnPoint("bottom", *SPAWN_BOTTOM)
            entry.spawns["top_left"] = SpawnPoint("top_left", 150, 100); entry.spawns["top_center"] = SpawnPoint("top_center", 400, 100); entry.spawns["top_right"] = SpawnPoint("top_right", 650, 100)
            entry.spawns["from_hub"] = SpawnPoint("from_hub", *SPAWN_BOTTOM)
            entry.colliders = list(layout["entry"])
            entry.triggers.append(TriggerZone("back", pygame.Rect(350, 550, 100, 50), "exit", target_room="hub", target_spawn=f"from_{prefix}", label="Torna all'Hub"))
            entry.triggers.append(TriggerZone(f"intro_{prefix}", pygame.Rect(SPAWN_BOTTOM[0]-50, SPAWN_BOTTOM[1]-50, 100, 100), "script", script_id=f"intro_{prefix}_entry", auto_trigger=True))
            door_y = config["door_y"]; door_xs = config["door_x"]
            doors = [("door_l", pygame.Rect(door_xs[0], door_y, 80, 50), item_configs[0][0], item_configs[0][1]), ("door_c", pygame.Rect(door_xs[1], door_y, 80, 50), item_configs[1][0], item_configs[1][1]), ("door_r", pygame.Rect(door_xs[2], door_y, 80, 50), item_configs[2][0], item_configs[2][1])]
            for d_id, rect, suffix, label in doors: entry.triggers.append(TriggerZone(d_id, rect, "script", script_id=f"enter_door_{suffix}", label=f"Entra ({label})"))
            content_registry.register("rooms", { "id": entry.room_id, "obj": entry })

            gatekeeper_room_id = f"{prefix}_gatekeeper"; item_x, item_y = config["item_pos"]; vault_exit_y = config.get("vault_exit_y", 160)
            for i, (suffix, label, script_pk, sprite) in enumerate(item_configs):
                vid = f"{prefix}_vault_{suffix}"; v = RoomData(vid, name=f"{name}: Caveau {label}", width=SCREEN_W, height=SCREEN_H, background_id=f"bg_{prefix}_vault")
                v.spawns["bottom"] = SpawnPoint("bottom", 400, 480)
                v.colliders = list(layout["vault"])
                v.entities.append(EntityDefinition(sprite, "interactable", item_x, item_y, interaction_label=label, script_id=script_pk, once_flag=f"{prefix}_starter_received"))
                v.triggers.append(TriggerZone("fwd", pygame.Rect(350, vault_exit_y, 100, 50), "exit", target_room=gatekeeper_room_id, target_spawn="bottom", label="Procedi"))
                v.triggers.append(TriggerZone("bwd", pygame.Rect(300, 550, 200, 50), "exit", target_room=entry_id, target_spawn=["top_left", "top_center", "top_right"][i], label="Torna Indietro"))
                content_registry.register("rooms", { "id": v.room_id, "obj": v })

            g = RoomData(gatekeeper_room_id, name=f"{name}: Dogana", width=SCREEN_W, height=SCREEN_H, background_id=f"bg_{prefix}_gate")
            g.spawns["bottom"] = SpawnPoint("bottom", 400, 450)
            g.colliders = list(layout["gate"])
            npc_x, npc_y = config["gate_npc_pos"]
            g.entities.append(EntityDefinition(gatekeeper_cfg["sprite"], "npc", npc_x, npc_y, interaction_label=gatekeeper_cfg["name"], script_id=f"{prefix}_gate"))
            content_registry.register("rooms", { "id": g.room_id, "obj": g })

            bid = f"{prefix}_boss_room"; b = RoomData(bid, name=f"{name}: Sala del Trono", width=SCREEN_W, height=SCREEN_H, background_id=f"bg_{prefix}_boss")
            b.spawns["bottom"] = SpawnPoint("bottom", 400, 450)
            b.colliders = list(layout["boss"])
            boss_x, boss_y = config["boss_pos"]
            b.entities.append(EntityDefinition(boss_cfg["sprite"], "enemy", boss_x, boss_y, interaction_label=boss_cfg["name"], script_id=f"start_boss_{prefix}"))
            content_registry.register("rooms", { "id": b.room_id, "obj": b })

        build_region("aurion", "Aurion", [("arancina", "Arancina", "pickup_arancina", "item_arancina"), ("monete", "Monete", "pickup_monete", "item_sacco"), ("dossier", "Dossier", "pickup_dossier", "item_dossier")], {"name": "Guardie d'Elite", "sprite": "npc_guards"}, {"name": "Don Tanino", "sprite": "enemy_tanino"})
        build_region("ferrum", "Ferrum", [("oil", "Olio Eterno", "pickup_oil", "item_oil"), ("shield", "Scudo Torre", "pickup_shield", "item_shield"), ("head", "Testa Pupi", "pickup_head", "item_head")], {"name": "Golem di Scarti", "sprite": "enemy_golem"}, {"name": "Cavalier Peppino", "sprite": "enemy_peppino"})
        build_region("viridor", "Viridor", [("figs", "Fichi", "pickup_figs", "item_figs"), ("water", "Acqua Santa", "pickup_water", "item_water"), ("shears", "Cesoie", "pickup_shears", "item_shears")], {"name": "La Sphinx", "sprite": "enemy_sphinx"}, {"name": "Nonno Ciccio", "sprite": "enemy_ciccio"})
        build_region("vinalia", "Vinalia", [("wine", "Vino Eterno", "pickup_wine", "item_wine"), ("vinegar", "Aceto Madre", "pickup_vinegar", "item_vinegar"), ("marranzano", "Marranzano", "pickup_marranzano", "item_marranzano")], {"name": "Colapesce", "sprite": "enemy_colapesce"}, {"name": "Zio Totò", "sprite": "enemy_toto"})