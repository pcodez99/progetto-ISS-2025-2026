"""
Visual demo with pygame window for testing basic functions
"""

import pygame
from src.model.render_system import Renderer, Camera
from src.model.room_data import RoomData
from src.view.room_view import RoomView

# Initialize
pygame.init()
screen = pygame.display.set_mode((800, 600))
pygame.display.set_caption("Game Demo")
clock = pygame.time.Clock()

# Setup systems
renderer = Renderer()
camera = Camera(800, 600)
room_view = RoomView(renderer, camera)

# Load room
room = RoomData.create_hub()
player_x, player_y = room_view.load_room(room, "default")

# Player rect (simple square)
player_rect = pygame.Rect(player_x - 16, player_y - 16, 32, 32)
player_surface = pygame.Surface((32, 32))
player_surface.fill((0, 255, 0))

# Game loop
running = True
speed = 5

while running:
    # Events
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        elif event.type == pygame.KEYDOWN:
            if event.key == pygame.K_ESCAPE:
                running = False
    
    # Input
    keys = pygame.key.get_pressed()
    if keys[pygame.K_w] or keys[pygame.K_UP]:
        player_rect.y -= speed
    if keys[pygame.K_s] or keys[pygame.K_DOWN]:
        player_rect.y += speed
    if keys[pygame.K_a] or keys[pygame.K_LEFT]:
        player_rect.x -= speed
    if keys[pygame.K_d] or keys[pygame.K_RIGHT]:
        player_rect.x += speed
    
    # Simple collision
    for collider in room.colliders:
        if player_rect.colliderect(collider.rect):
            # Push back (simple)
            if keys[pygame.K_w] or keys[pygame.K_UP]:
                player_rect.y += speed
            if keys[pygame.K_s] or keys[pygame.K_DOWN]:
                player_rect.y -= speed
            if keys[pygame.K_a] or keys[pygame.K_LEFT]:
                player_rect.x += speed
            if keys[pygame.K_d] or keys[pygame.K_RIGHT]:
                player_rect.x -= speed
    
    # Render
    screen.fill((0, 0, 0))
    renderer.begin_frame()
    
    # Room background
    room_view.render(
        actors=[{'surface': player_surface, 'rect': player_rect}]
    )
    
    renderer.flush(screen, camera)
    
    pygame.display.flip()
    clock.tick(60)

pygame.quit()
