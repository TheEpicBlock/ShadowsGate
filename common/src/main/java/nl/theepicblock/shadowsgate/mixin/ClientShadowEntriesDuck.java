package nl.theepicblock.shadowsgate.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import nl.theepicblock.shadowsgate.ShadowEntry;

public interface ClientShadowEntriesDuck {
    Int2ObjectMap<ShadowEntry> shadowsgate$getEntries();
}
