package io.github.iss_2025_2026.service;

import com.badlogic.gdx.maps.MapObject;
import io.github.iss_2025_2026.model.Npc;

public class NpcInteraction {
    private final MapObject mapObject;
    private final Npc npc;

    public NpcInteraction(MapObject mapObject, Npc npc) {
        this.mapObject = mapObject;
        this.npc = npc;
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public Npc getNpc() {
        return npc;
    }
}
