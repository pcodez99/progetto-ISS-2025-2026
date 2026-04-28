"""
Consolidated Unit Tests for Epic 18 (Combat System Advanced)
Covers US 71, 72, 73, 74, 75, 76
Updated: Removed invalid View patching.
"""
import unittest
from unittest.mock import Mock, patch
import pygame

from src.model.combat.damage_calculator import DamageCalculator
from src.model.utils.rng import RNG
from src.model.character import Character
from src.model.combat.enemy import Enemy
from src.model.status.status_effects import StatusInstance
from src.model.states.game_states import CombatState, HubState
from src.model.combat.combat_types import Encounter
from src.controller.state_machine import StateMachine, StateID
from src.controller.game_controller import GameController
from src.model.input_actions import Action

class TestDamageFormula_US72(unittest.TestCase):
    def setUp(self):
        self.rng = RNG(seed=12345)
        self.calc = DamageCalculator(self.rng)
        
        # Setup Attacker (Hero)
        self.hero = Character()
        self.hero.atk = 20
        self.hero.magic = 20
        self.hero.crit_rate = 0
        
        # Setup Defender (Monster)
        self.monster = Enemy("Monster", 100, 100, 10, 10, magic=0, mdef=0)

    def test_physical_damage(self):
        """US 72: Physical damage formula."""
        move = {"power": 20, "type": "physical", "accuracy": 100}
        res = self.calc.compute(self.hero, self.monster, move)
        self.assertFalse(res.is_miss)
        self.assertGreater(res.damage, 0)

    def test_magic_damage_ignores_phys_def(self):
        """US 72: Magic uses Matk vs Mdef."""
        move = {"power": 20, "type": "magical", "accuracy": 100}
        res = self.calc.compute(self.hero, self.monster, move)
        self.assertGreater(res.damage, 0)

    def test_miss_mechanic(self):
        """US 72: 0 Accuracy always misses."""
        move = {"power": 100, "type": "physical", "accuracy": 0}
        res = self.calc.compute(self.hero, self.monster, move)
        self.assertTrue(res.is_miss)
        self.assertEqual(res.damage, 0)


class TestStatusEffects_US73(unittest.TestCase):
    def setUp(self):
        self.hero = Character()
        self.hero.atk = 10
    
    def test_buff_increases_stats(self):
        """US 73: Buff increases stat."""
        rage = StatusInstance("Rage", "buff_atk", duration=3, stat_modifiers={"atk": 2.0})
        self.hero.add_status(rage)
        self.assertEqual(self.hero.get_stat("atk"), 20)

    def test_stun_flag(self):
        """US 73: Stun flag is detected."""
        stun = StatusInstance("Stun", "stun", duration=1)
        self.hero.add_status(stun)
        self.assertTrue(self.hero.is_stunned)


class TestCombatFlow_US74_75_76(unittest.TestCase):
    def setUp(self):
        # Mocking the full stack
        self.controller = Mock(spec=GameController)
        self.controller.game = Mock()
        self.controller.input_manager = Mock()
        self.controller.render_controller = Mock()
        
        # Setup Gamestate
        self.controller.game.gamestate = Mock()
        self.hero = Character()
        self.hero.name = "Hero"
        self.hero.hp = 100; self.hero.max_hp = 100
        self.hero.atk = 10; self.hero.defense = 5; self.hero.spd = 10
        self.controller.game.gamestate.party.get_enabled_characters.return_value = [self.hero]

        # Configure content mock
        mock_room_data = Mock()
        mock_room_data.get_spawn_position.return_value = (100, 200)
        self.controller.game.content.get.return_value = mock_room_data
        
        # Setup State Machine
        self.sm = StateMachine()
        self.sm.controller = self.controller
        
        # Register States
        self.state = CombatState(self.sm)
        self.hub_state = HubState(self.sm)
        
        self.sm.register_state(self.state)
        self.sm.register_state(self.hub_state)

    def test_combat_initialization_US71(self):
        """US 71: Combat initializes with enemies."""
        self.state.enter(encounter_id="test_enc", seed=123)
        self.assertIsNotNone(self.state.battle_ctx)
        self.assertGreaterEqual(len(self.state.battle_ctx.enemies), 1)
        self.assertEqual(self.state.phase, CombatState.PHASE_START_TURN)

    def test_player_attack_logic_US74(self):
        """US 74: Player can select attack and damage enemy."""
        self.state.enter(encounter_id="test_enc", seed=123)
        
        # Force active player
        self.state.turn_manager._turn_queue = [self.hero] 
        self.state.turn_manager._active_actor = None
        
        # 1. Update to Start Turn -> Input
        self.state.update(0.1)
        self.assertEqual(self.state.phase, CombatState.PHASE_INPUT)
        
        # 2. Select Attack
        self.controller.input_manager.was_just_pressed.side_effect = lambda k: k == Action.CONFIRM
        
        self.state.menu_state.cursor_index = 0 # Attack
        self.state.handle_event(None) # Confirm Attack
        self.assertEqual(self.state.phase, CombatState.PHASE_TARGETING)
        
        # 3. Confirm Target
        enemy = self.state.battle_ctx.enemies[0]
        initial_hp = enemy.current_hp
        self.state.handle_event(None) # Confirm Target
        
        # 4. Action Executed
        self.assertEqual(self.state.phase, CombatState.PHASE_EXECUTE_ACTION)
        self.assertLess(enemy.current_hp, initial_hp)

    def test_enemy_ai_response_US76(self):
        """US 76: Enemy AI executes action."""
        self.state.enter(encounter_id="test_enc", seed=123)
        enemy = self.state.battle_ctx.enemies[0]
        enemy.spd = 999 # Make enemy go first
        
        # Force enemy turn
        self.state.turn_manager._turn_queue = [enemy, self.hero]
        self.state.turn_manager._active_actor = None
        
        initial_hero_hp = self.hero.current_hp
        
        # Update loop should trigger AI
        self.state.update(0.1) # Start Turn
        
        # Verify Damage
        self.assertLess(self.hero.current_hp, initial_hero_hp)
        self.assertEqual(self.state.phase, CombatState.PHASE_EXECUTE_ACTION)

    def test_victory_condition_US75(self):
        """US 75: Killing all enemies triggers victory."""
        self.state.enter(encounter_id="test_enc", seed=123)
        
        # Kill all enemies manually
        for e in self.state.battle_ctx.enemies:
            e.current_hp = 0
            
        self.state.phase = CombatState.PHASE_CHECK_OUTCOME
        self.state.update(0.1)
        
        # Should call _resolve_victory and change state to HUB
        self.assertEqual(self.sm.peek().state_id, StateID.HUB)

if __name__ == "__main__":
    unittest.main()