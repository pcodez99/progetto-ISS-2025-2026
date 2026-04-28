"""
Game Serialization for Save/Load
Epic 5: User Stories 16, 17, 18, 19
Updated: Added custom_name support.
"""

from datetime import datetime
from typing import List

from src.model.save.constants import CURRENT_SAVE_SCHEMA_VERSION
from src.model.save.dtos import (
    SaveMeta, SaveFileDTO, SaveDataDTO,
    WorldDTO, PartyDTO, PlayerDTO, InventoryDTO,
    ProgressionDTO, WorldStateDTO, TurnStateDTO, CheckpointDTO
)


class GameSerializer:
    """Serializes and deserializes game model to/from dictionaries."""

    @staticmethod
    def to_dict(game_model, custom_name: str = "") -> dict:
        """
        Convert GameModel to a saveable dictionary.
        Accepts custom_name for save metadata.
        """
        gs = game_model.gamestate

        # Build character and inventory data
        characters = []
        inventories = []

        for player in gs.players:
            # Player DTO
            player_dto = PlayerDTO(
                name=player.name,
                hp=player.hp,
                max_hp=player.max_hp,
                stats={
                    'atk': player.atk,
                    'defense': player.defense,
                    'magic': player.magic,
                    'res': player.res,
                    'spd': player.spd
                },
                abilities=[ab.name for ab in (player.abilities or [])]
            )
            characters.append(player_dto)

            # Inventory DTO
            inv_dto = InventoryDTO(
                items=[
                    {'name': item.name, 'description': item.description}
                    for item in player.inventory.items
                ],
                capacity=player.inventory.max_capacity
            )
            inventories.append(inv_dto)

        # Get progression data
        aces = getattr(gs, 'aces_collected', [])
        flags = getattr(gs, 'flags', {})

        # Build world DTO
        world = WorldDTO(
            room_id=getattr(gs, 'current_room_id', 'hub'),
            spawn_id=getattr(gs, 'spawn_id', None),
            party_world_pos=getattr(gs, 'party_position', [0, 0])
        )

        # Build party DTO
        party = PartyDTO(
            characters=characters,
            num_humans=getattr(gs, 'num_humans', len(gs.players)),
            enabled_mask=[True] * len(gs.players),
            guest_id=getattr(gs, 'guest_id', None),
            inventories=inventories
        )

        # Build metadata
        meta = SaveMeta(
            timestamp_iso=datetime.now().isoformat(),
            room_id=world.room_id,
            playtime_seconds=getattr(gs, 'playtime_seconds', 0),
            aces_count=len(aces),
            aces_collected=aces,
            custom_name=custom_name  # <--- PASSED HERE
        )

        # Build complete save file structure
        save_file = SaveFileDTO(
            schema_version=CURRENT_SAVE_SCHEMA_VERSION,
            meta=meta,
            data=SaveDataDTO(
                world=world,
                party=party,
                progression=ProgressionDTO(aces=aces, flags=flags),
                world_state=WorldStateDTO(
                    removed_entities=getattr(gs, 'removed_entities', [])
                ),
                turn_state=TurnStateDTO(
                    exploration_active_index=getattr(gs, 'exploration_active_index', 0),
                    awaiting_handoff_confirm=getattr(gs, 'awaiting_handoff_confirm', False)
                ),
                checkpoint=CheckpointDTO(
                    checkpoint_room_id=getattr(gs, 'checkpoint_room_id', None),
                    checkpoint_spawn_id=getattr(gs, 'checkpoint_spawn_id', None)
                )
            )
        )

        return save_file.to_dict()

    @staticmethod
    def from_dict(d: dict, game_model) -> bool:
        """
        Restore GameModel from a dictionary.
        """
        try:
            save_file = SaveFileDTO.from_dict(d)
            gs = game_model.gamestate

            # Restore world state
            gs.current_room_id = save_file.data.world.room_id
            gs.spawn_id = save_file.data.world.spawn_id
            gs.party_position = GameSerializer._validate_position(
                save_file.data.world.party_world_pos
            )

            # Restore party info
            gs.num_humans = save_file.data.party.num_humans
            gs.guest_id = save_file.data.party.guest_id

            # Restore characters
            gs.players = GameSerializer._restore_characters(
                save_file.data.party.characters,
                save_file.data.party.inventories
            )

            # Restore progression
            gs.aces_collected = save_file.data.progression.aces
            gs.flags = save_file.data.progression.flags

            # Restore world state
            gs.removed_entities = save_file.data.world_state.removed_entities

            # Restore turn state
            gs.exploration_active_index = save_file.data.turn_state.exploration_active_index
            gs.awaiting_handoff_confirm = save_file.data.turn_state.awaiting_handoff_confirm

            # Restore checkpoint
            gs.checkpoint_room_id = save_file.data.checkpoint.checkpoint_room_id
            gs.checkpoint_spawn_id = save_file.data.checkpoint.checkpoint_spawn_id

            # Restore metadata
            gs.playtime_seconds = save_file.meta.playtime_seconds

            # Compatibility fields
            gs.current_room = 1
            gs.current_level = 1
            gs.is_running = True

            return True

        except Exception as e:
            print(f"Error restoring game model: {e}")
            return False

    @staticmethod
    def _validate_position(pos) -> List[int]:
        if isinstance(pos, list) and len(pos) == 2:
            if all(isinstance(p, (int, float)) for p in pos):
                return [int(pos[0]), int(pos[1])]
        return [0, 0]

    @staticmethod
    def _restore_characters(characters_dto, inventories_dto) -> list:
        from src.model.character import Character, Inventory, Ability

        players = []

        for i, char_dto in enumerate(characters_dto):
            char = Character()

            # Basic info
            char.name = char_dto.name
            char.hp = char_dto.hp
            char.max_hp = char_dto.max_hp

            # Stats
            char.atk = char_dto.stats.get('atk', 5)
            char.defense = char_dto.stats.get('defense', 3)
            char.magic = char_dto.stats.get('magic', 4)
            char.res = char_dto.stats.get('res', 2)
            char.spd = char_dto.stats.get('spd', 1)

            # Abilities
            char.abilities = []
            for ab_name in char_dto.abilities:
                ab = Ability()
                ab.name = ab_name
                ab.description = f"Ability: {ab_name}"
                char.abilities.append(ab)

            # Inventory
            char.inventory = Inventory()
            if i < len(inventories_dto):
                inv_dto = inventories_dto[i]
                char.inventory.max_capacity = inv_dto.capacity
                for item_data in inv_dto.items:
                    char.inventory.add_item(
                        item_data.get('name', ''),
                        item_data.get('description', '')
                    )

            players.append(char)

        return players