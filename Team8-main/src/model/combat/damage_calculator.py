"""
Damage Calculator - Handles math for combat
Epic 18: US 72
"""
from src.model.utils.rng import RNG
from src.model.combat.combat_types import DamageResult

class DamageCalculator:
    def __init__(self, rng: RNG):
        self.rng = rng

    def compute(self, attacker, defender, move_data: dict) -> DamageResult:
        """
        Calcola il danno basato su statistiche e mossa.
        attacker/defender: oggetti che supportano get_stat(name).
        move_data: dict {power, type, accuracy}
        """
        
        # 1. Calcolo HIT/MISS (US 72)
        accuracy = move_data.get("accuracy", 100)
        # Qui potremmo aggiungere evasion del difensore in futuro
        if not self.rng.chance(accuracy):
            return DamageResult(0, False, True, move_data.get("type", "physical"))

        # 2. Calcolo CRIT
        crit_rate = attacker.get_stat("crit_rate") # Default 5 da Character/Enemy
        is_crit = self.rng.chance(crit_rate)

        # 3. Selezione Stats
        move_type = move_data.get("type", "physical")
        power = move_data.get("power", 0)

        if move_type == "magical":
            atk = attacker.get_stat("matk")
            defense = defender.get_stat("mdef")
        else: # physical
            atk = attacker.get_stat("atk")
            defense = defender.get_stat("def")

        # 4. Formula Danno (US 72)
        # (Atk * PowerMult) - (Def * 0.5)
        power_mult = power / 10.0
        base_dmg = (atk * 2 * power_mult) - (defense)
        atk_factor = atk * power_mult
        def_factor = defense * 0.5

        if is_crit:
            base_dmg *= 1.5

        raw_damage = atk_factor - def_factor
        
        # Varianza +/- 10%
        variance = self.rng.randint(90, 110) / 100.0
        final_damage = base_dmg * variance

        # Minimo garantito 1
        final_damage = max(1, int(final_damage))

        return DamageResult(int(final_damage), is_crit, False, move_type)