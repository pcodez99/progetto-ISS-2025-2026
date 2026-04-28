"""
Briscola Model - Core logic for the Briscola card game.
Rules: 1v1 (Party vs Boss).
Updated: AI Nerfed (Peppino makes strategic errors).
"""
import random
from dataclasses import dataclass
from typing import List, Optional, Tuple

# --- DATA STRUCTURES ---

@dataclass
class BriscolaCard:
    valore: int  # 1-10 (come scritto sulla carta)
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
    def punti(self) -> int:
        """Valore in punti della carta"""
        if self.valore == 1: return 11  # Asso
        if self.valore == 3: return 10  # Tre
        if self.valore == 10: return 4  # Re
        if self.valore == 9: return 3   # Cavallo
        if self.valore == 8: return 2   # Donna
        return 0 # Scartine (Lisci)

    @property
    def forza(self) -> int:
        """Forza relativa per determinare la presa (a parità di seme)"""
        # Ordine: Asso, 3, Re, Cavallo, Donna, 7, 6, 5, 4, 2
        if self.valore == 1: return 10
        if self.valore == 3: return 9
        if self.valore == 10: return 8
        if self.valore == 9: return 7
        if self.valore == 8: return 6
        return self.valore - 2 # 7->5, 6->4, 5->3, 4->2, 2->0

# --- CONSTANTS ---
SEMI = ["Coppe", "Denari", "Bastoni", "Spade"]
VALORI = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

