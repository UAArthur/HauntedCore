package net.hntdstudio.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import lombok.NonNull;
import net.hntdstudio.core.Main;
import net.hntdstudio.npc.NpcManager;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.hypixel.hytale.server.core.util.TargetUtil.getTargetEntity;

public class HNpcCommand extends AbstractCommandCollection {
    public HNpcCommand(@NonNull String name, @NonNull String description) {
        super(name, description);
        addAliases("hauntednpc", "hnpc");

        addSubCommand(new HNpcListCommand());
        addSubCommand(new HNpcAddCommand());
    }

    private static class HNpcListCommand extends CommandBase {

        public HNpcListCommand() {
            super("list", "hnpc.command.list.desc");
        }

        @Override
        protected void executeSync(@NonNull CommandContext context) {
            NpcManager npcManager = NpcManager.get();

            if (npcManager.getNpcs().isEmpty()) {
                context.sendMessage(Message.translation("hnpc.command.list.empty"));
                return;
            }

            Set<Message> npcs = npcManager.getNpcs().stream()
                    .map(npc -> Message.raw(npc.getUuid() + " - " + npc.getName()))
                    .collect(Collectors.toSet());

            context.sendMessage(MessageFormat.list(
                    Message.raw("NPCs:"),
                    npcs
            ));
        }
    }

    private static class HNpcAddCommand extends AbstractPlayerCommand {
        private static final float RAYCAST_RADIUS = 3.0f;

        private final RequiredArg<String> nameArg;

        public HNpcAddCommand() {
            super("add", "hnpc.command.add.desc");

            this.nameArg = this.withRequiredArg(
                    "name",
                    "hnpc.command.add.arg.name",
                    ArgTypes.STRING

            );
        }

        @Override
        protected void execute(@NonNull CommandContext commandContext,
                               @NonNull Store<EntityStore> store,
                               @NonNull Ref<EntityStore> ref,
                               @NonNull PlayerRef playerRef,
                               @NonNull World world) {

            Ref<EntityStore> targetEntity = getTargetEntity(ref, RAYCAST_RADIUS, store);
            if (targetEntity == null) {
                commandContext.sendMessage(
                        Message.translation("hnpc.command.add.error.no_target"));
                return;
            }

            UUIDComponent uuidComponent = store.getComponent(targetEntity, UUIDComponent.getComponentType());
            assert uuidComponent != null;

            String npcName = this.nameArg.get(commandContext);
            NpcManager npcManager = NpcManager.get();

            npcManager.registerNpc(uuidComponent.getUuid().toString(), npcName);
            commandContext.sendMessage(
                    Message.translation("hnpc.command.add.success")
                            .param("name", npcName)
            );
        }
    }
}
