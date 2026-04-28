"""
Application Entry Point - Crash Safe
"""
import os
import traceback
import pygame

# Importa il gioco unificato
from src.model.game import Game
from src.model.utils.logging_setup import setup_logging
from src.view.error_screen import ErrorScreen

def run_game(mainloop_fn=None):
    # Setup base
    logger, _ = setup_logging()
    
    try:
        # Se non viene passata una funzione mainloop specifica, usa quella del gioco
        if mainloop_fn is None:
            game = Game()
            
            # Pre-load opzionale
            game.load_content()
            
            # Avvia
            print("Avvio gioco...")
            game.start_new_game(1)
        else:
            # Per testing o entry point custom
            mainloop_fn()
            
    except Exception as e:
        # CRASH HANDLER (Sicily Logic)
        os.makedirs("logs", exist_ok=True)
        crash_path = "logs/crash.log"
        
        with open(crash_path, "w", encoding="utf-8") as f:
            f.write("An error occurred\n")
            f.write("".join(traceback.format_exception(type(e), e, e.__traceback__)))

        logger.exception("Fatal crash: %s", e)

        # Se pygame è inizializzato, prova a mostrare l'errore a video
        if pygame.get_init():
            try:
                ErrorScreen(f"Fatal Error: {str(e)}\nCheck logs/crash.log").render()
            except:
                pass
        
        return None

if __name__ == "__main__":
    run_game()