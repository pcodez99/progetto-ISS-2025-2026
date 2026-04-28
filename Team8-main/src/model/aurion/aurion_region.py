"""
Aurion Region Module - Handles logic for Epic 23 and 27.
Updated: RPG Stat Boosts + Visible Passive Abilities.
"""
from src.model.items.item_ids import ItemIds

class AurionRegion:
    def __init__(self, game):
        self.game = game
        self.completed_flag_key = "region_aurion_completed"
        self.boss_weakened_flag = "aurion_boss_weakened"
        
        # Path-specific flags
        self.PATH_ARANCINA = "aurion_path_arancina"
        self.PATH_MONETE = "aurion_path_monete"
        self.PATH_DOSSIER = "aurion_path_dossier"
        self.STARTER_RECEIVED = "aurion_starter_received"

    def has_made_choice(self) -> bool:
        return self.game.get_flag(self.STARTER_RECEIVED)

    def make_choice(self, selection_idx: int) -> str:
        if self.has_made_choice():
            return "Hai già scelto il tuo destino."

        item_id = None
        path_flag = None
        msg = ""
        stat_boost = ""
        passive_name = ""
        
        player = self.game.gamestate.get_active_player()
        if not player: return "Errore: Nessun giocatore attivo."

        if selection_idx == 0:
            # ARANCINA (Difesa/HP)
            item_id = ItemIds.ARANCINA_CALDA
            path_flag = self.PATH_ARANCINA
            
            player.apply_permanent_bonus("defense", 3)
            player.apply_permanent_bonus("max_hp", 20)
            passive_name = "Dieta Mediterranea"
            stat_boost = "DIF +3, HP +20"
            msg = "Hai mangiato l'Arancina! Ti senti più robusto."
            
        elif selection_idx == 1:
            # MONETE (Attacco)
            item_id = ItemIds.SACCO_MONETE
            path_flag = self.PATH_MONETE
            
            player.apply_permanent_bonus("attack", 4)
            passive_name = "Mani d'Oro"
            stat_boost = "ATK +4"
            msg = "Hai preso l'Oro! Il peso della ricchezza ti rende potente."
            
        elif selection_idx == 2:
            # DOSSIER (Velocità/Critico)
            item_id = ItemIds.FASCICOLO_SEGRETO
            path_flag = self.PATH_DOSSIER
            
            player.apply_permanent_bonus("speed", 2)
            player.apply_permanent_bonus("crit_rate", 10)
            passive_name = "Intelligence"
            stat_boost = "SPD +2, CRIT +10%"
            msg = "Hai letto il Dossier! Conosci i punti deboli del nemico."
        
        if item_id:
            self.game.add_global_item(item_id, 1)
            self.game.set_flag(self.STARTER_RECEIVED, True)
            self.game.set_flag(path_flag, True)
            
            # DESCRIZIONE PARLANTE
            player.inventory.add_item(item_id, f"Oggetto Aurion. Passiva: {stat_boost}")
            
            # --- AGGIUNTA ABILITÀ VISIBILE ---
            player.learn_special_ability(passive_name, f"[Passiva] {stat_boost}")
            
            return f"{msg}\n(Abilità appresa: {passive_name})"

        return msg

    def resolve_gatekeeper(self) -> dict:
        inventory = self.game.inventory_global
        
        if inventory.get(ItemIds.ARANCINA_CALDA, 0) > 0:
            return {"outcome": "skip", "msg": "Guardie: 'Mbare, ma sono calde! Passate pure!'"}
        elif inventory.get(ItemIds.SACCO_MONETE, 0) > 0:
            self.game.inventory_global[ItemIds.SACCO_MONETE] -= 1
            return {"outcome": "skip", "msg": "Le guardie accettano l'oro. 'Avanti!'"}
        elif inventory.get(ItemIds.FASCICOLO_SEGRETO, 0) > 0:
            self.game.set_flag(self.boss_weakened_flag, True)
            return {"outcome": "skip", "msg": "Guardie: 'Il dossier?! Scappiamo!'"}
        else:
            # PENALITÀ HP (NO FIGHT)
            for char in self.game.gamestate.party.main_characters:
                char.hp = max(1, char.hp - 20)
            return {"outcome": "skip", "encounter_id": "aurion_guards_fight", "msg": "Scontro fisico! Passate a forza, ma feriti. (-20 HP)"}

    def on_boss_victory(self):
        self.game.give_ace("Denari") 
        self.game.set_flag(self.completed_flag_key, True)
        self.game.return_to_hub()