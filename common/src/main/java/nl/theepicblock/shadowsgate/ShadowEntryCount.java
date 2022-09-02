package nl.theepicblock.shadowsgate;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

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

    public int getNextId() {
        count += 1;
        this.markDirty();
        return count;
    }
}