class BriscolaModel:
    def __init__(self):
        self.mazzo: List[BriscolaCard] = []
        self.mano_player: List[BriscolaCard] = []
        self.mano_cpu: List[BriscolaCard] = []
        
        self.carta_briscola: Optional[BriscolaCard] = None
        self.seme_briscola: str = ""
        
        # Carte giocate sul tavolo nel turno corrente
        # (Carta, Proprietario) -> Proprietario "P" o "C"
        self.tavolo: List[Tuple[BriscolaCard, str]] = [] 
        
        self.punti_player = 0
        self.punti_cpu = 0
        
        # Chi gioca per primo in questo turno (P o C)
        self.turno_iniziale = "P" 
        self.current_turn = "P"
        
        self.state = "INIT" # INIT, PLAYER_TURN, CPU_TURN, RESOLVE_TRICK, GAME_OVER
        self.message = ""
        self.winner = None
        
        # Nerf parameter
        self.mistake_chance = 0.50 # 50% di probabilità di giocare una carta non ottimale

    def start_game(self):
        self._init_deck()
        self.mano_player = []
        self.mano_cpu = []
        self.tavolo = []
        self.punti_player = 0
        self.punti_cpu = 0
        
        # Pesca la briscola (l'ultima carta del mazzo, visibile)
        if len(self.mazzo) > 0:
            self.carta_briscola = self.mazzo[0] # La mettiamo in fondo, ma visivamente è l'ultima
            self.seme_briscola = self.carta_briscola.seme
            
        self.deal_hands()
        
        # Player inizia sempre per convenzione Boss Fight
        self.turno_iniziale = "P"
        self.current_turn = "P"
        self.state = "PLAYER_TURN"
        self.message = "Sfida Peppino a Briscola!"

    def _init_deck(self):
        self.mazzo = [BriscolaCard(v, s) for s in SEMI for v in VALORI]
        random.shuffle(self.mazzo)

    def deal_hands(self):
        """Riempie le mani fino a 3 carte."""
        # Se siamo alla fine, non si pesca
        if not self.mazzo and self.carta_briscola is None:
            return

        # Player pesca
        while len(self.mano_player) < 3:
            card = self._draw_card()
            if card: self.mano_player.append(card)
            else: break
            
        # CPU pesca
        while len(self.mano_cpu) < 3:
            card = self._draw_card()
            if card: self.mano_cpu.append(card)
            else: break

    def _draw_card(self) -> Optional[BriscolaCard]:
        if len(self.mazzo) > 1:
            return self.mazzo.pop() # Pesca dalla cima
        elif len(self.mazzo) == 1:
            # L'ultima carta del mazzo è la Briscola stessa
            last = self.mazzo.pop()
            self.carta_briscola = None # Non è più sotto il mazzo
            return last
        return None

    def play_card_player(self, idx: int) -> bool:
        if not (0 <= idx < len(self.mano_player)): return False
        
        card = self.mano_player.pop(idx)
        self.tavolo.append((card, "P"))
        
        if len(self.tavolo) == 1:
            self.state = "CPU_TURN"
            self.current_turn = "C"
            self.message = f"Hai giocato {card}. Tocca a Peppino."
        else:
            self.state = "RESOLVE_TRICK"
            self.message = f"Hai risposto con {card}."
            
        return True

    def cpu_turn(self):
        """AI di Peppino (NERFATA)."""
        if not self.mano_cpu: return
        
        card_idx = 0
        make_mistake = (random.random() < self.mistake_chance)
        
        # --- LOGICA AI ---
        if len(self.tavolo) == 0:
            # 1. CPU gioca per primo
            
            if make_mistake:
                # ERRORE: Gioca una carta alta (Carico) per primo, regalandola al giocatore
                # Cerca la carta con più punti
                candidates = sorted(range(len(self.mano_cpu)), key=lambda i: self.mano_cpu[i].punti, reverse=True)
                card_idx = candidates[0] 
            else:
                # NORMALE: Gioca la carta più debole (liscia)
                candidates = sorted(range(len(self.mano_cpu)), key=lambda i: self.mano_cpu[i].punti)
                card_idx = candidates[0]
        else:
            # 2. CPU risponde
            card_avversario = self.tavolo[0][0]
            pts_sul_tavolo = card_avversario.punti
            
            winning_moves = []
            losing_moves = []
            
            for i, c in enumerate(self.mano_cpu):
                if self._does_win(c, card_avversario):
                    winning_moves.append(i)
                else:
                    losing_moves.append(i)
            
            if make_mistake and winning_moves:
                # ERRORE: Ha una carta vincente ma decide di NON prenderla (gioca una perdente a caso)
                if losing_moves:
                    card_idx = random.choice(losing_moves)
                else:
                    # Costretto a vincere, ma magari spreca la briscola più alta invece della più bassa
                    winning_moves.sort(key=lambda i: self.mano_cpu[i].forza, reverse=True)
                    card_idx = winning_moves[0]
            else:
                # GIOCA NORMALE (Logica base)
                if pts_sul_tavolo >= 2:
                    if winning_moves:
                        winning_moves.sort(key=lambda i: self.mano_cpu[i].forza)
                        card_idx = winning_moves[0] 
                    else:
                        losing_moves.sort(key=lambda i: self.mano_cpu[i].punti)
                        card_idx = losing_moves[0]
                else:
                    if losing_moves:
                        losing_moves.sort(key=lambda i: self.mano_cpu[i].punti)
                        card_idx = losing_moves[0]
                    else:
                        winning_moves.sort(key=lambda i: self.mano_cpu[i].punti)
                        card_idx = winning_moves[0]

        card = self.mano_cpu.pop(card_idx)
        self.tavolo.append((card, "C"))
        
        if len(self.tavolo) == 1:
            self.state = "PLAYER_TURN"
            self.current_turn = "P"
            self.message = f"Peppino gioca {card}."
        else:
            self.state = "RESOLVE_TRICK"
            self.message = f"Peppino risponde con {card}."

    def _does_win(self, card_a: BriscolaCard, card_b: BriscolaCard) -> bool:
        """Ritorna True se card_a batte card_b (assumendo card_b giocata per prima)."""
        if card_a.seme == card_b.seme:
            return card_a.forza > card_b.forza
        if card_a.seme == self.seme_briscola:
            return True
        return False

    def resolve_trick(self):
        """Calcola chi ha vinto la mano."""
        c1, owner1 = self.tavolo[0]
        c2, owner2 = self.tavolo[1]
        
        points = c1.punti + c2.punti
        winner = owner1 # Default: il primo vince se il secondo non supera
        
        # Logica vittoria (chi gioca per secondo deve superare)
        if c1.seme == c2.seme:
            if c2.forza > c1.forza:
                winner = owner2
        else:
            if c2.seme == self.seme_briscola:
                winner = owner2
        
        # Assegna punti
        if winner == "P":
            self.punti_player += points
            self.message = f"Hai vinto la mano! (+{points} punti)"
            self.turno_iniziale = "P" # Chi vince gioca per primo
        else:
            self.punti_cpu += points
            self.message = f"Peppino vince la mano. (+{points} punti)"
            self.turno_iniziale = "C"
            
        # Pulisci
        self.tavolo = []
        
        # Pesca
        if self.mazzo or self.carta_briscola:
            self._draw_phase(winner)
        
        # Check End
        if not self.mano_player and not self.mano_cpu:
            self.state = "GAME_OVER"
            self._determine_winner()
        else:
            # Imposta turno in base al vincitore
            if self.turno_iniziale == "P":
                self.state = "PLAYER_TURN"
                self.current_turn = "P"
            else:
                self.state = "CPU_TURN"
                self.current_turn = "C"

    def _draw_phase(self, winner):
        """Chi vince pesca per primo."""
        card1 = self._draw_card()
        card2 = self._draw_card()
        
        if not card1: return 

        if winner == "P":
            self.mano_player.append(card1)
            if card2: self.mano_cpu.append(card2)
        else:
            self.mano_cpu.append(card1)
            if card2: self.mano_player.append(card2)

    def _determine_winner(self):
        if self.punti_player > 60: self.winner = "player"
        elif self.punti_cpu > 60: self.winner = "cpu"
        else: self.winner = "draw" # 60 a 60