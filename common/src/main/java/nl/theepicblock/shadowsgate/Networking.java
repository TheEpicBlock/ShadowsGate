package nl.theepicblock.shadowsgate;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

public class Networking {
    public static final String PROTOCOL_VERSION = "1";
    public static final Identifier SYNC_ENTRY = new Identifier(ShadowsGate.MOD_ID, "sync_entry");

    /**
     * @see ShadowEntry#getEntryClient(int)
     */
    @Environment(EnvType.CLIENT)
    public static void onSyncPacket(int id, ShadowEntry entry) {
        var client = MinecraftClient.getInstance();
        if (client.player != null) {
            var entries = ((ClientShadowEntriesDuck)client.player.networkHandler).shadowsgate$getEntries();
            entries.put(id, entry);
        }
    }

    @ExpectPlatform
    public static Packet<?> createPacket(int id, ShadowEntry entry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
