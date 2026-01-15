package net.hntdstudio.npc;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.hntdstudio.npc.model.Npc;

import java.util.ArrayList;
import java.util.List;

//Keep track of every NPC used by the plugin
public class NpcManager {
    private static NpcManager instance;
    @Getter
    private List<Npc> npcs;

    public NpcManager() {
        instance = this;
        this.npcs = new ArrayList<>();
    }

    public void registerNpc(Ref<EntityStore> entityStore, String name){
        Npc npc = new Npc(entityStore, name);
        addNpc(npc);
    }

    private void addNpc(Npc npc) {
        this.npcs.add(npc);
    }

    private void removeNpc(Npc npc) {
        this.npcs.remove(npc);
    }

    public Npc getNpcById(String id) {
        return this.npcs.stream()
                .filter(npc -> npc.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    public static NpcManager get() {
        return instance;
    }

}
