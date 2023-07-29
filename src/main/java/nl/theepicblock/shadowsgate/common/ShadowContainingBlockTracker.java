package nl.theepicblock.shadowsgate.common;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.mixin.DoubleInventoryAccessor;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * Tracks block positions that contain inventories with shadow items
 */
public class ShadowContainingBlockTracker {
    /**
     * Hack because I can't add variables to these random calls
     * @see nl.theepicblock.shadowsgate.common.mixin.FixComparator
     * @see nl.theepicblock.shadowsgate.common.mixin.FixComparatorLithium
     */
    public static List<ShadowEntry> ShadowEntriesUsedInComparatorCalculation = new ArrayList<>();
    private ShadowEntry parent;
    private Set<BlockPos> mainSet = new HashSet<>();
    private Set<BlockPos> backupSet = new HashSet<>();

    public ShadowContainingBlockTracker(ShadowEntry parent) {
        this.parent = parent;
    }

    public void updateBlocks(World world) {
        if (!Objects.equals(mainSet, backupSet)) {
            parent.markForSaving();
        }

        // Avoiding comodification
        var set = mainSet;
        this.mainSet = backupSet;
        this.backupSet = set;

        // If the blocks are still depending on the shadowentry, they'll get re-added
        this.mainSet.clear();

        for (BlockPos pos : set) {
            var state = world.getBlockState(pos);
            if (!state.hasBlockEntity()) {
                continue;
            }
            if (ShadowsGate.LITHIUM) {
                var be = world.getBlockEntity(pos);
                if (be == null) {
                    continue;
                }
                LithiumCompat.updateBE(be);
            }
            world.updateComparators(pos, world.getBlockState(pos).getBlock());
        }
    }

    public void add(BlockPos pos) {
        this.mainSet.add(pos);
    }

    /**
     * To be used only for debugging!
     */
    @ApiStatus.Internal
    public Collection<BlockPos> getPositions() {
        return mainSet;
    }

    public void fromNbt(NbtElement e) {
        if (e.getType() != NbtElement.INT_ARRAY_TYPE) {
            ShadowsGate.LOGGER.error("Wrong nbt type "+e.getType()+" whilst loading block tracker");
            return;
        }

        var array = ((NbtIntArray)e).getIntArray();
        if (array.length % 3 != 0) {
            ShadowsGate.LOGGER.error("Invalid length "+array.length+" for pos array. Must be multiple of 3");
        }

        for (int i = 0; i < array.length; i += 3) {
            var pos = new BlockPos(array[i], array[i+1], array[i+2]);
            mainSet.add(pos);
            backupSet.add(pos);
        }
    }

    public NbtElement toNbt() {
        var positionsArray = new int[mainSet.size() * 3];

        int i = 0;
        for (var pos : mainSet) {
            positionsArray[i] = pos.getX();
            positionsArray[i+1] = pos.getY();
            positionsArray[i+2] = pos.getZ();
            i += 3;
        }

        return new NbtIntArray(positionsArray);
    }

    public static void onComparatorCalcFinish(Inventory inventory) {
        var entries = ShadowEntriesUsedInComparatorCalculation;
        if (inventory instanceof BlockEntity be) {
            onComparatorCalcFinish(be);
        } else if (inventory instanceof DoubleInventory doubleInventory) {
            if (((DoubleInventoryAccessor)doubleInventory).getFirst() instanceof BlockEntity be) {
                onComparatorCalcFinish(be);
            }
            if (((DoubleInventoryAccessor)doubleInventory).getSecond() instanceof BlockEntity be) {
                onComparatorCalcFinish(be);
            }
        }
        entries.clear();
    }

    public static void onComparatorCalcFinish(BlockEntity be) {
        ShadowEntriesUsedInComparatorCalculation.forEach(entry -> {
            var world = be.getWorld();
            if (world != null) {
                entry.startTrackingBlockPos(world, be.getPos());
            }
        });
    }
}
