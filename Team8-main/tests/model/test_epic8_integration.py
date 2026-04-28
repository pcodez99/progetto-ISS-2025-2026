import unittest
from src.model.game import Game
from src.model.content.hub_builder import HubBuilder

class TestEpic8Integration(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)
        self.hub = HubBuilder.create_hub()
        
    def test_party_guest_bonus_logic(self):
        """Test US 30: Guest bonus logic inside GameState."""
        self.assertIsNone(self.game.gamestate.get_guest_bonus())
        
        self.game.gamestate.set_guest("pupo_siciliano")
        self.assertEqual(self.game.gamestate.guest_id, "pupo_siciliano")
        self.assertEqual(self.game.gamestate.get_guest_bonus(), "DEFENSE_UP")

    def test_party_stacked_movement(self):
        """Test US 31: Single world position and Active Index."""
        self.game.gamestate.party_position = [100, 100]
        
        # Muovi
        self.game.gamestate.party_position[0] += 10
        self.assertEqual(self.game.gamestate.party_position, [110, 100])
        
        # End Turn (cicla leader)
        initial_idx = self.game.gamestate.exploration_active_index
        self.game.gamestate.cycle_active_player()
        new_idx = self.game.gamestate.exploration_active_index
        
        # Con 2 giocatori (default RPG mode), l'indice cambia
        self.assertNotEqual(initial_idx, new_idx)
        self.assertEqual(new_idx, 1)

    def test_hub_exits_configuration(self):
        """
        Test Navigation: Hub has regional exits.
        Updated: Etna is now a special trigger (to_etna), not a standard gate.
        """
        triggers = self.hub.triggers
        
        # Filtra i trigger cancelli
        exits = [t for t in triggers if t.trigger_type == 'script' and 'gate_' in t.id]
        self.assertEqual(len(exits), 4) # 4 Regioni
        
        targets = [t.id for t in exits]
        self.assertIn("gate_aurion", targets)
        self.assertIn("gate_ferrum", targets)
        self.assertIn("gate_vinalia", targets)
        self.assertIn("gate_viridor", targets)
        
        # Etna check separately
        etna = next((t for t in triggers if t.id == "to_etna"), None)
        self.assertIsNotNone(etna)

    def test_npc_giufa_properties(self):
        """
        Test NPC: Giufà exists.
        Updated: Giufà uses new EntityDefinition structure.
        """
        giufa = next((e for e in self.hub.entities if e.entity_id == "npc_giufa"), None)
        self.assertIsNotNone(giufa)
        # Check specific field if dict properties not available directly
        self.assertEqual(giufa.interaction_label, "Parla con Giufà")
        self.assertEqual(giufa.script_id, "giufa_hub_talk")