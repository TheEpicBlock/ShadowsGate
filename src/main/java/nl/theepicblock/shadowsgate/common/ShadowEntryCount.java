package nl.theepicblock.shadowsgate.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.ApiStatus;

public class ShadowEntryCount extends PersistentState {
    private int count;

    public static ShadowEntryCount fromNbt(NbtCompound nbt) {
        var count = new ShadowEntryCount();

        if (nbt.contains("count", NbtElement.NUMBER_TYPE)) {
            count.count = nbt.getInt("count");
        }

        return count;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("count", count);
        return nbt;
    }

    public static ShadowEntryCount get(PersistentStateManager manager) {
        return manager.getOrCreate(ShadowEntryCount::fromNbt, ShadowEntryCount::new, "shadowsgate_entrycount");
    }

    /**
     * Used only for debugging!
     */
    @ApiStatus.Internal
    public int getCount() {
        return count;
    }

    public int getNextId() {
        count += 1;
        this.markDirty();
        return count;
    }
}
