package nl.theepicblock.shadowsgate.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import nl.theepicblock.shadowsgate.ClientShadowEntriesDuck;
import nl.theepicblock.shadowsgate.ShadowEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler implements ClientShadowEntriesDuck {
    // Yes, this is in fact a very weird place to put this, thanks for noticing
    @Unique
    private final Int2ObjectMap<ShadowEntry> entries = new Int2ObjectOpenHashMap<>();

    @Override
    public Int2ObjectMap<ShadowEntry> shadowsgate$getEntries() {
        return entries;
    }
}
