package net.hntdstudio.dialogue;

import com.google.gson.Gson;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NonNull;
import net.hntdstudio.core.Main;
import net.hntdstudio.dialogue.interactions.DialogueInteraction;
import net.hntdstudio.dialogue.model.DialogueData;
import net.hntdstudio.npc.NpcManager;
import net.hntdstudio.npc.model.Npc;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class DialogueManager {
    private final Main main;
    private static DialogueManager instance;
    @Getter
    private final File dialogueDirectory;
    @Getter
    private List<DialogueData> dialogues;

    public DialogueManager(Main main) {
        this.main = main;
        instance = this;
        this.dialogueDirectory = new File(this.main.getDataDirectory() + "/dialogue");
        if (!dialogueDirectory.exists())
            dialogueDirectory.mkdirs();

        this.setup();
    }

    private void setup() {
        this.dialogues = loadDialogues(dialogueDirectory);
        this.loadInteractions();
    }

    public static List<DialogueData> loadDialogues(File dialogueDirectory) {
        Gson gson = new Gson();
        List<DialogueData> list = new ArrayList<>();

        Arrays.stream(Objects.requireNonNull(dialogueDirectory.listFiles())).forEach(file -> {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try (FileReader reader = new FileReader(file)) {
                    DialogueData dialogueData = gson.fromJson(reader, DialogueData.class);
                    if (dialogueData != null) {
                        list.add(dialogueData);
                        HytaleLogger.forEnclosingClass().atInfo()
                                .log("Loaded dialogue %s from %s",
                                        dialogueData.getDialogueId(), file.getName());
                    }
                } catch (Exception e) {
                    HytaleLogger.forEnclosingClass().atSevere()
                            .log("Error while trying to load dialogues from %s: %s",
                                    file.getName(), e.getMessage());
                }
            }
        });

        return list;
    }

    private void loadInteractions() {
        getDialogues().forEach(dialogue -> {
            DialogueInteraction defaultInteraction = new DialogueInteraction(
                    main,
                    "*" + dialogue.getDialogueId(),
                    dialogue
            );
            Interaction.getAssetStore().loadAssets("uaarthur:HNTDCore", List.of(defaultInteraction));

            RootInteraction rootInteraction = new RootInteraction(
                    "*" + dialogue.getDialogueId(),
                    "*" + dialogue.getDialogueId()
            );
            RootInteraction.getAssetStore().loadAssets("uaarthur:HNTDCore", List.of(rootInteraction));
        });
    }

    public void ensureDialogueAssigned(@NonNull PlayerRef playerRef, @NonNull Npc npc, @NonNull Store<EntityStore> store, @NonNull World world) {
        Ref<EntityStore> npcRef = npc.getRef(store);
        if (npcRef == null || !npcRef.isValid()) {
            System.out.println("NPC ref is null or invalid for: " + npc.getName());
            return;
        }

        Interactions existing = store.getComponent(npcRef, Interactions.getComponentType());
        if (existing != null) {
            String existingId = existing.getInteractionId(InteractionType.Use);
            if (existingId != null && !existingId.equals("*UseNPC")) {
                return;
            }
        }

        DialogueData dialogue = dialogues.stream()
                .filter(d -> npc.getUuid().equalsIgnoreCase(d.getNpcId()))
                .findFirst()
                .orElse(null);

        if (dialogue == null) {
            System.out.println("No dialogue found for NPC: " + npc.getName() + " (UUID: " + npc.getUuid() + ")");
            return;
        }

        if (!dialogue.getSymbol().isEmpty())
            System.out.println("Adding Symbol");
        System.out.println("Assigning dialogue '" + dialogue.getDialogueId() + "' to NPC '" + npc.getName() + "'");

        this.main.getSymbolManager().getSymbol("exclamation").ifPresent(symbol -> {
            symbol.spawn(world, playerRef, npcRef, store, this.main.getSymbolManager().getNextNetworkId());
        });

        assignInteractionToEntity(world, store, npcRef, npc, dialogue.getDialogueId());
    }

    public void assignInteractionToEntity(
            @NonNull World world,
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> npcRef,
            @NonNull Npc npc,
            @NonNull String dialogueId
    ) {
        if (dialogueId.isEmpty()) {
            HytaleLogger.forEnclosingClass().atWarning()
                    .log("Cannot assign empty dialogue ID to NPC %s", npc.getName());
            return;
        }

        DialogueData dialogue = getDialogueById(dialogueId);
        if (dialogue == null) {
            HytaleLogger.forEnclosingClass().atWarning()
                    .log("Dialogue %s not found", dialogueId);
            return;
        }

        dialogue.setNpcId(npc.getUuid());
        saveDialogue(dialogue);

        world.execute(() -> {
            try {
                store.ensureComponent(npcRef, Interactable.getComponentType());

                Interactions interactionsComponent = store.getComponent(
                        npcRef,
                        Interactions.getComponentType()
                );

                if (interactionsComponent == null) {
                    interactionsComponent = new Interactions();
                    store.addComponent(
                            npcRef,
                            Interactions.getComponentType(),
                            interactionsComponent
                    );
                }

                interactionsComponent.setInteractionId(InteractionType.Use, "*" + dialogueId);
                interactionsComponent.setInteractionHint(""); // Remove default hint (cus of custom UI)

                HytaleLogger.forEnclosingClass().atInfo()
                        .log("Assigned dialogue '%s' to NPC '%s'", dialogueId, npc.getName());
            } catch (Exception e) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Error assigning dialogue to NPC %s: %s", npc.getName(), e.getMessage());
            }
        });
    }

    public void removeInteractionFromEntity(
            @NonNull World world,
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> npcRef,
            @NonNull Npc npc
    ) {
        world.execute(() -> {
            try {
                Interactions interactionsComponent = store.getComponent(
                        npcRef,
                        Interactions.getComponentType()
                );

                if (interactionsComponent != null) {
                    interactionsComponent.setInteractionId(InteractionType.Use, null);
                    HytaleLogger.forEnclosingClass().atInfo()
                            .log("Removed dialogue interaction from NPC '%s'", npc.getName());
                }
            } catch (Exception e) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Error removing dialogue from NPC %s: %s", npc.getName(), e.getMessage());
            }
        });
    }

    public DialogueData getDialogueById(String dialogueId) {
        return dialogues.stream()
                .filter(d -> Objects.equals(d.getDialogueId(), dialogueId))
                .findFirst()
                .orElse(null);
    }

    public boolean dialogueExists(String dialogueId) {
        return getDialogueById(dialogueId) != null;
    }

    public List<String> getAllDialogueIds() {
        return dialogues.stream()
                .map(DialogueData::getDialogueId)
                .toList();
    }

    public void saveDialogue(DialogueData dialogueData) {
        if (dialogueData == null || dialogueData.getDialogueId() == null) {
            HytaleLogger.forEnclosingClass().atWarning()
                    .log("Cannot save null dialogue or dialogue with null ID");
            return;
        }

        Gson gson = new com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create();

        File file = new File(dialogueDirectory, dialogueData.getDialogueId() + ".json");

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            gson.toJson(dialogueData, writer);
            HytaleLogger.forEnclosingClass().atInfo()
                    .log("Saved dialogue %s to %s",
                            dialogueData.getDialogueId(), file.getName());

            DialogueData existing = getDialogueById(dialogueData.getDialogueId());
            if (existing != null) {
                dialogues.remove(existing);
            }
            dialogues.add(dialogueData);

        } catch (Exception e) {
            HytaleLogger.forEnclosingClass().atSevere()
                    .log("Error while trying to save dialogue %s: %s",
                            dialogueData.getDialogueId(), e.getMessage());
        }
    }

    public void reload() {
        dialogues.clear();
        setup();
        HytaleLogger.forEnclosingClass().atInfo()
                .log("Reloaded all dialogues");
    }

    public static DialogueManager get() {
        return instance;
    }
}