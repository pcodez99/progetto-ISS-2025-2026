"""
Sette e Mezzo Model - Core logic for the card game against Zio Totò.
Updated: AI Nerfed extensively (Zio Totò plays scared and doesn't cheat).
"""
import random
from dataclasses import dataclass
from typing import List, Tuple

@dataclass
class SmCard:
    valore: int  # 1-10
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
    def punti(self) -> float:
        """Valore base: 1-7 valgono nominale, 8-9-10 valgono 0.5"""
        if self.valore >= 8: return 0.5
        return float(self.valore)
        
    @property
    def is_matta(self) -> bool:
        """Re di Denari è la Matta"""
        return self.valore == 10 and self.seme == "Denari"

class SetteMezzoModel:
    def __init__(self):
        self.mazzo: List[SmCard] = []
        self.mano_player: List[SmCard] = []
        self.mano_cpu: List[SmCard] = []
        
        self.score_player = 0.0
        self.score_cpu = 0.0
        
        self.state = "INIT" # INIT, PLAYER_TURN, CPU_TURN, GAME_OVER
        self.message = ""
        self.winner = None
        
    def start_game(self):
        self._init_deck()
        self.mano_player = []
        self.mano_cpu = []
        self.score_player = 0.0
        self.score_cpu = 0.0
        self.winner = None
        
        # Distribuzione Iniziale (1 a testa)
        self.player_hit()
        self.cpu_hit() 
        
        self.state = "PLAYER_TURN"
        self.message = "Zio Totò ti sfida! Carta o Stai?"

    def _init_deck(self):
        semi = ["Coppe", "Denari", "Bastoni", "Spade"]
        valori = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        self.mazzo = [SmCard(v, s) for s in semi for v in valori]
        random.shuffle(self.mazzo)

    def _calculate_hand_score(self, hand: List[SmCard]) -> float:
        """Calcola il punteggio gestendo la Matta."""
        matta = None
        score = 0.0
        
        for c in hand:
            if c.is_matta:
                matta = c
            else:
                score += c.punti
                
        if matta:
            # Logica Matta: deve raggiungere il valore più alto possibile <= 7.5
            needed = 7.5 - score
            
            if needed < 0.5:
                score += 0.5
            elif needed >= 7:
                score += 7
            else:
                # Se è intero (es. serve 4), vale 4. Se serve 4.5, vale 4 o 5?
                # La matta vale solo valori interi o 0.5
                # Semplificazione: prova ad avvicinarsi il più possibile a 7.5
                best_s = score + 0.5
                for v in range(1, 8):
                    tentative = score + v
                    if tentative > best_s and tentative <= 7.5:
                        best_s = tentative
                score = best_s
                
        return score

    def player_hit(self):
        """Giocatore chiede carta."""
        if not self.mazzo: return
        card = self.mazzo.pop()
        self.mano_player.append(card)
        self.score_player = self._calculate_hand_score(self.mano_player)
        
        if self.score_player > 7.5:
            self.state = "GAME_OVER"
            self.winner = "cpu"
            self.message = f"Hai sballato! ({self.score_player})"

    def cpu_hit(self):
        """Dà una carta alla CPU."""
        if not self.mazzo: return
        card = self.mazzo.pop()
        self.mano_cpu.append(card)
        self.score_cpu = self._calculate_hand_score(self.mano_cpu)

    def player_stand(self):
        """Giocatore sta."""
        self.state = "CPU_TURN"
        self.message = "Zio Totò sta riflettendo..."

    def cpu_turn(self):
        """Turno del banco (Zio Totò) - VERSIONE FACILITATA."""
        self.score_cpu = self._calculate_hand_score(self.mano_cpu)
        
        # LOGICA AI DEPOTENZIATA (Non guarda le carte del giocatore)
        # Zio Totò è un po' fifone e non calcola bene le probabilità.
        
        should_draw = False
        
        # 1. Se ha un punteggio molto basso, tira sempre
        if self.score_cpu < 4.5:
            should_draw = True
            
        # 2. Se ha un punteggio medio (4.5 - 6.0), ha paura di sballare
        elif 4.5 <= self.score_cpu <= 6.0:
            # Tira solo nel 30% dei casi (molto conservativo)
            if random.random() < 0.3:
                should_draw = True
            else:
                should_draw = False
                
        # 3. Se ha un punteggio alto (> 6.0), si ferma QUASI sempre
        # Anche se sta perdendo contro un 7 del giocatore, potrebbe fermarsi
        else:
            should_draw = False

        # --- ESECUZIONE ---
        if should_draw:
            if not self.mazzo:
                # Mazzo finito, confronto immediato
                self._resolve_winner()
                return

            self.cpu_hit() 
            self.message = "Zio Totò chiede carta..."
            
            if self.score_cpu > 7.5:
                self.state = "GAME_OVER"
                self.winner = "player"
                self.message = f"Zio Totò ha sballato! ({self.score_cpu})"
            else:
                # Se non ha sballato, decide di nuovo nel prossimo tick dell'update loop
                # (Ma per semplicità nel loop attuale, richiamiamo ricorsivamente o usciamo per dare tempo)
                # Qui usciamo e lasciamo che l'update loop richiami cpu_turn
                pass 
        else:
            # Si ferma
            self._resolve_winner()

    def _resolve_winner(self):
        self.state = "GAME_OVER"
        # Il banco vince i pareggi
        if self.score_cpu >= self.score_player:
            self.winner = "cpu"
            self.message = f"Zio Totò vince con {self.score_cpu}!"
        else:
            self.winner = "player"
            self.message = f"Hai vinto! ({self.score_player} vs {self.score_cpu})"