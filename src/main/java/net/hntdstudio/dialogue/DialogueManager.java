package net.hntdstudio.dialogue;

import com.google.gson.Gson;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import lombok.Getter;
import net.hntdstudio.core.Main;
import net.hntdstudio.dialogue.interactions.DialogueInteraction;
import net.hntdstudio.dialogue.model.DialogueData;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class DialogueManager {
    private final Main main;
    @Getter
    private final File dialogueDirectory;
    @Getter
    private List<DialogueData> dialogues;

    public DialogueManager(Main main) {
        this.main = main;
        this.dialogueDirectory = new File(this.main.getBasePath() + "/dialogue");
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


}
