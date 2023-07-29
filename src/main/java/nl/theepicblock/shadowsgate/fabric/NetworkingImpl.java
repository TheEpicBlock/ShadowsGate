package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.Packet;
import nl.theepicblock.shadowsgate.common.Networking;
import nl.theepicblock.shadowsgate.common.ShadowEntry;

public class NetworkingImpl {
    public static Packet<?> createPacket(int id, ShadowEntry entry) {
        var buf = PacketByteBufs.create();
        buf.writeVarInt(id);
        entry.write(buf);
        return ServerPlayNetworking.createS2CPacket(Networking.SYNC_ENTRY_S2C, buf);
    }

    public static Packet<?> createUpdatePacket(int id, ShadowEntry entry) {
        var buf = PacketByteBufs.create();
        buf.writeVarInt(id);
        buf.writeItemStack(entry.getStack());
        return ClientPlayNetworking.createC2SPacket(Networking.UPDATE_ENTRY_C2S, buf);
    }
}
