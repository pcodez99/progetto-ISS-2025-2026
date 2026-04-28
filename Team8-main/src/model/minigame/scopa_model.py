"""
Scopa Model - Core logic for the Scopa card game.
Adapted for Sicily RPG MVC architecture.
Handles deck, hands, table, capturing logic, and scoring.
Updated: SUPER NERFED AI (Don Tanino plays almost randomly).
"""
import random
import itertools
from dataclasses import dataclass, field
from typing import List, Optional, Tuple, Dict

# --- DATA STRUCTURES ---

@dataclass
class ScopaCard:
    valore: int  # 1-10
    seme: str    # "Coppe", "Denari", "Bastoni", "Spade"
    
    def __repr__(self):
        return f"{self.valore} di {self.seme}"
    
    @property
    def asset_key(self) -> str:
        """Returns the asset key for the AssetManager (e.g. 'coppe_1')"""
        val_str = str(self.valore)
        if self.valore == 8: val_str = "donna"
        elif self.valore == 9: val_str = "cavallo"
        elif self.valore == 10: val_str = "re"
        return f"cards/{self.seme.lower()}_{val_str}"

@dataclass
class ScopaStats:
    carte: int = 0
    denari: int = 0
    settebello: int = 0
    primiera: int = 0
    scope: int = 0
    
    def total_score(self, opponent_stats: 'ScopaStats') -> int:
        score = self.scope
        # Punti di mazzo
        if self.carte > opponent_stats.carte: score += 1
        if self.denari > opponent_stats.denari: score += 1
        if self.settebello > 0: score += 1
        if self.primiera > opponent_stats.primiera: score += 1
        return score

# --- CONSTANTS ---
SEMI = ["Coppe", "Denari", "Bastoni", "Spade"]
VALORI = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
PUNTI_PRIMIERA = {
    7: 21, 6: 18, 1: 16, 5: 15, 4: 14, 3: 13, 2: 12, 8: 10, 9: 10, 10: 10
}

