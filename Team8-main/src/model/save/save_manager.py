"""
Save Manager - File I/O for Save/Load (Merged: Amelia Structure + Sicily Atomic Writes)
"""
import os
import json
import shutil
from typing import List, Optional

from src.model.save.constants import (
    SAVE_DIR, MAX_SLOTS, CURRENT_SAVE_SCHEMA_VERSION,
    get_slot_filename, SlotStatus
)
from src.model.save.dtos import (
    SlotInfo, SaveMeta, SaveFileDTO,
    SaveResult, LoadResult
)
from src.model.save.validator import SaveValidator
from src.model.save.serializer import GameSerializer

class SaveManager:
    def __init__(self, save_dir: str = SAVE_DIR, max_slots: int = MAX_SLOTS):
        self.save_dir = save_dir
        self.max_slots = max_slots
        self._error_log: List[str] = []
    
    def _get_slot_path(self, slot_index: int) -> str:
        return os.path.join(self.save_dir, get_slot_filename(slot_index))
    
    def _log_error(self, message: str, exception: Optional[Exception] = None):
        log_entry = f"[SaveManager] {message}"
        if exception:
            log_entry += f" | {type(exception).__name__}: {exception}"
        self._error_log.append(log_entry)
        print(log_entry)

    def list_slots(self) -> List[SlotInfo]:
        slots = []
        for i in range(1, self.max_slots + 1):
            filepath = self._get_slot_path(i)
            if not os.path.exists(filepath):
                slots.append(SlotInfo(slot_index=i, status=SlotStatus.EMPTY))
                continue
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                validation = SaveValidator.validate_save_dict(data)
                if validation.ok:
                    meta = SaveMeta.from_dict(data.get('meta', {}))
                    slots.append(SlotInfo(slot_index=i, status=SlotStatus.OK, meta=meta))
                else:
                    slots.append(SlotInfo(slot_index=i, status=SlotStatus.CORRUPT))
            except Exception as e:
                self._log_error(f"Error reading slot {i}", e)
                slots.append(SlotInfo(slot_index=i, status=SlotStatus.CORRUPT))
        return slots

    def is_slot_occupied(self, slot_index: int) -> bool:
        if not 1 <= slot_index <= self.max_slots: return False
        return os.path.exists(self._get_slot_path(slot_index))

    def save_to_slot(self, slot_index: int, game_model, force_overwrite: bool = False, custom_name: str = "") -> SaveResult:
        """
        Salva il gioco usando scrittura atomica e backup (Sicily Logic).
        Accepts custom_name to name the save.
        """
        if not 1 <= slot_index <= self.max_slots:
            return SaveResult(ok=False, message=f"Invalid slot index: {slot_index}")
        
        final_path = self._get_slot_path(slot_index)
        temp_path = final_path + ".tmp"
        bak_path = final_path + ".bak"

        try:
            os.makedirs(self.save_dir, exist_ok=True)
            
            # 1. Creazione Backup se esiste un save precedente (Sicily Feature)
            if os.path.exists(final_path):
                try:
                    shutil.copy2(final_path, bak_path)
                except Exception as e:
                    self._log_error(f"Backup creation failed for slot {slot_index}", e)

            # 2. Serializzazione (Amelia Feature) - PASSING NAME
            save_dict = GameSerializer.to_dict(game_model, custom_name=custom_name)
            
            # 3. Scrittura Atomica (Sicily Feature)
            with open(temp_path, 'w', encoding='utf-8') as f:
                json.dump(save_dict, f, indent=2, ensure_ascii=False)
            
            # Sostituzione finale
            os.replace(temp_path, final_path)
            
            return SaveResult(ok=True, message="Game saved successfully")
        
        except Exception as e:
            self._log_error(f"Critical save error slot {slot_index}", e)
            # Pulizia file temporaneo
            if os.path.exists(temp_path):
                try: os.remove(temp_path)
                except: pass
            return SaveResult(ok=False, message=f"Save failed: {e}", error=e)

    def load_from_slot(self, slot_index: int) -> LoadResult:
        """Carica il gioco, con fallback al backup se il principale è corrotto (Sicily Logic)."""
        if not 1 <= slot_index <= self.max_slots:
            return LoadResult(ok=False, message=f"Invalid slot index: {slot_index}")
        
        filepath = self._get_slot_path(slot_index)
        bak_path = filepath + ".bak"
        
        # Tentativo 1: Carica file principale
        load_res = self._try_load_file(filepath, slot_index)
        if load_res.ok:
            return load_res
            
        # Tentativo 2: Carica backup (Sicily Feature)
        if os.path.exists(bak_path):
            self._log_error(f"Slot {slot_index} corrupted, attempting backup restore...")
            backup_res = self._try_load_file(bak_path, slot_index)
            if backup_res.ok:
                backup_res.message = "Game loaded from backup (primary was corrupt)"
                return backup_res
                
        return load_res # Ritorna l'errore originale se fallisce anche il backup

    def _try_load_file(self, path: str, slot_index: int) -> LoadResult:
        if not os.path.exists(path):
            return LoadResult(ok=False, message="Empty slot")
        
        try:
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            # Version Check & Migration
            file_version = data.get('schema_version', 0)
            if file_version > CURRENT_SAVE_SCHEMA_VERSION:
                return LoadResult(ok=False, message="Save from a newer version - cannot load")
            if file_version < CURRENT_SAVE_SCHEMA_VERSION:
                from src.model.migration import migrate_to_current
                data = migrate_to_current(data)
            
            # Validation
            validation = SaveValidator.validate_save_dict(data)
            if not validation.ok:
                return LoadResult(ok=False, message="Save file invalid", error=Exception(str(validation.errors)))
            
            save_data = SaveFileDTO.from_dict(data)
            return LoadResult(ok=True, message="Loaded", save_data=save_data)
            
        except Exception as e:
            return LoadResult(ok=False, message="File corrupted", error=e)

    def delete_slot(self, slot_index: int) -> SaveResult:
        filepath = self._get_slot_path(slot_index)
        bak_path = filepath + ".bak"
        try:
            if os.path.exists(filepath): os.remove(filepath)
            if os.path.exists(bak_path): os.remove(bak_path)
            return SaveResult(ok=True, message="Deleted")
        except Exception as e:
            return SaveResult(ok=False, message=str(e))
        

    def save_atomic(self, slot: str, data: dict) -> None:
        """Alias per compatibilità con test Sicily."""
        try:
            slot_idx = int(slot.replace("slot", ""))
        except ValueError:
            slot_idx = 1
        
        p, t, b = self._get_slot_path(slot_idx), self._get_slot_path(slot_idx) + ".tmp", self._get_slot_path(slot_idx) + ".bak"
        with open(t, "w") as f: json.dump(data, f)
        import os
        if os.path.exists(p): 
            import shutil
            shutil.copy2(p, b)
        os.replace(t, p)

    def load_with_backup(self, slot: str):
        """Alias per compatibilità con test Sicily."""
        try:
            slot_idx = int(slot.replace("slot", ""))
        except ValueError:
            slot_idx = 1
        
        res = self.load_from_slot(slot_idx)
        if res.ok and res.save_data:
            return res.save_data.to_dict(), "backup" in res.message
        return None, False