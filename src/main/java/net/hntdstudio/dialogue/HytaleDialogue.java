package net.hntdstudio.dialogue;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.hntdstudio.hytale.api.events.ListenerRegister;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HytaleDialogue extends JavaPlugin {

    public HytaleDialogue(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
    }

    @Override
    protected void start() {
        super.start();

        //register listeners
        ListenerRegister registrar = new ListenerRegister(getEventRegistry());
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }
}
