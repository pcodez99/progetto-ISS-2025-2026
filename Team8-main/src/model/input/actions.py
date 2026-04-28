from enum import Enum


class Action(str, Enum):
    MENU_UP = "menu_up"
    MENU_DOWN = "menu_down"
    CONFIRM = "confirm"
    CANCEL = "cancel"
    INTERACT = "interact"
    PAUSE = "pause"
