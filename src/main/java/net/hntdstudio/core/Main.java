package net.hntdstudio.core;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.machinima.UpdateMachinimaScene;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NonNull;
import net.hntdstudio.core.commands.HDialogueCommand;
import net.hntdstudio.core.commands.HNpcCommand;
import net.hntdstudio.core.commands.TestCommand;
import net.hntdstudio.core.packets.EntityLookAtPlayerAdapter;
import net.hntdstudio.core.ui.hud.InteractHud;
import net.hntdstudio.dialogue.DialogueManager;
import net.hntdstudio.npc.NpcManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.hypixel.hytale.server.core.util.TargetUtil.getTargetEntity;

public class Main extends JavaPlugin {
    @Getter
    private final HytaleLogger hntdLogger = HytaleLogger.forEnclosingClass();
    private ScheduledFuture<?> checkTask;
    //Managers
    @Getter
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    @Getter
    private SymbolManager symbolManager;
    private NpcManager npcManager;
    private DialogueManager dialogueManager;
    //listener

    public Main(@NonNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        //initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.symbolManager = new SymbolManager(this);
        this.dialogueManager = new DialogueManager(this);
        this.npcManager = new NpcManager(this);
    }

    @Override
    protected void start() {
        super.start();

        //register listeners

        //register commands
        CommandManager.get().register(new HNpcCommand("HauntedNpc", "Register your NPCs for dialogues"));
        CommandManager.get().register(new HDialogueCommand("HDialogue", "Manage dialogues for NPCs"));
        CommandManager.get().register(new TestCommand("TPlayAnimation", "test"));

        this.startCheckingEntitiesInRange(Universe.get().getWorlds());
        EntityLookAtPlayerAdapter entityLookAtPlayerAdapter = new EntityLookAtPlayerAdapter();
//        entityLookAtPlayerAdapter.register();

    }


    @Override
    protected void shutdown() {
        super.shutdown();
        this.stopChecking();
    }

    /// Custom Interact UI
    private void startCheckingEntitiesInRange(@NonNull Map<String, World> worlds) {
        checkTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                for (World world : worlds.values()) {
                    if (world.getPlayerCount() > 0) {
                        world.execute(() -> {
                            try {
                                world.getPlayerRefs().forEach(playerRef -> {
                                    checkEntitiesNearPlayer(world, playerRef, Objects.requireNonNull(playerRef.getReference()));
                                });
                            } catch (Exception e) {
                                HytaleLogger.forEnclosingClass().atSevere()
                                        .log("Error while checking entities near players in world thread: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Error while scheduling entity checks: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    private final Map<String, Ref<EntityStore>> playerTargetedNpc = new ConcurrentHashMap<>();

    private void checkEntitiesNearPlayer(@NonNull World world, @NonNull PlayerRef playerRef,
                                         @NonNull Ref<EntityStore> playerEntityRef) {
        EntityStore entityStore = world.getEntityStore();
        Store<EntityStore> store = entityStore.getStore();

        TransformComponent playerTransform = store.getComponent(
                playerEntityRef,
                TransformComponent.getComponentType()
        );

        if (playerTransform == null) return;
        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) return;

        Vector3d playerPos = playerTransform.getPosition();
        List<Ref<EntityStore>> nearbyNpcs = new ArrayList<>();

        Selector.selectNearbyEntities(
                store,
                playerPos,
                5.0,
                nearbyNpcs::add,
                (entityRef) -> {
                    if (entityRef.equals(playerEntityRef)) return false;

                    return npcManager.getNpcs().stream()
                            .anyMatch(npc -> {
                                Ref<EntityStore> ref = npc.getRef(store);
                                return ref != null && ref.equals(entityRef) &&
                                        player.getPageManager().getCustomPage() == null;
                            });
                }
        );

        for (Ref<EntityStore> npcRef : nearbyNpcs) {
            npcManager.getNpcs().stream()
                    .filter(npc -> {
                        Ref<EntityStore> ref = npc.getRef(store);
                        return ref != null && ref.equals(npcRef);
                    })
                    .findFirst()
                    .ifPresent(npc -> {
                        dialogueManager.ensureDialogueAssigned(playerRef, npc, store, world);
                    });
        }

        if (nearbyNpcs.isEmpty()) {
            hideUIForPlayer(playerRef, playerEntityRef, store);
            return;
        }

        Ref<EntityStore> targetEntity = getTargetEntity(playerEntityRef, 5.0f, store);
        String playerId = playerRef.getUuid().toString();
        Ref<EntityStore> previousTarget = playerTargetedNpc.get(playerId);

        if (targetEntity != null && nearbyNpcs.contains(targetEntity)) {
            if (!targetEntity.equals(previousTarget)) {
                player.getHudManager().setCustomHud(playerRef, new InteractHud(playerRef));
                playerTargetedNpc.put(playerId, targetEntity);
            }
        } else {
            hideUIForPlayer(playerRef, playerEntityRef, store);
        }
    }

    public void hideUIForPlayer(PlayerRef playerRef, Ref<EntityStore> playerEntityRef, Store<EntityStore> store) {
        String playerId = playerRef.getUuid().toString();
        Ref<EntityStore> previousTarget = playerTargetedNpc.remove(playerId);

        if (previousTarget != null) {
            Player player = store.getComponent(playerEntityRef, Player.getComponentType());
            if (player != null) {
                player.getHudManager().setCustomHud(playerRef, new CustomUIHud(playerRef) {
                    @Override
                    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {

                    }
                });
            }
        }
    }

    public void stopChecking() {
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel(false);
        }
    }

}