class ScopaModel:
    def __init__(self):
        self.mazzo: List[ScopaCard] = []
        self.mano_player: List[ScopaCard] = []
        self.mano_cpu: List[ScopaCard] = []
        self.tavolo: List[ScopaCard] = []
        
        self.prese_player: List[ScopaCard] = []
        self.prese_cpu: List[ScopaCard] = []
        
        self.scope_player = 0
        self.scope_cpu = 0
        
        self.ultimo_a_prendere = None # "P" or "C"
        self.state = "INIT" # INIT, PLAYER_TURN, CPU_TURN, END_HAND, GAME_OVER
        self.message = ""
        
        # Difficulty Settings
        self.mistake_chance = 0.70  # 70% chance to play BADLY

    def start_game(self):
        """Initialize and shuffle deck, deal first cards."""
        self._init_deck()
        self.prese_player = []
        self.prese_cpu = []
        self.scope_player = 0
        self.scope_cpu = 0
        self.ultimo_a_prendere = None
        
        # Deal initial 4 to table
        for _ in range(4):
            self.tavolo.append(self.mazzo.pop())
            
        self.deal_hands()
        self.state = "PLAYER_TURN"
        self.message = "Tocca a te!"

    def _init_deck(self):
        self.mazzo = [ScopaCard(v, s) for s in SEMI for v in VALORI]
        random.shuffle(self.mazzo)

    def deal_hands(self) -> bool:
        """Deals 3 cards to each player. Returns False if deck empty."""
        if not self.mazzo:
            return False
        
        self.mano_player = []
        self.mano_cpu = []
        
        for _ in range(3):
            if self.mazzo: self.mano_player.append(self.mazzo.pop())
            if self.mazzo: self.mano_cpu.append(self.mazzo.pop())
        return True

    def analizza_presa(self, carta: ScopaCard) -> Dict:
        """
        Analyze what can be captured with 'carta' from 'tavolo'.
        Returns {'tipo': 'calata'|'diretta'|'somma', 'opzioni': [[Card, ...]]}
        """
        # 1. Presa Diretta (Obbligatoria in alcune varianti, qui la priorita')
        dirette = [c for c in self.tavolo if c.valore == carta.valore]
        if dirette:
            return {'tipo': 'diretta', 'opzioni': [[c] for c in dirette]}
        
        # 2. Presa per Somma
        somme = []
        target = carta.valore
        # Combinazioni da 2 a N carte
        for r in range(2, len(self.tavolo) + 1):
            for combo in itertools.combinations(self.tavolo, r):
                if sum(c.valore for c in combo) == target:
                    somme.append(list(combo))
        
        if somme:
            return {'tipo': 'somma', 'opzioni': somme}
            
        return {'tipo': 'calata', 'opzioni': []}

    def play_card(self, card_idx: int, option_idx: int = 0) -> str:
        """
        Player plays a card.
        card_idx: Index in mano_player.
        option_idx: Index of capture option (if multiple).
        """
        if not (0 <= card_idx < len(self.mano_player)):
            return "Invalid card"
            
        carta = self.mano_player.pop(card_idx)
        analisi = self.analizza_presa(carta)
        result_msg = ""
        
        if analisi['tipo'] == 'calata':
            self.tavolo.append(carta)
            result_msg = f"Hai calato il {carta}"
        else:
            # Capture
            opzioni = analisi['opzioni']
            scelta = opzioni[option_idx if 0 <= option_idx < len(opzioni) else 0]
            
            self.prese_player.append(carta)
            self.prese_player.extend(scelta)
            
            for c in scelta:
                if c in self.tavolo: self.tavolo.remove(c)
                
            self.ultimo_a_prendere = "P"
            result_msg = "Presa!"
            
            # Scopa Check
            if not self.tavolo and self.mazzo:
                self.scope_player += 1
                result_msg = "SCOPA!"

        self.state = "CPU_TURN"
        return result_msg

    def cpu_turn(self) -> str:
        """Executes CPU move (DUMB AI)."""
        if not self.mano_cpu:
            return self._check_end_hand()

        # Classifica le carte in mano
        captures = []   # (index, options, type)
        discards = []   # index
        
        for i, carta in enumerate(self.mano_cpu):
            analisi = self.analizza_presa(carta)
            if analisi['tipo'] != 'calata':
                captures.append((i, analisi))
            else:
                discards.append(i)

        chosen_idx = 0
        chosen_action = None # None = calata, else = options list
        
        # LOGICA DI NERF ESTREMA:
        # Se c'è una presa, ha il 70% di probabilità di ignorarla e scartare una carta a caso (se possibile)
        make_mistake = (random.random() < self.mistake_chance)
        
        can_capture = len(captures) > 0
        can_discard = len(discards) > 0
        
        if can_capture:
            if make_mistake and can_discard:
                # ERRORE: Scarta una carta a caso invece di prendere
                chosen_idx = random.choice(discards)
                chosen_action = None
            else:
                # GIOCA (QUASI) BENE: Prende
                # Anche qui, scegliamo la prima presa a caso invece della migliore
                chosen_idx, analisi = random.choice(captures)
                chosen_action = analisi['opzioni'][0]
        else:
            # Solo scarti possibili, sceglie a caso (non ottimizza per lasciare carte scomode)
            chosen_idx = random.choice(discards)
            chosen_action = None

        # Esecuzione Mossa
        carta = self.mano_cpu.pop(chosen_idx)
        msg = ""
        
        if chosen_action is None:
            # Calata
            self.tavolo.append(carta)
            msg = f"Don Tanino cala {carta}"
        else:
            # Presa
            self.prese_cpu.append(carta)
            self.prese_cpu.extend(chosen_action)
            for c in chosen_action:
                if c in self.tavolo: self.tavolo.remove(c)
            
            self.ultimo_a_prendere = "C"
            msg = "Don Tanino ha preso."
            
            if not self.tavolo and self.mazzo:
                self.scope_cpu += 1
                msg = "SCOPA di Don Tanino!"

        self.state = "PLAYER_TURN"
        self._check_end_hand()
        return msg

    def _check_end_hand(self):
        if not self.mano_player and not self.mano_cpu:
            if self.mazzo:
                self.deal_hands()
                return "Nuova mano."
            else:
                # End Game
                if self.tavolo:
                    if self.ultimo_a_prendere == "P":
                        self.prese_player.extend(self.tavolo)
                    else:
                        self.prese_cpu.extend(self.tavolo)
                    self.tavolo = []
                self.state = "GAME_OVER"
                return "Partita finita."
        return ""

    def calculate_stats(self) -> Tuple[ScopaStats, ScopaStats]:
        def calc(prese, scope):
            denari = len([c for c in prese if c.seme == "Denari"])
            settebello = 1 if any(c.valore == 7 and c.seme == "Denari" for c in prese) else 0
            
            # Primiera logic
            migliori = {s: 0 for s in SEMI}
            trovati = {s: False for s in SEMI}
            for c in prese:
                pts = PUNTI_PRIMIERA[c.valore]
                if pts > migliori[c.seme]:
                    migliori[c.seme] = pts
                    trovati[c.seme] = True
            primiera = sum(migliori.values()) if all(trovati.values()) else 0
            
            return ScopaStats(len(prese), denari, settebello, primiera, scope)

        return calc(self.prese_player, self.scope_player), calc(self.prese_cpu, self.scope_cpu)

    def is_game_over(self) -> bool:
        return self.state == "GAME_OVER"

    def get_winner(self) -> str:
        """Returns 'player', 'cpu', or 'draw'"""
        sp, sc = self.calculate_stats()
        score_p = sp.total_score(sc)
        score_c = sc.total_score(sp)
        if score_p > score_c: return "player"
        if score_c > score_p: return "cpu"
        return "draw"