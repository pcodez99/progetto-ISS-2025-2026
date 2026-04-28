"""
Migration System - Save version compatibility
Epic 5: User Story 19
"""

from typing import Dict, Callable

from src.model.save import CURRENT_SAVE_SCHEMA_VERSION

# Registry of migrations: {from_version: migration_function}
MIGRATIONS: Dict[int, Callable[[dict], dict]] = {}


def register_migration(from_version: int):
    """Decorator to register a migration function."""
    def decorator(func: Callable[[dict], dict]):
        MIGRATIONS[from_version] = func
        return func
    return decorator


# ============== MIGRATION FUNCTIONS ==============

@register_migration(0)
def migrate_v0_to_v1(data: dict) -> dict:
    """
    Migrate from version 0 (legacy/no version) to version 1.
    Adds missing fields with defaults.
    """
    # Ensure base structure exists
    if 'meta' not in data:
        data['meta'] = {}
    if 'data' not in data:
        data['data'] = {}
    
    # Add missing meta fields
    meta_defaults = {
        'timestamp_iso': '',
        'room_id': 'hub',
        'playtime_seconds': 0,
        'aces_count': 0,
        'aces_collected': []
    }
    for key, default in meta_defaults.items():
        if key not in data['meta']:
            data['meta'][key] = default
    
    # Add missing data structures
    data_defaults = {
        'world': {'room_id': 'hub', 'spawn_id': None, 'party_world_pos': [0, 0]},
        'party': {
            'characters': [], 'num_humans': 1, 'enabled_mask': [],
            'guest_id': None, 'inventories': []
        },
        'progression': {'aces': [], 'flags': {}},
        'world_state': {'removed_entities': []},
        'turn_state': {'exploration_active_index': 0, 'awaiting_handoff_confirm': False},
        'checkpoint': {'checkpoint_room_id': None, 'checkpoint_spawn_id': None}
    }
    
    for key, default in data_defaults.items():
        if key not in data['data']:
            data['data'][key] = default
        else:
            # Merge missing subfields
            for subkey, subdefault in default.items():
                if subkey not in data['data'][key]:
                    data['data'][key][subkey] = subdefault
    
    data['schema_version'] = 1
    return data


# ============== MIGRATION PIPELINE ==============

def migrate_to_current(data: dict) -> dict:
    """
    Apply all necessary migrations to bring save to current version.
    
    Args:
        data: Save dictionary to migrate.
        
    Returns:
        Migrated dictionary at current version.
    """
    current_version = data.get('schema_version', 0)
    
    while current_version < CURRENT_SAVE_SCHEMA_VERSION:
        if current_version in MIGRATIONS:
            data = MIGRATIONS[current_version](data)
            current_version = data.get('schema_version', current_version + 1)
        else:
            # No migration defined, just bump version
            current_version += 1
            data['schema_version'] = current_version
    
    return data


def get_migration_path(from_version: int, to_version: int = None) -> list:
    """
    Get list of versions in migration path.
    Useful for debugging.
    """
    if to_version is None:
        to_version = CURRENT_SAVE_SCHEMA_VERSION
    return list(range(from_version, to_version + 1))


def is_version_supported(version: int) -> bool:
    """Check if version can be migrated."""
    return version <= CURRENT_SAVE_SCHEMA_VERSION


def is_future_version(version: int) -> bool:
    """Check if version is from the future (cannot load)."""
    return version > CURRENT_SAVE_SCHEMA_VERSION