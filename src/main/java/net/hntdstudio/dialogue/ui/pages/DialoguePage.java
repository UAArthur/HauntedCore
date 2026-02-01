package net.hntdstudio.dialogue.ui.pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.NonNull;
import net.hntdstudio.core.Main;
import net.hntdstudio.dialogue.model.DialogueData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class DialoguePage extends CustomUIPage {
    private final Main main;
    private final DialogueData dialogueData;

    private int currentDialogueIndex = 1;
    private int currentMessageIndex = 0;
    private String[] messages;
    public DialoguePage(@NonNull Main main, @NonNull PlayerRef playerRef, @NonNull DialogueData dialogueData) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.main = main;
        this.dialogueData = dialogueData;
    }

    @Override
    public void build(@NonNull Ref<EntityStore> ref,
                      @NonNull UICommandBuilder commandBuilder,
                      @NonNull UIEventBuilder uiEventBuilder,
                      @NonNull Store<EntityStore> store) {
        main.hideUIForPlayer(playerRef, ref, store);
        commandBuilder.append("Pages/Dialogue.ui");


        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Button",
                EventData.of("dialogue", "next")
        );

        dialogueData.getDialogues().stream()
                .filter(dialogue -> dialogue.getId() == currentDialogueIndex)
                .findFirst()
                .ifPresent(dialogue -> {
                    commandBuilder.set("#Title #TitleText.Text", dialogue.getDialogueFrom());
                    this.messages = dialogue.getDialogueText().split("\\|\\|");
                    commandBuilder.set("#Content #DialogueText.Text", messages[currentMessageIndex]);
                });
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                String rawData) {

        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        assert player != null;

        if (rawData.contains("\"dialogue\":\"next\"")) {
            if (messages != null && currentMessageIndex + 1 < messages.length) {
                currentMessageIndex++;
                this.updateDialogueText();
            } else {
                //TODO check for if there are answers to choose from
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } else if (rawData.contains("\"dialogue\":\"close\"")) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private void updateDialogueText() {
        if (messages == null || currentMessageIndex >= messages.length) return;
        UICommandBuilder commandBuilder = new UICommandBuilder();
        commandBuilder.set("#Content #DialogueText.Text", messages[currentMessageIndex]);
        this.sendUpdate(commandBuilder);
    }
}