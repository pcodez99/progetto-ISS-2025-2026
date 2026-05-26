Contratto TMX runtime

La mappa campagna viene creata e mantenuta in Tiled. Il gioco non deve generare mappe o ostacoli via codice.

- map/campagna.tmx e' la sorgente della mappa campagna.
- Il layer oggetti "Spawn" deve contenere un oggetto visibile chiamato "Spawn".
- Il layer oggetti "Ostacoli" contiene collisioni statiche: solo gli oggetti con proprieta bool collision=true vengono trasformati in corpi Box2D.
- Il layer oggetti opzionale "RigidBodies" contiene corpi fisici statici letti direttamente dal TMX.
- Gli oggetti fisici supportati sono rectangle, polygon e polyline.

