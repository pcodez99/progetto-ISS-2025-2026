class ValidationError(Exception):
    pass

def _require_keys(obj: dict, keys: list[str], ctx: str):
    for k in keys:
        if k not in obj:
            raise ValidationError(f"Missing key '{k}' in {ctx}: {obj}")

def validate_room(obj: dict):
    _require_keys(obj, ["id", "display_name", "exits"], "room")
    if not isinstance(obj["exits"], dict):
        raise ValidationError("room.exits must be a dict")

def validate_item(obj: dict):
    _require_keys(obj, ["id", "display_name", "description"], "item")
