class CommandRegistry:
    def __init__(self):
        self._cmds = {}

    def register(self, name: str, fn):
        self._cmds[name] = fn

    def run(self, name: str, args: list[str], game):
        if name not in self._cmds:
            return f"Unknown command: {name}"
        return self._cmds[name](args, game)

def default_registry():
    reg = CommandRegistry()

    def teleport(args, game):
        target = args[0] if args else "hub_01"
        game.debug_last_teleport = target
        return f"Teleported to {target}"

    def give_item(args, game):
        item_id = args[0] if args else None
        qty = int(args[1]) if len(args) > 1 else 1
        game.debug_last_given = (item_id, qty)
        return f"Gave {qty}x {item_id}"

    def set_flag(args, game):
        k = args[0] if args else None
        v = args[1] if len(args) > 1 else "true"
        game.debug_flags = getattr(game, "debug_flags", {})
        game.debug_flags[k] = v
        return f"Set flag {k}={v}"

    def win_combat(args, game):
        game.debug_win_combat = True
        return "Combat won"

    reg.register("teleport", teleport)
    reg.register("give_item", give_item)
    reg.register("set_flag", set_flag)
    reg.register("win_combat", win_combat)
    return reg
