package net.hntdstudio.core.ui.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public class InteractHud extends CustomUIHud {
    public InteractHud(@NotNull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/showInteract.ui");
    }
}
