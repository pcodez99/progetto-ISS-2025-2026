"""
Vinalia Region Module - Handles logic for Epic 23 and 27.
Updated: RPG Stat Boosts + Visible Passive Abilities.
"""
from src.model.items.item_ids import ItemIds
from src.model.status.status_effects import ResistanceBuff

class VinaliaRegion:
    def __init__(self, game):
        self.game = game
        self.completed_flag_key = "region_vinalia_completed"
        
        # Path Flags
        self.PATH_WINE = "vinalia_path_wine"
        self.PATH_VINEGAR = "vinalia_path_vinegar"
        self.PATH_MARRANZANO = "vinalia_path_marranzano"
        self.STARTER_RECEIVED = "vinalia_starter_received"

    def has_made_choice(self) -> bool:
        return self.game.get_flag(self.STARTER_RECEIVED)

    def make_choice(self, selection_idx: int) -> str:
        if self.has_made_choice():
            return "Il calice è tratto."

        item_id = None
        path_flag = None
        msg = ""
        stat_boost = ""
        passive_name = ""
        
        player = self.game.gamestate.get_active_player()
        if not player: return "Errore."

        if selection_idx == 0:
            # VINO (HP/Difesa)
            item_id = ItemIds.VINO_ETERNO
            path_flag = self.PATH_WINE
            
            player.apply_permanent_bonus("max_hp", 30)
            player.apply_permanent_bonus("defense", 3)
            passive_name = "Anestesia Etilica"
            stat_boost = "HP +30, DIF +3"
            msg = "Hai bevuto il Vino Eterno. Non senti più dolore."
            
        elif selection_idx == 1:
            # ACETO (Attacco/Magia)
            item_id = ItemIds.ACETO_MADRE
            path_flag = self.PATH_VINEGAR
            
            player.apply_permanent_bonus("attack", 3)
            player.apply_permanent_bonus("magic", 4)
            passive_name = "Sangue Acido"
            stat_boost = "ATK +3, MAG +4"
            msg = "Hai preso l'Aceto Madre. Un potere acido ti scorre nelle vene."
            
        elif selection_idx == 2:
            # MARRANZANO (Velocità/Res)
            item_id = ItemIds.MARRANZANO_ORO
            path_flag = self.PATH_MARRANZANO
            
            player.apply_permanent_bonus("speed", 4)
            player.apply_permanent_bonus("resistance", 4)
            passive_name = "Ritmo Ipnotico"
            stat_boost = "SPD +4, RES +4"
            msg = "Hai preso il Marranzano d'Oro. Il ritmo ti rende intoccabile."
        
        if item_id:
            self.game.add_global_item(item_id, 1)
            self.game.set_flag(self.STARTER_RECEIVED, True)
            self.game.set_flag(path_flag, True)
            
            player.inventory.add_item(item_id, f"Oggetto Vinalia. Passiva: {stat_boost}")
            
            # Logica immediata Marranzano (Bonus Squadra Extra)
            if item_id == ItemIds.MARRANZANO_ORO:
                for p in self.game.gamestate.players:
                    if p != player: p.apply_permanent_bonus("resistance", 1)

            # --- AGGIUNTA ABILITÀ VISIBILE ---
            player.learn_special_ability(passive_name, f"[Passiva] {stat_boost}")
            
            return f"{msg}\n(Abilità appresa: {passive_name})"

        return msg

    def resolve_gatekeeper(self) -> dict:
        inv = self.game.inventory_global
        
        if inv.get(ItemIds.VINO_ETERNO, 0) > 0:
            return {"outcome": "skip", "msg": "Colapesce beve e crolla addormentato. Passate."}
            
        elif inv.get(ItemIds.ACETO_MADRE, 0) > 0:
            # PENALITÀ LIEVE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 20)
            return {"outcome": "skip", "msg": "Colapesce annusa l'Aceto ed esplode di rabbia! Vi colpisce con detriti. (-20 HP)"}
            
        elif inv.get(ItemIds.MARRANZANO_ORO, 0) > 0:
            return {"outcome": "skip", "msg": "Suoni il Marranzano. Colapesce si incanta e vi apre la via."}
            
        else:
            # PENALITÀ PESANTE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 30)
            return {"outcome": "skip", "msg": "Colapesce vi scaccia con una manata tremenda! Siete schiacciati. (-30 HP)"}

    def on_boss_victory(self):
        self.game.give_ace("coppe")
        self.game.set_flag(self.completed_flag_key, True)
        self.game.return_to_hub()