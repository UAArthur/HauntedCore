package net.hntdstudio.core;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import lombok.Getter;
import net.hntdstudio.api.events.ListenerRegister;
import net.hntdstudio.core.commands.HNpcCommand;
import net.hntdstudio.npc.NpcManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Main extends JavaPlugin {
    @Getter
    private final HytaleLogger hntdLogger = HytaleLogger.get("HauntedCore");

    //Managers
    private NpcManager npcManager;

    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        //initialize managers
        this.npcManager = new NpcManager();
    }

    @Override
    protected void start() {
        super.start();

        //register listeners
        ListenerRegister registrar = new ListenerRegister(getEventRegistry());

        //register commands
        CommandManager.get().register(new HNpcCommand("HauntedNpc", "Register your NPCs for dialogues"));
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }
}
