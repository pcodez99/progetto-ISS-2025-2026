"""
Main Menu View - User interface for the main menu
Consolidated.
"""

from typing import Optional

class MainMenu:
    def __init__(self, controller):
        self.controller = controller

    def display(self):
        pass
    
    def singleplayer_new_game_button_press(self):
        self.controller.start_new_game(2)

    def multiplayer_new_game_button_press(self):
        self.controller.start_new_game(2)


class MainMenuWithLoad:
    def __init__(self, controller):
        self.controller = controller
        self._save_load_menu = None 
    
    @property
    def save_menu(self):
        if self._save_load_menu is None:
            from src.view.save_menu import SaveLoadMenu
            self._save_load_menu = SaveLoadMenu(self.controller)
        return self._save_load_menu
    
    def select_option(self, option: int) -> str:
        options = {
            1: 'new_single',
            2: 'new_multi',
            3: 'load',
            4: 'options',
            5: 'quit'
        }
        return options.get(option, 'invalid')
    
    def open_load_game(self) -> bool:
        return self.save_menu.open_load_menu()
    
    def start_adventure(self):
        self.controller.start_new_game(2)
        
    def has_save_games(self) -> bool:
        slots = self.controller.get_save_slots()
        from src.model.save import SlotStatus
        return any(slot.status == SlotStatus.OK for slot in slots)

    # Added methods for tests
    def singleplayer_new_game_button_press(self):
        self.controller.start_new_game(2)

    def multiplayer_new_game_button_press(self):
        self.controller.start_new_game(2)