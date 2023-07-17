package nl.theepicblock.shadowsgate.common;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@ClientOnly
public class ShadowsGateClient implements ClientModInitializer {
    @Nullable
    static World getClientWorld() {
        var client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            return client.world;
        }
        return null;
    }

    @Override
    public void onInitializeClient(ModContainer mod) {
        ClientPlayNetworking.registerGlobalReceiver(Networking.SYNC_ENTRY, (client, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var entry = ShadowEntry.read(buf);
            Networking.onSyncPacket(id, entry);
        });
    }
}
