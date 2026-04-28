"""
Animation System - Componente animazione per personaggi e entità
Epic 3: User Story 12
"""

from dataclasses import dataclass, field
from typing import Optional, List, Dict, Tuple
from enum import Enum, auto
import pygame


class AnimationState(Enum):
    """Stati di animazione predefiniti"""
    IDLE = auto()
    WALK = auto()
    ATTACK = auto()
    HIT = auto()
    KO = auto()
    
    @classmethod
    def from_string(cls, name: str) -> 'AnimationState':
        """Converte stringa in AnimationState"""
        name_upper = name.upper()
        for state in cls:
            if state.name == name_upper:
                return state
        raise ValueError(f"Unknown animation state: {name}")


@dataclass
class AnimationFrame:
    """Singolo frame di animazione"""
    surface: pygame.Surface
    duration: float
    
    @classmethod
    def create_placeholder(cls, width: int = 32, height: int = 32, 
                          color: tuple = (128, 128, 128)) -> 'AnimationFrame':
        """Crea un frame placeholder per testing"""
        surface = pygame.Surface((width, height))
        surface.fill(color)
        return cls(surface=surface, duration=0.1)


@dataclass
class AnimationClip:
    """
    Clip di animazione contenente una sequenza di frame.
    """
    name: str
    frames: List[AnimationFrame]
    loop: bool = True
    
    @property
    def total_duration(self) -> float:
        """Calcola la durata totale del clip"""
        return sum(f.duration for f in self.frames)
    
    @property
    def frame_count(self) -> int:
        """Numero di frame nel clip"""
        return len(self.frames)
    
    def get_frame_at_time(self, time: float) -> Tuple[int, Optional[AnimationFrame], bool]:
        """
        Ottiene il frame e l'indice per un dato tempo.
        
        Returns:
            (frame_index, AnimationFrame, is_complete)
        """
        if not self.frames:
            return (0, None, True)
        
        total = self.total_duration
        if total <= 0:
            return (0, self.frames[0], False)
        
        if self.loop:
            time = time % total
        elif time >= total:
            return (len(self.frames) - 1, self.frames[-1], True)
        
        accumulated = 0.0
        for i, frame in enumerate(self.frames):
            accumulated += frame.duration
            if time < accumulated:
                return (i, frame, False)
        
        return (len(self.frames) - 1, self.frames[-1], not self.loop)
    
    @classmethod
    def create_placeholder(cls, name: str, frame_count: int = 4, 
                          frame_duration: float = 0.1,
                          loop: bool = True,
                          color: tuple = (128, 128, 128)) -> 'AnimationClip':
        """Crea un clip placeholder per testing"""
        frames = []
        for _ in range(frame_count):
            frame = AnimationFrame.create_placeholder(color=color)
            frame.duration = frame_duration
            frames.append(frame)
        return cls(name=name, frames=frames, loop=loop)


