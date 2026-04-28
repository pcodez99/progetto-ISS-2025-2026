"""
Boss Model - L'Oste Eterno (Final Boss)
Logic for the final battle: Phases, Immortality, and Cheat Abilities.
"""
from dataclasses import dataclass
from typing import Optional

@dataclass
class BossOste:
    """
    Rappresenta lo stato unico dell'Oste Eterno.
    Non è un Enemy standard, ma un boss multi-fase gestito nel Model.
    """
    name: str = "L'OSTE ETERNO"
    hp: int = 100
    max_hp: int = 100
    phase: int = 1
    max_phases: int = 4
    is_immortal: bool = False
    
    # Flags per effetti status unici di questo fight
    stunned: bool = False
    half_damage: bool = False
    vulnerable: bool = False
    miss_next: bool = False
    
    def get_phase_description(self) -> str:
        if self.is_immortal: return "FASE FINALE: ETERNITÀ"
        if self.phase == 1: return "FASE 1: AVIDITÀ (Denari)"
        if self.phase == 2: return "FASE 2: OSTINAZIONE (Bastoni)"
        if self.phase == 3: return "FASE 3: GUERRA (Spade)"
        if self.phase == 4: return "FASE 4: OBLIO (Coppe)"
        return "Unknown"

    def get_phase_color(self) -> tuple:
        # Colori associati ai semi/fasi
        if self.is_immortal: return (240, 240, 255) # Bianco Spettrale
        if self.phase == 1: return (255, 215, 0)    # Gold (Aurion)
        if self.phase == 2: return (50, 200, 50)    # Green (Viridor)
        if self.phase == 3: return (220, 50, 50)    # Red (Ferrum)
        if self.phase == 4: return (148, 0, 211)    # Purple (Vinalia)
        return (255, 255, 255)

    def take_damage(self, damage: int) -> int:
        """Applica danno tenendo conto di flag e immunità."""
        if self.is_immortal:
            return 0
            
        final_damage = damage
        
        # Modificatori
        if self.vulnerable:
            final_damage *= 2
        if self.half_damage:
            final_damage //= 2
            
        # Reset flag one-shot
        self.miss_next = False 
        
        self.hp = max(0, self.hp - final_damage)
        return final_damage

    def check_phase_transition(self) -> bool:
        """
        Controlla se gli HP sono a 0 e avanza di fase.
        Ritorna True se la fase è cambiata.
        """
        if self.hp <= 0:
            if self.phase < self.max_phases:
                self.phase += 1
                self.hp = self.max_phases * 25 + 50 # HP crescono con le fasi
                self.max_hp = self.hp
                # Reset status
                self.stunned = False
                self.half_damage = False
                self.vulnerable = False
                return True
            else:
                # Fine Fase 4 -> Immortality Trigger
                if not self.is_immortal:
                    self.is_immortal = True
                    self.hp = 9999
                    self.max_hp = 9999
                    return True
        return False