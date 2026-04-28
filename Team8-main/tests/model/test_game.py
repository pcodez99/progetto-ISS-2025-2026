import unittest
from src.model.game import Game
from src.model.character import Character


class TestEnterMainGameloop(unittest.TestCase):

    def setUp(self):
        self.game = Game()
    
    def tearDown(self):
        self.game = None
    
    def test_main_gameloop_is_entered_after_new_singleplayer_game_initialization(self):
        """
        US13: Verify singleplayer game initialization.
        Updated: start_new_game(1) now forces 2 players (RPG mode).
        """
        self.assertEqual(self.game.start_new_game(1), "entered mainloop")
        self.assertEqual(self.game.gamestate.current_level, 1)
        self.assertEqual(self.game.gamestate.current_room, 1)
        
        # Verify legacy alias
        self.assertIsInstance(self.game.gamestate.players[0], Character)
        
        # FIX: RPG Mode forces 2 active players
        self.assertEqual(self.game.gamestate.num_humans, 2)
        self.assertGreaterEqual(len(self.game.gamestate.players), 2)
        
        self.assertEqual(self.game.gamestate.is_running, True)
    
    def test_main_gameloop_is_entered_after_new_multiplayer_game_initialization(self):
        """
        US13: Verify multiplayer game initialization.
        """
        self.assertEqual(self.game.start_new_game(2), "entered mainloop")
        self.assertEqual(self.game.gamestate.current_level, 1)
        self.assertEqual(self.game.gamestate.current_room, 1)
        self.assertIsInstance(self.game.gamestate.players[0], Character)
        self.assertIsInstance(self.game.gamestate.players[1], Character)
        
        self.assertEqual(self.game.gamestate.num_humans, 2)
        self.assertGreaterEqual(len(self.game.gamestate.players), 2)
        
        self.assertEqual(self.game.gamestate.is_running, True)