from src.model.debug.commands import default_registry

class DebugConsole:
    def __init__(self, enabled: bool = False):
        self.enabled = enabled
        self.registry = default_registry()

    def execute(self, text: str, game):
        if not self.enabled:
            return "Debug disabled"
        parts = text.strip().split()
        if not parts:
            return ""
        name, args = parts[0], parts[1:]
        return self.registry.run(name, args, game)
