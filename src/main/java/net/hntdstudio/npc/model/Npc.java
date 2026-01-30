package net.hntdstudio.npc.model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Npc {
    private String uuid;
    private String name;

    private transient Ref<EntityStore> cachedRef;

    public Npc(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public Ref<EntityStore> getRef(@NonNull Store<EntityStore> store) {
        if (cachedRef == null || !cachedRef.isValid()) {
            cachedRef = store.getExternalData().getRefFromUUID(UUID.fromString(uuid));
        }
        return cachedRef;
    }
}
