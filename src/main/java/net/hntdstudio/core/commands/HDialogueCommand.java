package net.hntdstudio.core.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.NonNull;
import net.hntdstudio.npc.NpcManager;
import net.hntdstudio.npc.model.Npc;

public class HDialogueCommand extends AbstractCommandCollection {
    public HDialogueCommand(@NonNull String name, @NonNull String description) {
        super(name, description);
        addAliases("hdialog", "hd");

        addSubCommand(new HDialogueAddCommand());
    }

    private static class HDialogueAddCommand extends AbstractPlayerCommand {
        private final RequiredArg<String> npcNameArg;
        private final RequiredArg<String> dialogueIdArg;

        public HDialogueAddCommand() {
            super("add", "hdialogue.command.add.desc");

            this.npcNameArg = this.withRequiredArg(
                    "npcName",
                    "hdialogue.command.add.arg.npcname.desc",
                    ArgTypes.STRING
            );
            this.dialogueIdArg = this.withRequiredArg(
                    "dialogueId",
                    "hdialogue.command.add.arg.dialogueid.desc",
                    ArgTypes.STRING
            );
        }

        @Override
        protected void execute(
                @NonNull CommandContext commandContext,
                @NonNull Store<EntityStore> store,
                @NonNull Ref<EntityStore> ref,
                @NonNull PlayerRef playerRef,
                @NonNull World world
        ) {
            String npcName = commandContext.get(npcNameArg);
            String dialogueId = commandContext.get(dialogueIdArg);

            NpcManager npcManager = NpcManager.get();
            Npc npc = npcManager.getNpcs().stream()
                    .filter(n -> n.getName().equalsIgnoreCase(npcName))
                    .findFirst()
                    .orElse(null);

            if (npc == null) {
                commandContext.sendMessage(Message.raw("NPC not found: " + npcName));
                return;
            }

            Ref<EntityStore> npcRef = npc.getRef(store);
            if (npcRef == null || !npcRef.isValid()) {
                commandContext.sendMessage(Message.raw("NPC reference invalid"));
                return;
            }

            world.execute(() -> {
                try {
                    store.ensureComponent(npcRef, Interactable.getComponentType());

                    Interactions interactionsComponent = (Interactions) store.getComponent(
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
//                    interactionsComponent.setInteractionHint(Message.translation("hdialogue.command.interactionHints.talk")
//                            .param("name", npcName)
//                            .getAnsiMessage());
                    interactionsComponent.setInteractionHint("");
                    commandContext.sendMessage(Message.raw("Interaction hint added to NPC: " + npcName));
                } catch (Exception e) {
                    HytaleLogger.forEnclosingClass().atSevere().log("Error while trying to set the Interaction for %s \n %s ", npcName, e.getMessage());
                }
            });
        }
    }
}