class AnimationComponent:
    """
    Componente per gestire le animazioni di un'entità.
    
    Supporta:
    - Stati looping (idle, walk, KO)
    - One-shot (attack, hit) con return state
    - Lock su KO
    - Attack uninterruptible by hit
    """
    
    STATE_IDLE = "idle"
    STATE_WALK = "walk"
    STATE_ATTACK = "attack"
    STATE_HIT = "hit"
    STATE_KO = "ko"
    
    ONE_SHOT_STATES = {STATE_ATTACK, STATE_HIT}
    UNINTERRUPTIBLE_STATES = {STATE_ATTACK}
    
    def __init__(self):
        self._clips: Dict[str, AnimationClip] = {}
        self._current_state: str = self.STATE_IDLE
        self._current_clip: Optional[AnimationClip] = None
        self._frame_index: int = 0
        self._time_accumulator: float = 0.0
        self._one_shot_active: bool = False
        self._one_shot_return_state: Optional[str] = None
        self._one_shot_name: Optional[str] = None
        self._locked: bool = False
        self._previous_state: str = self.STATE_IDLE
    
    @property
    def current_state(self) -> str:
        return self._current_state
    
    @property
    def current_frame(self) -> Optional[pygame.Surface]:
        if self._current_clip and self._current_clip.frames:
            if 0 <= self._frame_index < len(self._current_clip.frames):
                return self._current_clip.frames[self._frame_index].surface
        return None
    
    @property
    def is_locked(self) -> bool:
        return self._locked
    
    @property
    def is_one_shot_active(self) -> bool:
        return self._one_shot_active
    
    @property
    def frame_index(self) -> int:
        return self._frame_index
    
    @property
    def time_accumulator(self) -> float:
        return self._time_accumulator
    
    def add_clip(self, name: str, clip: AnimationClip) -> None:
        """Aggiunge un clip alla libreria"""
        self._clips[name] = clip
    
    def has_clip(self, name: str) -> bool:
        """Verifica se un clip esiste"""
        return name in self._clips
    
    def set_state(self, name: str, force: bool = False) -> bool:
        """
        Imposta uno stato looping (idle, walk, ecc.).
        
        Returns:
            True se lo stato è stato cambiato
        """
        if self._locked and not force:
            return False
        
        if self._one_shot_active and not force:
            return False
        
        if self._current_state == name and not force:
            return False
        
        if name not in self._clips:
            return False
        
        if self._current_state not in self.ONE_SHOT_STATES:
            self._previous_state = self._current_state
        
        self._current_state = name
        self._current_clip = self._clips[name]
        self._frame_index = 0
        self._time_accumulator = 0.0
        self._one_shot_active = False
        self._one_shot_name = None
        self._one_shot_return_state = None
        
        if name == self.STATE_KO:
            self._locked = True
        
        return True
    
    def play_one_shot(self, name: str, return_state: Optional[str] = None) -> bool:
        """
        Avvia un'animazione one-shot (attack, hit).
        
        Returns:
            True se il one-shot è stato avviato
        """
        if self._locked:
            return False
        
        if self._one_shot_active:
            if self._one_shot_name in self.UNINTERRUPTIBLE_STATES:
                return False
            return False
        
        if name not in self._clips:
            return False
        
        if return_state is None:
            if self._current_state not in self.ONE_SHOT_STATES:
                return_state = self._current_state
            else:
                return_state = self._previous_state
        
        self._previous_state = self._current_state
        self._current_state = name
        self._current_clip = self._clips[name]
        self._frame_index = 0
        self._time_accumulator = 0.0
        self._one_shot_active = True
        self._one_shot_name = name
        self._one_shot_return_state = return_state
        
        return True
    
    def update(self, dt: float) -> None:
        """Aggiorna l'animazione."""
        if not self._current_clip or not self._current_clip.frames:
            return
        
        self._time_accumulator += dt
        
        frame_idx, frame, is_complete = self._current_clip.get_frame_at_time(
            self._time_accumulator
        )
        self._frame_index = frame_idx
        
        if is_complete and self._one_shot_active:
            self._complete_one_shot()
    
    def _complete_one_shot(self) -> None:
        """Completa un one-shot e torna allo stato di ritorno"""
        self._one_shot_active = False
        self._one_shot_name = None
        
        return_state = self._one_shot_return_state or self.STATE_IDLE
        self._one_shot_return_state = None
        
        if not self._locked and return_state in self._clips:
            self._current_state = return_state
            self._current_clip = self._clips[return_state]
            self._frame_index = 0
            self._time_accumulator = 0.0
    
    def reset(self) -> None:
        """Resetta completamente il componente"""
        self._current_state = self.STATE_IDLE
        self._current_clip = self._clips.get(self.STATE_IDLE)
        self._frame_index = 0
        self._time_accumulator = 0.0
        self._one_shot_active = False
        self._one_shot_name = None
        self._one_shot_return_state = None
        self._locked = False
        self._previous_state = self.STATE_IDLE
    
    def unlock(self, force: bool = True) -> None:
        """Sblocca il componente (per revive)"""
        if force:
            self._locked = False


class AnimationController:
    """
    Controller per gestire animazioni basate su stato di gioco.
    """
    
    def __init__(self, animation_component: AnimationComponent):
        self.animation = animation_component
        self._velocity_threshold: float = 0.1
    
    def update_locomotion(self, velocity_x: float, velocity_y: float) -> None:
        """Aggiorna l'animazione in base alla velocità."""
        if self.animation.is_one_shot_active:
            return
        
        if self.animation.is_locked:
            return
        
        speed = (velocity_x ** 2 + velocity_y ** 2) ** 0.5
        
        if speed > self._velocity_threshold:
            self.animation.set_state(AnimationComponent.STATE_WALK)
        else:
            self.animation.set_state(AnimationComponent.STATE_IDLE)
    
    def trigger_attack(self) -> bool:
        """Avvia animazione attack"""
        return self.animation.play_one_shot(AnimationComponent.STATE_ATTACK)
    
    def trigger_hit(self) -> bool:
        """Avvia animazione hit (se non in attack)"""
        return self.animation.play_one_shot(AnimationComponent.STATE_HIT)
    
    def trigger_ko(self) -> bool:
        """Imposta stato KO (lock permanente)"""
        return self.animation.set_state(AnimationComponent.STATE_KO, force=True)
    
    def handle_damage(self, current_hp: int) -> None:
        """Gestisce danno: trigger hit o KO in base a HP"""
        if current_hp <= 0:
            self.trigger_ko()
        else:
            self.trigger_hit()