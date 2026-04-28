"""
Cucù Model - Core logic for the Cucu card game (Viridor Boss).
"""
import random
from dataclasses import dataclass
from typing import List, Optional

@dataclass
class CucuCard:
    valore: int  # 1 (Asso) - 10 (Re)
    seme: str    # "Coppe", "Denari", "Bastoni", "Spade"
    
    def __repr__(self):
        return f"{self.valore} di {self.seme}"
    
    @property
    def asset_key(self) -> str:
        val_str = str(self.valore)
        if self.valore == 8: val_str = "donna"
        elif self.valore == 9: val_str = "cavallo"
        elif self.valore == 10: val_str = "re"
        return f"cards/{self.seme.lower()}_{val_str}"
    
    @property
    def is_king(self) -> bool:
        return self.valore == 10

class CucuModel:
    def __init__(self):
        self.mazzo: List[CucuCard] = []
        self.card_player: Optional[CucuCard] = None
        self.card_cpu: Optional[CucuCard] = None
        
        self.lives_player = 3
        self.lives_cpu = 3
        
        self.state = "INIT" # INIT, PLAYER_TURN, CPU_TURN, ROUND_END, GAME_OVER
        self.message = ""
        self.winner = None
        self.round_winner = None # 'player', 'cpu', 'draw'
        
        self.last_action = "" # "swap", "keep", "blocked"

    def start_game(self):
        self.lives_player = 3
        self.lives_cpu = 3
        self.winner = None
        self.start_round()

    def start_round(self):
        self._init_deck()
        self.card_player = self.mazzo.pop()
        self.card_cpu = self.mazzo.pop()
        
        self.state = "PLAYER_TURN"
        self.message = "Tieni o Scambi col Boss?"
        self.last_action = ""
        self.round_winner = None

    def _init_deck(self):
        semi = ["Coppe", "Denari", "Bastoni", "Spade"]
        valori = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        self.mazzo = [CucuCard(v, s) for s in semi for v in valori]
        random.shuffle(self.mazzo)

    def player_action(self, action: str) -> bool:
        """
        action: 'keep' or 'swap'
        Returns True se l'azione è completata, False se illegale.
        """
        if self.state != "PLAYER_TURN": return False
        
        if action == "keep":
            self.last_action = "Player tiene."
            self.state = "CPU_TURN"
            self.message = "La Sphinx sta decidendo..."
            
        elif action == "swap":
            # Check Re del Boss
            if self.card_cpu.is_king:
                self.last_action = "BLOCCATO! CUCÙ!"
                self.message = "Il Boss ha il Re! Scambio annullato."
                # Player tiene la sua carta forzatamente
            else:
                # Scambio
                self.card_player, self.card_cpu = self.card_cpu, self.card_player
                self.last_action = "Scambio effettuato."
                self.message = "Hai preso la carta del Boss."
            
            self.state = "CPU_TURN"
        
        return True

    def cpu_turn(self):
        """
        AI Semplice:
        - Se ha Re -> Tiene (ovvio, vince o pareggia quasi sempre).
        - Se ha <= 3 (carte basse) -> Tenta scambio con mazzo.
        - Se ha > 3 -> Tiene.
        """
        if self.card_cpu.is_king:
            # Tiene (Re vince quasi sempre)
            pass
        elif self.card_cpu.valore <= 4: # Rischia scambio se carta bassa
            if self.mazzo:
                top_deck = self.mazzo[0] # sbircia
                # Se mazzo ha RE, CPU viene bloccata? 
                # Regola standard: se mazzo ha Re, lo scambio fallisce e CPU tiene la sua.
                if top_deck.is_king:
                    self.message += " La Sphinx voleva cambiare ma il mazzo ha fatto CUCÙ!"
                else:
                    self.card_cpu = self.mazzo.pop()
                    self.message += " La Sphinx ha scambiato col mazzo."
        else:
            # Tiene (media/alta)
            pass
            
        self.state = "ROUND_END"
        self._resolve_round()

    def _resolve_round(self):
        val_p = self.card_player.valore
        val_c = self.card_cpu.valore
        
        if val_p < val_c:
            self.lives_player -= 1
            self.round_winner = "cpu"
            self.message = f"Hai perso! {self.card_player} < {self.card_cpu}"
        elif val_c < val_p:
            self.lives_cpu -= 1
            self.round_winner = "player"
            self.message = f"Hai vinto il round! {self.card_player} > {self.card_cpu}"
        else:
            self.round_winner = "draw"
            self.message = f"Pareggio! {self.card_player} = {self.card_cpu}"
            
        if self.lives_player <= 0:
            self.state = "GAME_OVER"
            self.winner = "cpu"
        elif self.lives_cpu <= 0:
            self.state = "GAME_OVER"
            self.winner = "player"