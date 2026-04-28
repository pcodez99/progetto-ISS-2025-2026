import unittest
from src.model.vfx.vfx_manager import VFXManager


class TestVFXLifetime(unittest.TestCase):

    def test_vfx_entity_expires_and_is_removed_after_lifetime(self):
        vm = VFXManager()
        vm.spawn("vfx_slash", (5, 5), lifetime_ms=100, now_ms=1000)

        self.assertEqual(len(vm.entities), 1)

        vm.update(now_ms=1099)
        self.assertEqual(len(vm.entities), 1)

        vm.update(now_ms=1100)
        self.assertEqual(len(vm.entities), 0)
