package nl.theepicblock.shadowsgate.common;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import nl.theepicblock.shadowsgate.fabric.NetworkingImpl;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public class Networking {
    public static final String PROTOCOL_VERSION = "1";
    public static final Identifier SYNC_ENTRY_S2C = new Identifier(ShadowsGate.MOD_ID, "sync_entry");
    /**
     * Allows the client to override the entry's contents with anything. Therefore, it's creative-only
     */
    public static final Identifier UPDATE_ENTRY_C2S = new Identifier(ShadowsGate.MOD_ID, "update_entry");

    /**
     * This is now done directly in the packet handler
     */
    @ClientOnly
    public static void onSyncPacket(int id, ShadowEntry entry) {
        var client = MinecraftClient.getInstance();
        if (client.player != null) {
            var entries = ((ClientShadowEntriesDuck)client.player.networkHandler).shadowsgate$getEntries();
            entries.put(id, entry);
        }
    }

    public static Packet<?> createPacket(int id, ShadowEntry entry) {
        return NetworkingImpl.createPacket(id, entry);
    }
}
