package net.hntdstudio.npc;

import com.google.gson.Gson;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorPlayer;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorPlayer;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterSpotsMe;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterSpotsMe;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import lombok.Getter;
import lombok.NonNull;
import net.hntdstudio.core.Main;
import net.hntdstudio.dialogue.model.DialogueData;
import net.hntdstudio.npc.model.Npc;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//Keep track of every NPC used by the plugin
public class NpcManager {
    private final Main main;
    private static NpcManager instance;
    @Getter
    private List<Npc> npcs;

    public NpcManager(@NonNull Main main) {
        this.main = main;
        instance = this;
        this.npcs = new ArrayList<>();
    }

    private void loadNpcs(File npcFile) {
        Gson gson = new Gson();

        if (npcFile.isFile() && npcFile.getName().endsWith(".json")) {
            try (FileReader reader = new FileReader(npcFile)) {
                Npc npcData = gson.fromJson(reader, Npc.class);
                if (npcData != null) {
                    npcs.add(npcData);
                    HytaleLogger.forEnclosingClass().atInfo()
                            .log("Loaded npcs %s from %s",
                                    npcData.getName(), npcFile.getName());
                }
            } catch (Exception e) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Error while trying to load dialogues from %s: %s",
                                npcFile.getName(), e.getMessage());
            }
        }

    }

    public void registerNpc(String uuid, String name) {
        Npc npc = new Npc(uuid, name);
        addNpc(npc);
    }

    private void addNpc(Npc npc) {
        this.npcs.add(npc);
    }

    private void removeNpc(Npc npc) {
        this.npcs.remove(npc);
    }

    public Npc getNpcById(String uuid) {
        return this.npcs.stream()
                .filter(npc -> npc.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }


    public static NpcManager get() {
        return instance;
    }

}
