"""
Action Pipeline - Deterministic execution of combat actions.
Epic 19: US 79
"""
from typing import List, Any, Dict
from src.model.combat.damage_calculator import DamageCalculator
from src.model.utils.rng import RNG
import copy

class ActionPipeline:
    """
    Pipeline di esecuzione deterministica:
    1. Check Costi (Placeholder)
    2. Per ogni target:
       a. Hit Check
       b. Calcolo Danno
       c. Applicazione Danno
       d. Applicazione Status (se Hit)
       e. Death Check immediato
    """
    def __init__(self, calculator: DamageCalculator, rng: RNG):
        self.calculator = calculator
        self.rng = rng

    def execute(self, attacker: Any, targets: List[Any], move_data: Dict[str, Any]) -> List[str]:
        """
        Esegue una mossa su una lista di bersagli.
        Ritorna una lista di stringhe di log per l'UI.
        """
        logs = []
        move_name = move_data.get("name", "Unknown Move")
        
        # 1. Start Log (Optional)
        # logs.append(f"{attacker.name} uses {move_name}!")

        for target in targets:
            if getattr(target, "hp", 0) <= 0:
                logs.append(f"{target.name} is already down!")
                continue

            # 2. Hit/Miss & Damage Calculation
            result = self.calculator.compute(attacker, target, move_data)

            # 3. Resolution
            if result.is_miss:
                logs.append(f"{attacker.name} attacks {target.name}... Miss!")
                continue

            # 4. Apply Damage
            damage = result.damage
            target.current_hp -= damage
            
            hit_msg = f"{attacker.name} hits {target.name} for {damage}"
            if result.is_crit:
                hit_msg += " (CRIT!)"
            logs.append(hit_msg)

            # 5. Apply Status Effects (US 79 Requirement)
            status_template = move_data.get("status_apply")
            if status_template:
                # Creiamo una copia profonda dello status per la nuova istanza
                try:
                    new_status = copy.deepcopy(status_template)
                    target.add_status(new_status)
                    logs.append(f"{target.name} is affected by {new_status.name}!")
                except Exception as e:
                    logs.append(f"Error applying status: {e}")

            # 6. Death Check Immediate
            if target.current_hp <= 0:
                target.current_hp = 0 # Clamp
                logs.append(f"{target.name} collapses!")

        return logs