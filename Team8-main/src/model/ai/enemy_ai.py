"""
Enemy AI - Brain Logic
Epic 18: US 76
Epic 23: US 93 (Don Tanino Pattern)
Epic 28: Finale (Oste Eterno)
"""
from typing import Dict, Any

class EnemyBrain:
    """
    Cervello dell'IA che decide l'azione del nemico.
    """
    def __init__(self, behavior_id="aggressive"):
        self.behavior_id = behavior_id

    def decide_action(self, enemy, hero_target, turn_count: int) -> Dict[str, Any]:
        """
        Decide l'azione per il turno corrente.
        Returns:
            {'type': 'attack', 'target': hero, 'move': {...}}
            {'type': 'heal', 'target': enemy, 'amount': X}
            {'type': 'wait'}
            {'type': 'buff', 'name': '...'}
        """
        if self.behavior_id == "healer":
            return self._decide_healer(enemy, hero_target)
        elif self.behavior_id == "boss_pattern":
            return self._decide_boss_generic(enemy, hero_target, turn_count)
        else:
            return self._decide_aggressive(enemy, hero_target)

    def _decide_aggressive(self, enemy, hero):
        # Attacco base
        return {
            "type": "attack",
            "target": hero,
            "move": {"power": 10, "type": "physical", "accuracy": 90, "name": "Claw"}
        }

    def _decide_healer(self, enemy, hero):
        # Se HP < 30%, cura
        if enemy.current_hp < (enemy.max_hp * 0.3):
            return {
                "type": "heal",
                "target": enemy,
                "amount": 20,
                "name": "Self-Repair"
            }
        return self._decide_aggressive(enemy, hero)

    def _decide_boss_generic(self, enemy, hero, turn_count):
        # Pattern Ciclico Generico
        cycle = turn_count % 3
        if cycle == 1:
            return {"type": "buff", "target": enemy, "name": "Power Charge"}
        elif cycle == 2:
             return {"type": "attack", "target": hero, "move": {"power": 30, "type": "magical", "accuracy": 80, "name": "Hyper Beam"}}
        else:
            return {"type": "wait", "name": "Cooldown"}


class DonTaninoBrain(EnemyBrain):
    """
    AI specifica per Don Tanino (US 93).
    Pattern: 1. Cane Shot, 2. Cane Shot, 3. Explosive Coins.
    """
    def __init__(self):
        super().__init__(behavior_id="don_tanino")

    def decide_action(self, enemy, hero_target, turn_count: int) -> Dict[str, Any]:
        # turn_count è 0-based nel CombatState o 1-based? 
        # Assumiamo 0-based dal CombatState update loop, quindi +1 per logica umana
        cycle = (turn_count) % 3 
        
        if cycle == 2: # Turno 3, 6, 9... (Indici 2, 5, 8)
            return {
                "type": "attack",
                "target": hero_target,
                "move": {
                    "power": 25, 
                    "type": "physical", 
                    "accuracy": 85, 
                    "name": "Explosive Coins"
                }
            }
        else: # Turno 1, 2 (Indici 0, 1)
            return {
                "type": "attack",
                "target": hero_target,
                "move": {
                    "power": 12, 
                    "type": "physical", 
                    "accuracy": 95, 
                    "name": "Cane Shot"
                }
            }

class BossOsteBrain(EnemyBrain):
    """
    AI per L'Oste Eterno.
    Le fasi determinano il tipo di attacco.
    """
    def __init__(self, boss_oste_model):
        super().__init__(behavior_id="oste_eterno")
        self.model = boss_oste_model # Riferimento al model di logica (fasi)

    def decide_action(self, enemy, hero_target, turn_count: int) -> Dict[str, Any]:
        phase = self.model.phase
        
        move_name = "Colpo Secco"
        power = 15
        acc = 90
        dmg_type = "physical"

        if phase == 1: # Avidità (Denari)
            move_name = "Conto Salato"
            power = 15 + (turn_count % 5)
            dmg_type = "magical"
        elif phase == 2: # Ostinazione (Bastoni)
            move_name = "Bastone Nodoso"
            power = 20
            dmg_type = "physical"
        elif phase == 3: # Guerra (Spade)
            move_name = "Lama del Tradimento"
            power = 25
            acc = 85
            dmg_type = "physical"
        elif phase == 4: # Oblio (Coppe)
            move_name = "Calice dell'Oblio"
            power = 30
            acc = 100
            dmg_type = "magical"
        
        if self.model.is_immortal:
            move_name = "Eternità"
            power = 999
            acc = 100
            dmg_type = "true"

        return {
            "type": "attack",
            "target": hero_target,
            "move": {
                "power": power,
                "type": dmg_type,
                "accuracy": acc,
                "name": move_name
            }
        }