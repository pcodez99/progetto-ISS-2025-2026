Contratto TMX runtime

Le mappe vengono create e mantenute in Tiled. Il gioco non deve generare mappe o ostacoli via codice.

- map/levels.yaml dichiara i livelli runtime disponibili.
- In dev_mode=true e' obbligatorio solo il livello 1; in dev_mode=false sono obbligatori tutti e 3 i livelli previsti.
- map/levels/<id>/level.tmx e' la sorgente TMX del livello.
- map/shared/tiles/isometric contiene i tile comuni ai livelli.
- map/levels/<id>/assets contiene gli asset specifici del livello.
- Il layer oggetti "Spawn" deve contenere un oggetto visibile chiamato "Spawn".
- Il layer oggetti "Ostacoli" contiene collisioni statiche: solo gli oggetti con proprieta bool collision=true vengono trasformati in corpi Box2D.
- Il layer oggetti opzionale "RigidBodies" contiene corpi fisici statici letti direttamente dal TMX.
- Gli oggetti fisici supportati sono rectangle, polygon e polyline.

