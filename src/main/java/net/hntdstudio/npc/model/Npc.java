package net.hntdstudio.npc.model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Npc {
    private String id;
    private Ref<EntityStore> ref;
    private String name;

    public Npc(Ref<EntityStore> ref, String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.ref = ref;
        this.name = name;
    }
}
