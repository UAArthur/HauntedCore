package net.hntdstudio.npc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import net.hntdstudio.dialogue.DialogueManager;
import net.hntdstudio.dialogue.model.DialogueData;
import net.hntdstudio.npc.model.Npc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//Keep track of every NPC used by the plugin
public class NpcManager {
    private final Main main;
    private static NpcManager instance;
    private final File dataFile;
    private final Gson gson;
    @Getter
    private List<Npc> npcs;
    public NpcManager(@NonNull Main main) {
        this.main = main;
        instance = this;
        this.npcs = new ArrayList<>();

        dataFile = new File(main.getDataDirectory() +  "/npcs.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        this.loadNpcs();
    }

    private void loadNpcs() {
        if (!dataFile.exists()) {
            HytaleLogger.forEnclosingClass().atInfo()
                    .log("No NPC data file found, starting with empty list");
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type listType = new TypeToken<List<Npc>>(){}.getType();
            List<Npc> loadedNpcs = gson.fromJson(reader, listType);

            if (loadedNpcs != null) {
                npcs = loadedNpcs;
                HytaleLogger.forEnclosingClass().atInfo()
                        .log("Loaded %d NPCs from %s", npcs.size(), dataFile.getName());
            }
        } catch (Exception e) {
            HytaleLogger.forEnclosingClass().atSevere()
                    .log("Error loading NPCs from %s: %s", dataFile.getName(), e.getMessage());
        }
    }

    public void saveNpcs() {
        try {
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(npcs, writer);
                HytaleLogger.forEnclosingClass().atInfo()
                        .log("Saved %d NPCs to %s", npcs.size(), dataFile.getName());
            }
        } catch (Exception e) {
            HytaleLogger.forEnclosingClass().atSevere()
                    .log("Error saving NPCs to %s: %s", dataFile.getName(), e.getMessage());
        }
    }

    public void registerNpc(String uuid, String name) {
        if (getNpcByUuid(uuid) != null) {
            HytaleLogger.forEnclosingClass().atWarning()
                    .log("NPC with UUID %s already exists", uuid);
            return;
        }

        Npc npc = new Npc(uuid, name);
        addNpc(npc);
        saveNpcs();

        HytaleLogger.forEnclosingClass().atInfo()
                .log("Registered NPC '%s' with UUID %s", name, uuid);
    }
    private void addNpc(Npc npc) {
        this.npcs.add(npc);
    }

    private void removeNpc(Npc npc) {
        this.npcs.remove(npc);
    }

    public Npc getNpcByUuid(String uuid) {
        return this.npcs.stream()
                .filter(npc -> npc.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public static NpcManager get() {
        return instance;
    }

}
