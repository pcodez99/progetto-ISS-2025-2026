"""
Save/Load Menu - Interfaccia utente per salvataggio e caricamento
Epic 5: User Stories 16, 17
"""

from src.model.save import SlotStatus

class SaveLoadMenu:
    """Menu per il salvataggio e caricamento del gioco"""
    
    def __init__(self, controller):
        self.controller = controller
        self.current_view = "slot_list"  # slot_list, confirm_overwrite, message
        self.selected_slot = None
        self.pending_message = None
        self.pending_action = None  # 'save' o 'load'
    
    # ============== DISPLAY METHODS ==============
    
    def display_slot_list(self, mode: str = "save"):
        """
        Mostra la lista degli slot.
        
        Args:
            mode: 'save' o 'load'
        """
        slots = self.controller.get_save_slots()
        
        print(f"\n=== {'SAVE' if mode == 'save' else 'LOAD'} GAME ===")
        print("-" * 40)
        
        for slot in slots:
            print(slot.get_display_text())
        
        print("-" * 40)
        print("Select slot (1-3) or 0 to cancel")
        
        return slots
    
    def display_confirm_overwrite(self, slot_index: int):
        """Mostra il dialogo di conferma sovrascrittura"""
        slot_info = self.controller.get_slot_info(slot_index)
        
        print(f"\n=== CONFIRM OVERWRITE ===")
        print(f"Slot {slot_index} contains an existing save:")
        if slot_info.meta:
            print(f"  {slot_info.meta.format_display()}")
        print("\nOverwrite this save? (Y/N)")
    
    def display_message(self, message: str, is_error: bool = False):
        """Mostra un messaggio all'utente"""
        prefix = "[ERROR]" if is_error else "[INFO]"
        print(f"\n{prefix} {message}")
    
    # ============== SAVE FLOW ==============
    
    def open_save_menu(self):
        """Apre il menu di salvataggio"""
        # Verifica se il salvataggio è permesso
        can_save, message = self.controller.can_save_game()
        
        if not can_save:
            self.display_message(message, is_error=True)
            return False
        
        self.current_view = "slot_list"
        self.pending_action = "save"
        self.display_slot_list(mode="save")
        return True
    
    def select_save_slot(self, slot_index: int) -> dict:
        """
        Seleziona uno slot per il salvataggio.
        
        Returns:
            dict con 'status': 'saved', 'confirm_required', 'error', 'cancelled'
        """
        if slot_index == 0:
            return {'status': 'cancelled', 'message': 'Save cancelled'}
        
        if slot_index < 1 or slot_index > 3:
            return {'status': 'error', 'message': 'Invalid slot'}
        
        # Tenta il salvataggio
        result = self.controller.save_game(slot_index, confirmed=False)
        
        if result.message == "CONFIRM_OVERWRITE":
            self.selected_slot = slot_index
            self.current_view = "confirm_overwrite"
            self.display_confirm_overwrite(slot_index)
            return {'status': 'confirm_required', 'message': 'Confirm overwrite?'}
        
        if result.ok:
            self.display_message("Game saved successfully!")
            return {'status': 'saved', 'message': 'Saved'}
        else:
            self.display_message(result.message, is_error=True)
            return {'status': 'error', 'message': result.message}
    
    def confirm_overwrite(self, confirmed: bool) -> dict:
        """
        Gestisce la conferma di sovrascrittura.
        
        Args:
            confirmed: True per confermare, False per annullare
        """
        if not confirmed:
            self.current_view = "slot_list"
            self.display_slot_list(mode="save")
            return {'status': 'cancelled', 'message': 'Overwrite cancelled'}
        
        # Esegui il salvataggio con conferma
        result = self.controller.save_game(self.selected_slot, confirmed=True)
        
        if result.ok:
            self.display_message("Game saved successfully!")
            return {'status': 'saved', 'message': 'Saved'}
        else:
            self.display_message(result.message, is_error=True)
            return {'status': 'error', 'message': result.message}
    
    # ============== LOAD FLOW ==============
    
    def open_load_menu(self):
        """Apre il menu di caricamento"""
        self.current_view = "slot_list"
        self.pending_action = "load"
        self.display_slot_list(mode="load")
        return True
    
    def select_load_slot(self, slot_index: int) -> dict:
        """
        Seleziona uno slot per il caricamento.
        
        Returns:
            dict con 'status': 'loaded', 'error', 'cancelled'
        """
        if slot_index == 0:
            return {'status': 'cancelled', 'message': 'Load cancelled'}
        
        if slot_index < 1 or slot_index > 3:
            return {'status': 'error', 'message': 'Invalid slot'}
        
        # Verifica se lo slot è vuoto
        if self.controller.is_slot_empty(slot_index):
            self.display_message("Empty slot", is_error=True)
            return {'status': 'error', 'message': 'Empty slot'}
        
        # Tenta il caricamento
        result = self.controller.load_game(slot_index)
        
        if result.ok:
            self.display_message("Game loaded successfully!")
            return {
                'status': 'loaded', 
                'message': 'Loaded',
                'room_id': result.save_data.data.world.room_id if result.save_data else None
            }
        else:
            self.display_message(result.message, is_error=True)
            return {'status': 'error', 'message': result.message}


class PauseMenu:
    """Menu di pausa con opzione di salvataggio"""
    
    def __init__(self, controller):
        self.controller = controller
        self.save_menu = SaveLoadMenu(controller)
    
    def display_pause_menu(self):
        """Mostra il menu di pausa"""
        print("\n=== PAUSE ===")
        print("1. Resume")
        print("2. Save Game")
        print("3. Options")
        print("4. Quit to Main Menu")
    
    def select_option(self, option: int) -> str:
        """
        Gestisce la selezione di un'opzione.
        
        Returns:
            'resume', 'save', 'options', 'quit'
        """
        options = {
            1: 'resume',
            2: 'save',
            3: 'options',
            4: 'quit'
        }
        return options.get(option, 'invalid')
    
    def open_save_from_pause(self):
        """Apre il menu di salvataggio dal menu di pausa"""
        return self.save_menu.open_save_menu()


class MainMenuWithLoad:
    """Main Menu esteso con opzione Load Game"""
    
    def __init__(self, controller):
        self.controller = controller
        self.save_menu = SaveLoadMenu(controller)
    
    def display_main_menu(self):
        """Mostra il menu principale"""
        print("\n=== MAIN MENU ===")
        print("1. New Game (Singleplayer)")
        print("2. New Game (Multiplayer)")
        print("3. Load Game")
        print("4. Options")
        print("5. Quit")
    
    def select_option(self, option: int) -> str:
        """
        Gestisce la selezione di un'opzione.
        
        Returns:
            'new_single', 'new_multi', 'load', 'options', 'quit'
        """
        options = {
            1: 'new_single',
            2: 'new_multi',
            3: 'load',
            4: 'options',
            5: 'quit'
        }
        return options.get(option, 'invalid')
    
    def open_load_game(self):
        """Apre il menu di caricamento"""
        return self.save_menu.open_load_menu()
    
    def singleplayer_new_game_button_press(self):
        """Inizia una nuova partita singleplayer"""
        self.controller.start_new_game(1)
    
    def multiplayer_new_game_button_press(self):
        """Inizia una nuova partita multiplayer"""
        self.controller.start_new_game(2)