from src.model.content.loader_base import LoaderBase
from src.model.content.validators import validate_room

class RoomsLoader(LoaderBase):
    def __init__(self):
        super().__init__(validate_room)
