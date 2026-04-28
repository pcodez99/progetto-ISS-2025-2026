"""
Tests per il Animation System
Epic 3: User Story 12 - Essential tests only
"""

import unittest
import pygame

pygame.init()

from src.model.animation import AnimationComponent, AnimationClip, AnimationController


class TestAnimationEssential(unittest.TestCase):
    """US12: Animation essential tests (REQUIRED)"""
    
    def setUp(self):
        self.anim = AnimationComponent()
        
        # Add required clips
        for name, loop in [("idle", True), ("walk", True), 
                           ("attack", False), ("hit", False), ("ko", False)]:
            clip = AnimationClip.create_placeholder(name, frame_count=4, 
                                                    frame_duration=0.1, loop=loop)
            self.anim.add_clip(name, clip)
        
        self.anim.set_state("idle", force=True)
    
    def test_animation_timing_uses_dt(self):
        """Animation progression uses dt (framerate independent)"""
        initial_time = self.anim.time_accumulator
        
        self.anim.update(0.05)
        
        self.assertAlmostEqual(self.anim.time_accumulator, initial_time + 0.05)
    
    def test_looping_idle_wraps(self):
        """Looping animation wraps correctly"""
        # Advance past total duration (0.4s)
        for _ in range(50):
            self.anim.update(0.01)
        
        self.assertLessEqual(self.anim.frame_index, 3)
    
    def test_attack_one_shot_plays_to_end_then_returns_to_idle(self):
        """Attack plays fully then returns to return_state"""
        self.anim.play_one_shot("attack", return_state="idle")
        
        self.assertEqual(self.anim.current_state, "attack")
        
        # Complete attack (0.4s total)
        for _ in range(50):
            self.anim.update(0.01)
        
        self.assertEqual(self.anim.current_state, "idle")
        self.assertFalse(self.anim.is_one_shot_active)
    
    def test_ko_locks_ignores_set_state_and_one_shots(self):
        """KO state locks and ignores further state changes"""
        self.anim.set_state("ko", force=True)
        
        self.assertTrue(self.anim.is_locked)
        
        # Try to change - should fail
        result = self.anim.set_state("idle")
        self.assertFalse(result)
        self.assertEqual(self.anim.current_state, "ko")
        
        result = self.anim.play_one_shot("attack")
        self.assertFalse(result)
        self.assertEqual(self.anim.current_state, "ko")
    
    def test_hit_during_attack_is_ignored_attack_continues(self):
        """HIT requests during ATTACK are ignored"""
        self.anim.play_one_shot("attack", return_state="idle")
        
        # Advance partially
        for _ in range(20):
            self.anim.update(0.01)
        
        # Try hit - should be ignored
        result = self.anim.play_one_shot("hit")
        
        self.assertFalse(result)
        self.assertEqual(self.anim.current_state, "attack")


class TestAnimationControllerEssential(unittest.TestCase):
    """US12: AnimationController essential test"""
    
    def test_velocity_logic_does_not_override_attack_one_shot(self):
        """Locomotion does not override one-shot"""
        anim = AnimationComponent()
        
        for name, loop in [("idle", True), ("walk", True), ("attack", False)]:
            clip = AnimationClip.create_placeholder(name, frame_count=4,
                                                    frame_duration=0.1, loop=loop)
            anim.add_clip(name, clip)
        
        anim.set_state("idle", force=True)
        controller = AnimationController(anim)
        
        # Start walking
        controller.update_locomotion(1.0, 0.0)
        self.assertEqual(anim.current_state, "walk")
        
        # Trigger attack
        controller.trigger_attack()
        self.assertEqual(anim.current_state, "attack")
        
        # Try locomotion during attack - should not override
        controller.update_locomotion(1.0, 0.0)
        self.assertEqual(anim.current_state, "attack")


if __name__ == "__main__":
    unittest.main()