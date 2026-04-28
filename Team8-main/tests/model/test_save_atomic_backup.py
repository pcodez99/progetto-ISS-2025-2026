import os
import json
from src.model.save.save_manager import SaveManager
from src.model.save.constants import get_slot_filename, CURRENT_SAVE_SCHEMA_VERSION

def _create_valid_data(playtime):
    """Helper per creare un dizionario save valido che passi il validatore."""
    return {
        "schema_version": CURRENT_SAVE_SCHEMA_VERSION,
        "meta": {
            "room_id": "hub", 
            "timestamp_iso": "2023-01-01", 
            "playtime_seconds": playtime,  # USIAMO QUESTO COME INDICATORE
            "aces_count": 0, 
            "aces_collected": []
        },
        "data": {
            "world": {"room_id": "hub"},
            "party": {"characters": [], "inventories": []},
            "progression": {},
            "world_state": {},
            "turn_state": {},
            "checkpoint": {}
        }
    }

def test_save_atomic_creates_primary_and_backup(tmp_path):
    sm = SaveManager(save_dir=str(tmp_path))
    
    # Usa l'alias save_atomic per scrivere dati grezzi (ma validi per lo schema)
    sm.save_atomic("slot1", _create_valid_data(100))
    sm.save_atomic("slot1", _create_valid_data(200))

    expected_json = tmp_path / get_slot_filename(1)
    expected_bak = str(expected_json) + ".bak"

    assert os.path.exists(expected_json)
    assert os.path.exists(expected_bak)

def test_load_with_backup_on_corruption(tmp_path):
    sm = SaveManager(save_dir=str(tmp_path))
    
    # 1. Salva stato A (Playtime 100) -> Finirà nel backup
    sm.save_atomic("slot1", _create_valid_data(100))
    
    # 2. Salva stato B (Playtime 200) -> Finirà nel file principale
    sm.save_atomic("slot1", _create_valid_data(200))

    expected_json = tmp_path / get_slot_filename(1)

    # 3. Corrompi il file principale (quello con 200)
    with open(expected_json, "w", encoding="utf-8") as f:
        f.write("{not json garbage")

    # 4. Carica (dovrebbe fallbackare al backup che ha 100)
    data, used_backup = sm.load_with_backup("slot1")
    
    assert data is not None
    assert used_backup is True
    
    # 5. Verifica: i dati caricati devono corrispondere al backup (100)
    # L'oggetto ritornato è un dict completo (da SaveFileDTO.to_dict)
    assert data["meta"]["playtime_seconds"] == 100