"""
Ferrum Region Module - Handles logic for Epic 23 and 27.
Updated: RPG Stat Boosts + Visible Passive Abilities.
"""
from src.model.items.item_ids import ItemIds

class FerrumRegion:
    def __init__(self, game):
        self.game = game
        self.completed_flag_key = "region_ferrum_completed"
        
        # Path Flags
        self.PATH_OIL = "ferrum_path_oil"
        self.PATH_SHIELD = "ferrum_path_shield"
        self.PATH_HEAD = "ferrum_path_head"
        self.STARTER_RECEIVED = "ferrum_starter_received"

    def has_made_choice(self) -> bool:
        return self.game.get_flag(self.STARTER_RECEIVED)

    def make_choice(self, selection_idx: int) -> str:
        if self.has_made_choice():
            return "L'armeria ha chiuso i battenti."

        item_id = None
        path_flag = None
        msg = ""
        stat_boost = ""
        passive_name = ""
        
        player = self.game.gamestate.get_active_player()
        if not player: return "Errore."

        if selection_idx == 0:
            # OLIO (Velocità/Magia)
            item_id = ItemIds.OLIO_LUBRIFICANTE
            path_flag = self.PATH_OIL
            
            player.apply_permanent_bonus("speed", 3)
            player.apply_permanent_bonus("magic", 5)
            passive_name = "Ingranaggi Oliati"
            stat_boost = "SPD +3, MAG +5"
            msg = "Hai preso l'Olio Eterno. Ti muovi fluido."
            
        elif selection_idx == 1:
            # SCUDO (Difesa/HP/Res)
            item_id = ItemIds.SCUDO_TORRE
            path_flag = self.PATH_SHIELD
            
            player.apply_permanent_bonus("defense", 5)
            player.apply_permanent_bonus("max_hp", 30)
            player.apply_permanent_bonus("resistance", 2)
            passive_name = "Fortezza Mobile"
            stat_boost = "DIF +5, HP +30"
            msg = "Hai imbracciato lo Scudo Torre."
            
        elif selection_idx == 2:
            # TESTA (Attacco)
            item_id = ItemIds.TESTA_ORLANDO
            path_flag = self.PATH_HEAD
            
            player.apply_permanent_bonus("attack", 6)
            passive_name = "Furia dei Pupi"
            stat_boost = "ATK +6"
            msg = "Hai preso la Testa di Pupi. La furia scorre in te."
        
        if item_id:
            self.game.add_global_item(item_id, 1)
            self.game.set_flag(self.STARTER_RECEIVED, True)
            self.game.set_flag(path_flag, True)
            
            player.inventory.add_item(item_id, f"Oggetto Ferrum. Passiva: {stat_boost}")
            
            # --- AGGIUNTA ABILITÀ VISIBILE ---
            player.learn_special_ability(passive_name, f"[Passiva] {stat_boost}")
            
            return f"{msg}\n(Abilità appresa: {passive_name})"

        return msg

    def resolve_gatekeeper(self) -> dict:
        inv = self.game.inventory_global
        
        if inv.get(ItemIds.OLIO_LUBRIFICANTE, 0) > 0:
            self.game.inventory_global[ItemIds.OLIO_LUBRIFICANTE] -= 1
            return {"outcome": "skip", "msg": "Il Golem scivola sull'olio e si disattiva!"}
            
        elif inv.get(ItemIds.SCUDO_TORRE, 0) > 0:
            # PENALITÀ LIEVE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 20)
            return {"outcome": "skip", "encounter_id": "ferrum_golem_fight", "msg": "Il Golem colpisce lo scudo! L'impatto fa male, ma passate. (-20 HP)"}
            
        elif inv.get(ItemIds.TESTA_ORLANDO, 0) > 0:
            self.game.gamestate.set_guest("pupo_siciliano")
            return {"outcome": "skip", "msg": "'ORLANDO È TORNATO!' Il Golem vi lascia passare."}
            
        else:
            # PENALITÀ PESANTE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 30)
            return {"outcome": "skip", "encounter_id": "ferrum_golem_fight", "msg": "Il Golem vi travolge! Fuggite feriti gravemente. (-30 HP)"}

    def on_boss_victory(self):
        self.game.give_ace("spade")
        self.game.set_flag(self.completed_flag_key, True)
        self.game.return_to_hub()