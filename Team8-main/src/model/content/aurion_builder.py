"""
Aurion Builder - Static definition of Aurion rooms for Epic 27 testing.
US 108: Gated room structure.
US 109: Interactive console.
US 110: Checkpoints.
"""
import pygame
from src.model.room_data import RoomData, TriggerZone, EntityDefinition, SpawnPoint
from src.model.render_system import CameraMode

class AurionBuilder:
    @staticmethod
    def build_aurion_rooms(content_registry):
        """Popola il registry con le stanze di Aurion."""
        
        # 1. Aurion Entry
        entry = RoomData("aurion_entry_01", name="Aurion Gates", width=800, height=600)
        entry.spawns["from_hub"] = SpawnPoint("from_hub", 400, 550, "up")
        entry.spawns["default"] = SpawnPoint("default", 400, 550, "up")
        entry.is_checkpoint = False
        
        # Exit to Final Stage
        entry.triggers.append(TriggerZone(
            "to_final_stage", pygame.Rect(350, 0, 100, 50), 
            "exit", target_room="aurion_final_stage", target_spawn="entry"
        ))
        
        # Starter Pedestal (US 91)
        entry.entities.append(EntityDefinition(
            "starter_pedestal", "interactable", 400, 300, 
            interaction_label="Pedestal", script_id="aurion_starter_pedestal"
        ))
        
        content_registry.register("rooms", entry.to_dict_dummy()) # Mock for dynamic loading

        # 2. Aurion Final Stage (US 108, 109, 110)
        final = RoomData("aurion_final_stage", name="Proving Grounds", width=800, height=600)
        final.spawns["entry"] = SpawnPoint("entry", 400, 550, "up")
        final.spawns["from_gate"] = SpawnPoint("from_gate", 400, 550, "up")
        final.is_checkpoint = True # US 110
        
        # Security Console (US 109)
        final.entities.append(EntityDefinition(
            "security_console", "interactable", 100, 100, 
            interaction_label="Console", script_id="aurion_security_console"
        ))
        
        # Exit to Boss (Gated US 108)
        final.triggers.append(TriggerZone(
            "to_boss", pygame.Rect(350, 0, 100, 50), 
            "exit", target_room="aurion_boss_room", target_spawn="entry",
            data={
                "req_flag": "aurion_final_stage_cleared", # Gate Logic
                "locked_msg": "Security Grid Active. Hack the console first."
            }
        ))
        
        content_registry.register("rooms", final.to_dict_dummy())

        # 3. Boss Room
        boss = RoomData("aurion_boss_room", name="Don Tanino's Office", width=800, height=600)
        boss.spawns["entry"] = SpawnPoint("entry", 400, 500, "up")
        
        # Trigger Boss Fight (Auto on enter or interact)
        # Per ora usiamo un NPC interagibile
        boss.entities.append(EntityDefinition(
            "don_tanino_npc", "npc", 400, 200,
            interaction_label="Don Tanino",
            actions=[{"label": "Challenge", "script_id": "start_boss_combat"}]
        ))
        
        content_registry.register("rooms", boss.to_dict_dummy())

# Helper per RoomData per evitare dipendenze da JSON serialization in questo mock
def to_dict_dummy(self):
    return {
        "room_id": self.room_id,
        "name": self.name,
        "width": self.width,
        "height": self.height,
        "camera_mode": self.camera_mode.value,
        "spawns": {k: {"x": v.x, "y": v.y} for k, v in self.spawns.items()},
        "entities": [{"entity_id": e.entity_id, "x": e.x, "y": e.y, "script_id": e.script_id, "properties": {"label": e.interaction_label}} for e in self.entities],
        "triggers": [{"rect": [t.rect.x, t.rect.y, t.rect.w, t.rect.h], "trigger_type": t.trigger_type, "target_room": t.target_room, "target_spawn": t.target_spawn, "data": t.data, "requires_confirm": t.requires_confirm, "prompt_text": t.prompt_text} for t in self.triggers],
        "is_checkpoint": self.is_checkpoint
    }
RoomData.to_dict_dummy = to_dict_dummy