package nl.theepicblock.shadowsgate.forge;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import nl.theepicblock.shadowsgate.Networking;
import nl.theepicblock.shadowsgate.ShadowEntry;

import java.util.Optional;

public class NetworkingImpl {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Networking.SYNC_ENTRY,
            () -> Networking.PROTOCOL_VERSION,
            // TODO it'd be cool if we can connect to fabric from forge or the other way around
            Networking.PROTOCOL_VERSION::equals,
            Networking.PROTOCOL_VERSION::equals
    );

    public static Packet<?> createPacket(int id, ShadowEntry entry) {
        return INSTANCE.toVanillaPacket(new EntrySyncPacket(id, entry), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void init() {
        INSTANCE.registerMessage(0, EntrySyncPacket.class, EntrySyncPacket::write, EntrySyncPacket::read, (packet, contextSupplier) -> {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Networking.onSyncPacket(packet.id, packet.entry));
            });
            contextSupplier.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public record EntrySyncPacket(int id, ShadowEntry entry) {
        void write(PacketByteBuf buf) {
            buf.writeVarInt(id);
            entry.write(buf);
        }

        static EntrySyncPacket read(PacketByteBuf buf) {
            var id = buf.readVarInt();
            var entry = ShadowEntry.read(buf);
            return new EntrySyncPacket(id, entry);
        }
    }
}
