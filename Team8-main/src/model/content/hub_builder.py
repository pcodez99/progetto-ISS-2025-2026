"""
Hub Builder - Implementazione Epic 14
Hub Centrale con uscite interattive e Trigger Narrativi.
Updated: Removed unreliable auto-triggers, fixed overlapping Etna gate.
"""
import pygame
from src.model.room_data import RoomData, TriggerZone, EntityDefinition, Collider, SpawnPoint
from src.model.render_system import CameraMode

class HubBuilder:
    @staticmethod
    def create_hub() -> RoomData:
        room = RoomData(
            room_id="hub",
            name="Ombelico della Sicilia",
            width=800,
            height=600,
            camera_mode=CameraMode.FIXED,
            background_color=(50, 50, 60)
        )

        # --- Spawn Points ---
        room.spawns["default"] = SpawnPoint("default", 400, 300, "down")
        room.spawns["from_aurion"] = SpawnPoint("from_aurion", 400, 100, "down")
        room.spawns["from_ferrum"] = SpawnPoint("from_ferrum", 400, 500, "up")
        room.spawns["from_vinalia"] = SpawnPoint("from_vinalia", 100, 300, "right")
        room.spawns["from_viridor"] = SpawnPoint("from_viridor", 700, 300, "left")

        # --- Colliders (Muri) ---
        room.colliders = [
            Collider("wall_top_L", pygame.Rect(0, 0, 350, 20)),
            Collider("wall_top_R", pygame.Rect(450, 0, 350, 20)),
            Collider("wall_bottom_L", pygame.Rect(0, 580, 350, 20)),
            Collider("wall_bottom_R", pygame.Rect(450, 580, 350, 20)),
            Collider("wall_left_U", pygame.Rect(0, 0, 20, 250)),
            Collider("wall_left_D", pygame.Rect(0, 350, 20, 250)),
            Collider("wall_right_U", pygame.Rect(780, 0, 20, 250)),
            Collider("wall_right_D", pygame.Rect(780, 350, 20, 250)),
        ]

        # --- Trigger Gates (Interactive Exits) ---
        room.triggers.append(TriggerZone(id="gate_aurion", rect=pygame.Rect(350, 0, 100, 50), trigger_type="script", script_id="interact_gate_aurion", requires_confirm=False, label="Verso Aurion"))
        room.triggers.append(TriggerZone(id="gate_ferrum", rect=pygame.Rect(350, 550, 100, 50), trigger_type="script", script_id="interact_gate_ferrum", requires_confirm=False, label="Verso Ferrum"))
        room.triggers.append(TriggerZone(id="gate_vinalia", rect=pygame.Rect(0, 250, 50, 100), trigger_type="script", script_id="interact_gate_vinalia", requires_confirm=False, label="Verso Vinalia"))
        room.triggers.append(TriggerZone(id="gate_viridor", rect=pygame.Rect(750, 250, 50, 100), trigger_type="script", script_id="interact_gate_viridor", requires_confirm=False, label="Verso Viridor"))
        
        # FIX: Trigger Etna spostato in alto (y=100) per non sovrapporsi al centro
        room.triggers.append(TriggerZone(id="to_etna", rect=pygame.Rect(380, 100, 40, 40), trigger_type="script", script_id="interact_etna_gate", label="???", auto_trigger=False))

        # --- NPCs e Props ---
        # Giufà (Sinistra)
        room.entities.append(EntityDefinition(
            entity_id="npc_giufa",
            entity_type="npc",
            x=280, y=300, 
            interaction_label="Parla con Giufà",
            script_id="giufa_hub_talk"
        ))

        # Carretto (Destra) - Spostato leggermente per essere ben visibile
        room.entities.append(EntityDefinition(
            entity_id="obj_carretto",
            entity_type="interactable", 
            x=520, y=300,
            interaction_label="Esamina Carretto",
            script_id="interact_carretto",
            properties={"name": "Carretto", "is_carretto": True}
        ))

        return room