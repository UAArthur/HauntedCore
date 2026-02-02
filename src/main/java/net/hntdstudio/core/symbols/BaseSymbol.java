package net.hntdstudio.core.symbols;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSymbol {
    protected final String assetName;
    protected final float scale;
    protected final float heightOffset;

    protected BaseSymbol(String assetName, float scale, float heightOffset) {
        this.assetName = assetName;
        this.scale = scale;
        this.heightOffset = heightOffset;
    }

    public void spawn(@NotNull World world, @NotNull PlayerRef playerRef, @NotNull Ref<EntityStore> ref,
                      @NotNull Store<EntityStore> store, int networkId) {
        world.execute(() -> {
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(assetName);
            if (modelAsset == null) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Error while trying to load asset '%s' for Entity '%s'", assetName);
                return;
            }

            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) {
                HytaleLogger.forEnclosingClass().atSevere()
                        .log("Failed to add Symbol %s, while trying to add it for Entity '%s'", assetName);
                return;
            }

            Vector3d spawnPosition = calculateSpawnPosition(transformComponent);
            Model model = Model.createScaledModel(modelAsset, scale);

            ComponentUpdate modelUpdate = new ComponentUpdate();
            modelUpdate.type = ComponentUpdateType.Model;
            modelUpdate.model = model.toPacket();

            ComponentUpdate transformUpdate = new ComponentUpdate();
            transformUpdate.type = ComponentUpdateType.Transform;
            transformUpdate.transform = createModelTransform(spawnPosition, transformComponent.getRotation());

            ComponentUpdate newSpawnUpdate = new ComponentUpdate();
            newSpawnUpdate.type = ComponentUpdateType.NewSpawn;

            EntityUpdate entityUpdate = new EntityUpdate();
            entityUpdate.networkId = networkId;
            entityUpdate.updates = new ComponentUpdate[]{
                    newSpawnUpdate,
                    modelUpdate,
                    transformUpdate
            };

            EntityUpdates packet = new EntityUpdates();
            packet.updates = new EntityUpdate[]{entityUpdate};
            playerRef.getPacketHandler().writeNoCache(packet);
        });
    }

    protected Vector3d calculateSpawnPosition(TransformComponent transformComponent) {
        Vector3d position = new Vector3d(transformComponent.getPosition());
        position.y += heightOffset;
        return position;
    }

    protected ModelTransform createModelTransform(Vector3d position, Vector3f rotation) {
        ModelTransform transform = new ModelTransform();

        transform.position = new Position();
        transform.position.x = position.x;
        transform.position.y = position.y;
        transform.position.z = position.z;

        transform.bodyOrientation = new Direction();
        transform.bodyOrientation.yaw = rotation.getYaw();
        transform.bodyOrientation.pitch = rotation.getPitch();
        transform.bodyOrientation.roll = rotation.getRoll();

        transform.lookOrientation = new Direction();
        transform.lookOrientation.yaw = rotation.getYaw();
        transform.lookOrientation.pitch = rotation.getPitch();
        transform.lookOrientation.roll = rotation.getRoll();

        return transform;
    }
}
