package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.Networking;
import nl.theepicblock.shadowsgate.common.ShadowEntry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class ShadowsGateClient implements ClientModInitializer {
    @Nullable
    public static World getClientWorld() {
        var client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            return client.world;
        }
        return null;
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Networking.SYNC_ENTRY, (client, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var entry = ShadowEntry.read(buf);
            Networking.onSyncPacket(id, entry);
        });
    }
}
