package net.hntdstudio.dialogue.interactions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NonNull;
import net.hntdstudio.core.Main;
import net.hntdstudio.dialogue.ui.pages.DialoguePage;
import net.hntdstudio.dialogue.model.DialogueData;

import javax.annotation.Nonnull;

@Getter
public class DialogueInteraction extends SimpleInteraction {
    private final Main main;
    private final DialogueData dialogueData;

    public DialogueInteraction(@NonNull Main main, @NonNull String id, @NonNull DialogueData dialogueData) {
        super(id);
        this.main = main;
        this.dialogueData = dialogueData;
    }

    @Override
    protected void tick0(
            boolean firstRun,
            float time,
            @NonNull InteractionType type,
            @Nonnull InteractionContext context,
            @NonNull CooldownHandler cooldownHandler
    ) {
        super.tick0(firstRun, time, type, context, cooldownHandler);

        if (firstRun) {
            Ref<EntityStore> ref = context.getEntity();

            if (ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                Player player = store.getComponent(ref, Player.getComponentType());


                if (player != null) {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

                    if (playerRef != null) {
                        player.getPageManager().openCustomPage(ref, store, new DialoguePage(main, playerRef, dialogueData));
                    }
                }
            }
        }
    }

}