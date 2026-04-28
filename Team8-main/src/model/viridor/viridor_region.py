"""
Viridor Region Module - Handles logic for Epic 23 and 27.
Updated: RPG Stat Boosts + Visible Passive Abilities.
"""
from src.model.items.item_ids import ItemIds

class ViridorRegion:
    def __init__(self, game):
        self.game = game
        self.completed_flag_key = "region_viridor_completed"
        
        # Path Flags
        self.PATH_FIGS = "viridor_path_figs"
        self.PATH_WATER = "viridor_path_water"
        self.PATH_SHEARS = "viridor_path_shears"
        self.STARTER_RECEIVED = "viridor_starter_received"
        self.BOSS_WEAKENED_FLAG = "viridor_goats_shorn"

    def has_made_choice(self) -> bool:
        return self.game.get_flag(self.STARTER_RECEIVED)

    def make_choice(self, selection_idx: int) -> str:
        if self.has_made_choice():
            return "La natura ha già dato."

        item_id = None
        path_flag = None
        msg = ""
        stat_boost = ""
        passive_name = ""
        
        player = self.game.gamestate.get_active_player()
        if not player: return "Errore."

        if selection_idx == 0:
            # FICHI (HP/Res)
            item_id = ItemIds.CESTA_FICHI
            path_flag = self.PATH_FIGS
            
            player.apply_permanent_bonus("max_hp", 40)
            player.apply_permanent_bonus("resistance", 2)
            passive_name = "Pelle di Cactus"
            stat_boost = "HP +40, RES +2"
            msg = "Hai preso la Cesta di Fichi. Il dolce nettare ti rinvigorisce."
            
        elif selection_idx == 1:
            # ACQUA (Magia/Res)
            item_id = ItemIds.ACQUA_BENEDETTA
            path_flag = self.PATH_WATER
            
            player.apply_permanent_bonus("magic", 6)
            player.apply_permanent_bonus("resistance", 4)
            passive_name = "Benedizione"
            stat_boost = "MAG +6, RES +4"
            msg = "Hai preso l'Acqua Benedetta. La tua anima è protetta."
            
        elif selection_idx == 2:
            # CESOIE (Attacco/Crit)
            item_id = ItemIds.CESOIE_ARRUGGINITE
            path_flag = self.PATH_SHEARS
            
            player.apply_permanent_bonus("attack", 4)
            player.apply_permanent_bonus("crit_rate", 15)
            passive_name = "Taglio Netto"
            stat_boost = "ATK +4, CRIT +15%"
            msg = "Hai preso le Cesoie. Uno strumento letale."
        
        if item_id:
            self.game.add_global_item(item_id, 1)
            self.game.set_flag(self.STARTER_RECEIVED, True)
            self.game.set_flag(path_flag, True)
            
            player.inventory.add_item(item_id, f"Oggetto Viridor. Passiva: {stat_boost}")
            
            # --- AGGIUNTA ABILITÀ VISIBILE ---
            player.learn_special_ability(passive_name, f"[Passiva] {stat_boost}")
            
            return f"{msg}\n(Abilità appresa: {passive_name})"

        return msg

    def resolve_gatekeeper(self) -> dict:
        inv = self.game.inventory_global
        
        if inv.get(ItemIds.CESTA_FICHI, 0) > 0:
            return {"outcome": "skip", "msg": "Sphinx: 'Ah, i Fichi.' La creatura fa le fusa e si sposta."}
            
        elif inv.get(ItemIds.ACQUA_BENEDETTA, 0) > 0:
            # PENALITÀ LIEVE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 20)
            return {"outcome": "skip", "msg": "Lanci l'Acqua! La Sphinx stride e vi graffia prima di fuggire. (-20 HP)"}
            
        elif inv.get(ItemIds.CESOIE_ARRUGGINITE, 0) > 0:
            self.game.set_flag(self.BOSS_WEAKENED_FLAG, True)
            # PENALITÀ LIEVE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 20)
            return {"outcome": "skip", "msg": "Colpite con le Cesoie! La Sphinx vi morde prima di ritirarsi. (-20 HP)"}
            
        else:
            # PENALITÀ PESANTE
            for char in self.game.gamestate.party.main_characters: char.hp = max(1, char.hp - 30)
            return {"outcome": "skip", "msg": "La Sphinx vi assale! Passate a stento, sanguinando. (-30 HP)"}

    def on_boss_victory(self):
        self.game.give_ace("bastoni")
        self.game.set_flag(self.completed_flag_key, True)
        self.game.return_to_hub()