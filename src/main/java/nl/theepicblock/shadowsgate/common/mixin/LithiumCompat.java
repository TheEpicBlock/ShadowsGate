package nl.theepicblock.shadowsgate.common.mixin;

import me.jellysquid.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import nl.theepicblock.shadowsgate.common.ShadowEntry;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = HopperHelper.class, remap = false)
public abstract class LithiumCompat {
    @Shadow
    private static boolean areNbtEqual(ItemStack stack1, ItemStack stack2) {
        return false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static boolean tryMoveSingleItem(
            Inventory to, @Nullable SidedInventory toSided, ItemStack transferStack, int targetSlot, @Nullable Direction fromDirection
    ) {
        /// Shadowsgate
        ShadowEntry transferEntry = null;
        if (transferStack.getItem() == ShadowsGate.getShadowItem()) {
            var world = ShadowsGate.tryGetWorldFromStack(transferStack);
            transferEntry = ShadowItem.getOrCreateEntry(world, transferStack);
            transferStack = transferEntry.getStack();
        }
        ///

        ItemStack toStack = to.getStack(targetSlot);

        /// Shadowsgate
        ShadowEntry toEntry = null;
        if (toStack.getItem() == ShadowsGate.getShadowItem()) {
            var world = ShadowsGate.tryGetWorldFromStack(toStack);
            toEntry = ShadowItem.getOrCreateEntry(world, toStack);
            toStack = toEntry.getStack();
        }
        ///

        if (to.isValid(targetSlot, transferStack) && (toSided == null || toSided.canInsert(targetSlot, transferStack, fromDirection))) {
            if (toStack.isEmpty()) {
                ItemStack singleItem = transferStack.split(1);
                if (toEntry != null) {                                  // Shadowsgate
                    toEntry.setStack(singleItem);                       // Shadowsgate
                } else {                                                // Shadowsgate
                    to.setStack(targetSlot, singleItem);
                }                                                       // Shadowsgate
                return true;
            }

            int toCount;
            if (toStack.isOf(transferStack.getItem())
                    && toStack.getMaxCount() > (toCount = toStack.getCount())
                    && (to.getMaxCountPerStack() > toCount /*(*/ || toEntry != null /*) Shadowsgate */ )
                    && areNbtEqual(toStack, transferStack)) {
                transferStack.decrement(1);
                toStack.increment(1);;
                if (transferEntry != null) transferEntry.markDirty();   // Shadowsgate
                if (toEntry != null) toEntry.markDirty();               // Shadowsgate
                return true;
            }
        }

        return false;
    }
}
