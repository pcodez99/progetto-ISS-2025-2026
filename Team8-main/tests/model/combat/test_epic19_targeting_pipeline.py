"""
Unit tests for Epic 19 (Targeting System & Action Pipeline).
Covers US 77, 78, 79.
"""
import unittest
from unittest.mock import Mock, MagicMock
from src.model.combat.targeting_system import TargetingSystem
from src.model.combat.action_pipeline import ActionPipeline
from src.model.combat.damage_calculator import DamageCalculator
from src.model.utils.rng import RNG
from src.model.status.status_effects import StatusInstance

class TestTargetingSystem(unittest.TestCase):
    """Tests for US 78 (Targeting Modes)."""
    
    def setUp(self):
        # Configure Mocks with integer HP to avoid TypeError in comparisons
        self.hero = Mock(spec=object)
        self.hero.hp = 100
        self.hero.current_hp = 100

        self.enemy1 = Mock(spec=object)
        self.enemy1.hp = 10
        self.enemy1.current_hp = 10
        
        self.enemy2 = Mock(spec=object)
        self.enemy2.hp = 10
        self.enemy2.current_hp = 10
        
        self.dead_enemy = Mock(spec=object)
        self.dead_enemy.hp = 0
        self.dead_enemy.current_hp = 0
        
        self.battle_ctx = Mock()
        self.battle_ctx.get_living_enemies.return_value = [self.enemy1, self.enemy2]
        self.battle_ctx.get_living_party.return_value = [self.hero]

    def test_get_candidates_single_enemy_returns_living_enemies(self):
        candidates = TargetingSystem.get_candidates(TargetingSystem.SCOPE_SINGLE_ENEMY, self.hero, self.battle_ctx)
        self.assertEqual(len(candidates), 2)
        self.assertIn(self.enemy1, candidates)
        self.assertNotIn(self.dead_enemy, candidates)

    def test_get_candidates_self_returns_user(self):
        candidates = TargetingSystem.get_candidates(TargetingSystem.SCOPE_SELF, self.hero, self.battle_ctx)
        self.assertEqual(candidates, [self.hero])

    def test_resolve_final_targets_all_enemies(self):
        # Even if cursor is on enemy1, ALL should be targeted
        candidates = [self.enemy1, self.enemy2]
        rng = Mock()
        
        targets = TargetingSystem.resolve_final_targets(
            TargetingSystem.SCOPE_ALL_ENEMIES, 
            self.enemy1, 
            candidates, 
            rng
        )
        self.assertEqual(len(targets), 2)
        self.assertIn(self.enemy1, targets)
        self.assertIn(self.enemy2, targets)

    def test_resolve_final_targets_random(self):
        candidates = [self.enemy1, self.enemy2]
        rng = Mock()
        rng.choice.return_value = self.enemy2 # Mock RNG choice
        
        targets = TargetingSystem.resolve_final_targets(
            TargetingSystem.SCOPE_RANDOM_ENEMY,
            None,
            candidates,
            rng
        )
        self.assertEqual(targets, [self.enemy2])


class TestActionPipeline(unittest.TestCase):
    """Tests for US 79 (Action Pipeline)."""

    def setUp(self):
        self.rng = Mock(spec=RNG)
        self.calc = Mock(spec=DamageCalculator)
        self.pipeline = ActionPipeline(self.calc, self.rng)
        
        self.hero = Mock()
        self.hero.name = "Hero"
        # FIX: Ensure int value for hp checks
        self.hero.hp = 50
        self.hero.current_hp = 50

        self.enemy = Mock()
        self.enemy.name = "Goblin"
        # FIX: Ensure int value for hp checks
        self.enemy.hp = 20
        self.enemy.current_hp = 20
        self.enemy.add_status = Mock()

    def test_pipeline_applies_damage_on_hit(self):
        # Setup Hit
        hit_result = MagicMock()
        hit_result.is_miss = False
        hit_result.damage = 5
        self.calc.compute.return_value = hit_result
        
        move_data = {"name": "Punch"}
        
        logs = self.pipeline.execute(self.hero, [self.enemy], move_data)
        
        self.assertEqual(self.enemy.current_hp, 15) # 20 - 5
        self.assertTrue(any("hits Goblin for 5" in l for l in logs))

    def test_pipeline_skips_damage_on_miss(self):
        # Setup Miss
        miss_result = MagicMock()
        miss_result.is_miss = True
        miss_result.damage = 0
        self.calc.compute.return_value = miss_result
        
        move_data = {"name": "Punch"}
        
        logs = self.pipeline.execute(self.hero, [self.enemy], move_data)
        
        self.assertEqual(self.enemy.current_hp, 20) # No damage
        self.assertTrue(any("Miss!" in l for l in logs))

    def test_pipeline_applies_status_on_hit(self):
        # Setup Hit
        hit_result = MagicMock()
        hit_result.is_miss = False
        hit_result.damage = 0
        self.calc.compute.return_value = hit_result
        
        status = StatusInstance("Poison", "poison", 3)
        move_data = {"name": "Poison Stab", "status_apply": status}
        
        logs = self.pipeline.execute(self.hero, [self.enemy], move_data)
        
        self.enemy.add_status.assert_called()
        self.assertTrue(any("affected by Poison" in l for l in logs))

if __name__ == "__main__":
    unittest.main()