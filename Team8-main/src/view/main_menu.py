"""
Main Menu View - User interface for the main menu
Legacy text-based menu (Updated for Fixed 2-Player RPG)
"""

from typing import Optional


class MainMenu:
    """
    Basic main menu for starting new games.
    """
    
    def __init__(self, controller):
        self.controller = controller

    def display(self):
        print("\n=== MAIN MENU ===")
        print("1. New Game (Start Adventure)")
        print("2. Quit")
    
    def singleplayer_new_game_button_press(self):
        """Starts standard adventure (2 characters)."""
        self.controller.start_new_game(2)


class MainMenuWithLoad:
    """
    Extended main menu with Load Game functionality.
    """
    
    def __init__(self, controller):
        self.controller = controller
        self._save_load_menu = None 
    
    @property
    def save_menu(self):
        if self._save_load_menu is None:
            from src.view.save_menu import SaveLoadMenu
            self._save_load_menu = SaveLoadMenu(self.controller)
        return self._save_load_menu
    
    def display_main_menu(self):
        print("\n" + "=" * 40)
        print("            MAIN MENU")
        print("=" * 40)
        print("1. New Game (Start Adventure)")
        print("2. Load Game")
        print("3. Quit")
        print("=" * 40)
    
    def select_option(self, option: int) -> str:
        options = {
            1: 'new_game',
            2: 'load',
            3: 'quit'
        }
        return options.get(option, 'invalid')
    
    def handle_selection(self, option: int) -> Optional[str]:
        action = self.select_option(option)
        
        if action == 'new_game':
            self.start_adventure()
            return "Starting adventure..."
        elif action == 'load':
            self.open_load_game()
            return None
        elif action == 'quit':
            return "QUIT"
        else:
            return "Invalid option"
    
    def open_load_game(self) -> bool:
        return self.save_menu.open_load_menu()
    
    def start_adventure(self):
        """Starts standard 2-player adventure."""
        self.controller.start_new_game(2)
    
    def has_save_games(self) -> bool:
        slots = self.controller.get_save_slots()
        from src.model.save import SlotStatus
        return any(slot.status == SlotStatus.OK for slot in slots)