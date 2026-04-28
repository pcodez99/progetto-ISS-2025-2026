"""
Etna Region Module - Handles logic for Epic 28 (Finale).
US 53/111: Access Check (4 Aces).
US 113: Final Choice (Ending).
"""

class EtnaRegion:
    def __init__(self, game):
        self.game = game
        self.final_ending_flag = "final_ending" # 'bad' or 'true'

    def can_access(self) -> bool:
        """
        Verifica se il giocatore può accedere all'Etna (US 53, US 111).
        Richiede 4 assi.
        """
        return self.game.get_ace_count() >= 4

    def make_final_choice(self, choice_value: str) -> str:
        """
        Gestisce la scelta finale (US 113).
        values: 'bad', 'true'
        """
        self.game.set_flag(self.final_ending_flag, choice_value)
        
        if choice_value == "bad":
            return "Hai usato il Cannolo Bazooka. L'Etna esplode, ma il ciclo ricomincia. (BAD ENDING)"
        elif choice_value == "true":
            return "Offri un brindisi all'Oste. Il vulcano si calma. La Sicilia è libera. (TRUE ENDING)"
        
        return "..."