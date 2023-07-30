package nl.theepicblock.shadowsgate.common.mixin;

import me.jellysquid.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import nl.theepicblock.shadowsgate.common.ShadowEntry;
import nl.theepicblock.shadowsgate.common.ShadowEntryFakeInventory;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HopperHelper.class)
public abstract class LithiumHopperBehaviour {
    @Unique
    private static final ThreadLocal<ShadowEntry> transferEntry = new ThreadLocal<>();

    @Shadow
    private static boolean areNbtEqual(ItemStack stack1, ItemStack stack2) {
        return false;
    }

    @ModifyVariable(method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), argsOnly = true)
    private static ItemStack modifyTransferringStack(ItemStack transferStack) {
        transferEntry.set(null);
        if (transferStack.getItem() == ShadowsGate.getShadowItem()) {
            var world = ShadowsGate.tryGetWorldFromStack(transferStack);
            var entry = ShadowItem.getOrCreateEntry(world, transferStack);
            transferEntry.set(entry);
            return entry.getStack();
        }
        return transferStack;
    }

    @ModifyVariable(method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), argsOnly = true)
    private static Inventory modifyDestination(Inventory to, Inventory to2, @Nullable SidedInventory toSided, ItemStack transferStack, int targetSlot, @Nullable Direction fromDirection) {
        var toStack = to.getStack(targetSlot);
        if (toStack.getItem() == ShadowsGate.getShadowItem()) {
            var world = ShadowsGate.tryGetWorldFromStack(toStack);
            var entry = ShadowItem.getOrCreateEntry(world, toStack);
            return entry.fakeInv;
        }
        return to;
    }

    @ModifyVariable(method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), argsOnly = true)
    private static SidedInventory removeSidedInv(SidedInventory toSided, Inventory to, @Nullable SidedInventory toSided2, ItemStack transferStack, int targetSlot, @Nullable Direction fromDirection) {
        if (to instanceof ShadowEntryFakeInventory) {
            // This inventory is still the old one
            // Shadow entries aren't sided anyway, so we can just yeet it so it doesn't cause issues
            return null;
        }
        return toSided;
    }

    @ModifyVariable(method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), argsOnly = true)
    private static int setSlot(int targetSlot, Inventory to, @Nullable SidedInventory toSided, ItemStack transferStack, int targetSlot2, @Nullable Direction fromDirection) {
        if (to instanceof ShadowEntryFakeInventory) {
            // Shadow inventories only have one slot
            return 0;
        }
        return targetSlot;
    }

    @SuppressWarnings("PointlessBooleanExpression")
    @Inject(method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z", at = @At("RETURN"))
    private static void markDirty(Inventory to, SidedInventory toSided, ItemStack transferStack, int targetSlot, Direction fromDirection, CallbackInfoReturnable<Boolean> cir) {
        // Check if transfer was a success
        if (cir.getReturnValueZ() == true) {
            // Check of the transferred stack was a shadow item
            // and mark it as dirty
            var transferEntry0 = transferEntry.get();
            if (transferEntry0 != null) {
                transferEntry0.markDirty();
            }

            // Mark the destination as dirty as well (if it was a shadow item)
            if (to instanceof ShadowEntryFakeInventory) {
                to.markDirty();
            }
        }
    }
}
