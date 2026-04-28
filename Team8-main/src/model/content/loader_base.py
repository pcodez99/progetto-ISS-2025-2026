import json
import os

class LoaderBase:
    def __init__(self, validator_fn):
        self.validator_fn = validator_fn

    def load_all(self, dir_path: str) -> list[dict]:
        out = []
        if not os.path.isdir(dir_path):
            return out
        for name in os.listdir(dir_path):
            if not name.endswith(".json"):
                continue
            path = os.path.join(dir_path, name)
            with open(path, "r", encoding="utf-8") as f:
                obj = json.load(f)
            self.validator_fn(obj)
            out.append(obj)
        return out
