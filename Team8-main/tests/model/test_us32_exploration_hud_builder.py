import unittest
from dataclasses import dataclass
from src.model.game import Game
from src.model.ui.exploration_hud import ExplorationHUDBuilder

@dataclass
class RoomStub:
    name: str  
    room_id: str = "stub_id"

class TestUS32ExplorationHUDBuilder(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(2)

    def test_exploration_hud_builder_includes_active_owner_name_and_zone_label(self):
        room = RoomStub(name="Hub")
        turn_state = self.game.gamestate.exploration_turn_manager
        
        data = ExplorationHUDBuilder.from_models(
            game_model=self.game,
            room_model=room,
            turn_state=turn_state,
            input_hints={"interact": "E"},
        )
        self.assertEqual(data.active_owner, "P1")
        self.assertEqual(data.active_name, "Turiddu")
        self.assertEqual(data.zone_label, "Hub")

    def test_exploration_hud_builder_includes_all_enabled_party_hp_even_in_stacked_mode(self):
        room = RoomStub(name="Aurion")
        turn_state = self.game.gamestate.exploration_turn_manager
        
        data = ExplorationHUDBuilder.from_models(
            self.game, room,
            turn_state=turn_state,
            input_hints={"interact": "E"},
        )
        # RPG Mode: 2 Players
        self.assertEqual(len(data.party_hp_list), 2)
        enabled_count = sum(1 for p in data.party_hp_list if p['enabled'])
        self.assertEqual(enabled_count, 2)

    def test_room_change_updates_zone_label_same_frame(self):
        room1 = RoomStub(name="Hub")
        room2 = RoomStub(name="Vinalia")
        turn_state = self.game.gamestate.exploration_turn_manager
        
        d1 = ExplorationHUDBuilder.from_models(self.game, room1, turn_state, {"interact": "E"})
        d2 = ExplorationHUDBuilder.from_models(self.game, room2, turn_state, {"interact": "E"})
        self.assertEqual(d1.zone_label, "Hub")
        self.assertEqual(d2.zone_label, "Vinalia")