package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBehaviour {
    private static final ThreadLocal<Boolean> shadowsgate$insertSuccesfull = ThreadLocal.withInitial(() -> false);

    @Shadow
    private static ItemStack transfer(@Nullable Inventory arg, Inventory arg2, ItemStack arg3, int l, @Nullable Direction arg4) {
        return null;
    }

    @Shadow
    public static ItemStack transfer(@Nullable Inventory arg, Inventory arg2, ItemStack arg3, @Nullable Direction arg4) {
        return null;
    }

    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "HEAD"), cancellable = true)
    private static void shadowsgate$unwrapShadowItems(Inventory sourceInventory, Inventory destination, ItemStack toTransfer, int destinationIndex, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        var destinationStack = destination.getStack(destinationIndex);
        if (destinationStack.getItem() == ShadowsGate.getShadowItem()) {
            // The destination is a shadow item, we will call this method again
            // using the fact that shadow entries can function as pseudo inventories.

            var world = (destination instanceof HopperBlockEntity be) ? be.getWorld() : ShadowsGate.tryGetWorldFromStack(toTransfer);
            var shadowEntry = ShadowItem.getOrCreateEntry(world, destinationStack);

            cir.setReturnValue(transfer(
                    sourceInventory,
                    shadowEntry.fakeInv,
                    toTransfer,
                    0, // The destination index is set to 0 because the destination is now the pseudo inventory, whose only slot is #0
                    direction));
        }
    }

    @Redirect(method = {"insert", "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z", "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack modifyTransferringStack(Inventory sourceInventory, Inventory destination, ItemStack toTransfer, Direction direction) {
        if (toTransfer.getItem() == ShadowsGate.getShadowItem()) {
            // The source is a shadow item, we will call this method again with the shadow item unwrapped

            var world = (destination instanceof HopperBlockEntity be) ? be.getWorld() : ShadowsGate.tryGetWorldFromStack(toTransfer);
            // Retrieve the shadow item data
            var shadowEntry = ShadowItem.getOrCreateEntry(world, toTransfer);
            // Call this method again but using the inner item this time
            // Note: we're splitting the stack here so only one item is transferred at a time. But there are cases where hoppers transfer more. Somehow these cases still work despite that not making any sense.
            var original = shadowEntry.getStack().copy();
            var result = transfer(sourceInventory, destination, shadowEntry.getStack().split(1), direction);

            if (result.isEmpty()) {
                shadowsgate$insertSuccesfull.set(true);
                shadowEntry.markDirty();
            } else {
                shadowEntry.setStack(original);
            }
            // Return the shadow item itself, which should be unaffected
            return toTransfer;
        }
        shadowsgate$insertSuccesfull.set(false);
        return transfer(sourceInventory, destination, toTransfer, direction);
    }

    @Inject(method = "insert", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"), cancellable = true)
    private static void fixInsert(World world, BlockPos blockPos, BlockState blockState, Inventory inventory, CallbackInfoReturnable<Boolean> cir) {
        if (shadowsgate$insertSuccesfull.get()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"), cancellable = true)
    private static void fixExtract(Hopper hopper, Inventory inventory, int i, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (shadowsgate$insertSuccesfull.get()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private static void fixExtract2(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (shadowsgate$insertSuccesfull.get()) {
            cir.setReturnValue(true);
        }
    }
}
