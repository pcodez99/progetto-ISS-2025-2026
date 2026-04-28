"""
Sikula: L'ultimo brindisi
Main Entry Point - The Game Loop
"""
import sys
import os
import pygame

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

from src.controller.game_controller import GameController
from src.model.states.base_state import StateID

SCREEN_WIDTH = 800
SCREEN_HEIGHT = 600
TARGET_FPS = 60

def main():
    pygame.init()
    pygame.font.init()
    pygame.display.set_caption("Sikula")
    screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
    clock = pygame.time.Clock()

    print("[Main] Initializing Controllers...")
    controller = GameController()
    
    # Carica i contenuti (stanze, oggetti, script)
    controller.game.load_content()
    
    # Start at MAIN MENU
    print("[Main] Entering Main Menu...")
    controller.state_machine.change_state(StateID.MAIN_MENU)
    
    print("[Main] Game Loop Started.")
    running = True
    
    while running:
        dt = clock.tick(TARGET_FPS) / 1000.0
        controller.render_controller.update_fps(clock.get_fps())

        events = pygame.event.get()
        for event in events:
            if event.type == pygame.QUIT:
                running = False
            
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_F3:
                    controller.render_controller.toggle_debug()
                if event.key == pygame.K_F4:
                    is_full = screen.get_flags() & pygame.FULLSCREEN
                    if is_full:
                        screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
                    else:
                        screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT), pygame.FULLSCREEN)

            controller.input_manager.process_event(event)
            controller.state_machine.handle_event(event)

        controller.process_frame(dt)

        # D. RENDERING
        screen.fill((0, 0, 0)) 
        
        controller.render_controller.begin_frame()
        
        sm = controller.state_machine
        
        # 1. Logic per Background/Mondo (Stati Principali)
        
        # --- BOSS FINALE ---
        if sm.has_state(StateID.BOSS_OSTE):
            boss_state = sm._get_state(StateID.BOSS_OSTE)
            boss_state.render(screen)

        # --- CUTSCENE (INTRO/OUTRO) --- 
        elif sm.has_state(StateID.CUTSCENE):
            cutscene_state = sm._get_state(StateID.CUTSCENE)
            # La cutscene si disegna da sola direttamente sulla surface
            cutscene_state.render(screen)
        
        # --- ALTRI STATI ---
        elif sm.has_state(StateID.COMBAT):
            combat_state = sm._get_state(StateID.COMBAT)
            controller.render_controller.render_combat(screen, combat_state)
        elif sm.has_state(StateID.SCOPA):
            scopa_state = sm._get_state(StateID.SCOPA)
            controller.render_controller.render_scopa(screen, scopa_state)
        elif sm.has_state(StateID.BRISCOLA):
            briscola_state = sm._get_state(StateID.BRISCOLA)
            controller.render_controller.render_briscola(screen, briscola_state)
        elif sm.has_state(StateID.SETTE_MEZZO):
            sm_state = sm._get_state(StateID.SETTE_MEZZO)
            controller.render_controller.render_sette_mezzo(screen, sm_state)
        elif sm.has_state(StateID.CUCU): 
            cucu_state = sm._get_state(StateID.CUCU)
            controller.render_controller.render_cucu(screen, cucu_state)
        elif sm.has_state(StateID.ACES_MENU): 
            aces_state = sm._get_state(StateID.ACES_MENU)
            controller.render_controller.render_aces_menu(screen, aces_state)
        elif sm.has_state(StateID.HUB) or sm.has_state(StateID.ROOM):
            # Se siamo in gioco (anche sotto pausa), renderizziamo il mondo
            controller.render_controller.render_game_state(screen, controller.game, dt)
        elif sm.has_state(StateID.MAIN_MENU):
            menu_state = sm._get_state(StateID.MAIN_MENU)
            controller.render_controller.render_main_menu(screen, menu_state)

        # 2. Logic per Overlay/Popup
        # Nota: BOSS_OSTE e CUTSCENE non usano overlay standard sopra di essi in questo modo
        if not sm.has_state(StateID.BOSS_OSTE) and not sm.has_state(StateID.CUTSCENE):
            current_state = sm.peek()
            if current_state:
                if current_state.state_id == StateID.INVENTORY:
                    controller.render_controller.render_inventory(screen, current_state)
                elif current_state.state_id == StateID.PAUSE: 
                    controller.render_controller.render_pause_menu(screen, current_state)
                elif current_state.state_id == StateID.SAVE_LOAD: 
                    controller.render_controller.render_save_load(screen, current_state)
                elif current_state.state_id == StateID.GAME_OVER: 
                    controller.render_controller.render_game_over(screen, current_state)
                elif current_state.is_overlay: 
                    # Renderizza elementi UI generici (dialoghi, prompt)
                    current_state.render(screen)

        pygame.display.flip()

    pygame.quit()
    sys.exit()

if __name__ == "__main__":
    main()