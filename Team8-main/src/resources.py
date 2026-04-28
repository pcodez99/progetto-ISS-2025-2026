import os
import sys


def get_resource_path(rel_path: str) -> str:
    """
    PyInstaller: sys._MEIPASS è la cartella temporanea dove vengono estratti i file.
    Dev: usa la root del progetto.
    """
    base = getattr(sys, "_MEIPASS", os.path.abspath("."))
    return os.path.join(base, rel_path)
