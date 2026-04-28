import os
from src.app import run_game


def test_crash_writes_crashlog(tmp_path, monkeypatch):
    monkeypatch.chdir(tmp_path)

    def bad():
        raise RuntimeError("boom")

    run_game(bad)

    assert os.path.exists("logs/crash.log")
