class ContentRegistry:
    def __init__(self):
        self._data = {
            "rooms": {},
            "items": {},
            "dialogues": {},
            "npcs": {},
        }

    def register(self, kind: str, obj: dict):
        obj_id = obj.get("id")
        
        # Se stiamo registrando una stanza creata dal WorldBuilder, estraiamo l'oggetto reale
        real_obj = obj.get("obj", obj) 
        
        if not obj_id:
            raise ValueError(f"Missing stable id for kind={kind}")
        
        # Sovrascriviamo se esiste (utile per ricaricamenti o fix)
        self._data[kind][obj_id] = real_obj

    def get(self, kind: str, obj_id: str):
        return self._data.get(kind, {}).get(obj_id)