package io.github.iss_2025_2026.service;

import com.badlogic.gdx.maps.MapObject;
import io.github.iss_2025_2026.model.Npc;

public class NpcInteraction {
    private final MapObject mapObject;
    private final Npc npc;
    private final float x;
    private final float y;

    public NpcInteraction(MapObject mapObject, Npc npc) {
        this(mapObject, npc, 0f, 0f);
    }

    public NpcInteraction(MapObject mapObject, Npc npc, float x, float y) {
        this.mapObject = mapObject;
        this.npc = npc;
        this.x = x;
        this.y = y;
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public Npc getNpc() {
        return npc;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
