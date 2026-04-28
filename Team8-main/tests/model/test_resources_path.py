import sys
from src.resources import get_resource_path


def test_get_resource_path_dev_mode():
    p = get_resource_path("data/rooms")
    assert "data" in p


def test_get_resource_path_meipass(monkeypatch):
    monkeypatch.setattr(sys, "_MEIPASS", "/tmp/bundle", raising=False)
    p = get_resource_path("data/rooms")
    assert p.startswith("/tmp/bundle")
