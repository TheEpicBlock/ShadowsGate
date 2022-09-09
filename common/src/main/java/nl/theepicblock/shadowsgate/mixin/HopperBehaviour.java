package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBehaviour {
    @Shadow
    private static ItemStack transfer(@Nullable Inventory arg, Inventory arg2, ItemStack arg3, int l, @Nullable Direction arg4) {
        return null;
    }

    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "HEAD"), cancellable = true)
    private static void shadowsgate$unwrapShadowItems(Inventory sourceInventory, Inventory destination, ItemStack toTransfer, int destinationIndex, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (toTransfer.getItem() == ShadowsGate.getShadowItem()) {
            // The source is a shadow item, we will call this method again with the shadow item unwrapped

            var world = (destination instanceof HopperBlockEntity be) ? be.getWorld() : ShadowsGate.tryGetWorldFromStack(toTransfer);
            // Retrieve the shadow item data
            var shadowEntry = ShadowItem.getOrCreateEntry(world, toTransfer);

            // Call this method again but using the inner item this time
            // Note: we're splitting the stack here so only one item is transferred at a time. But there are cases where hoppers transfer more. Somehow these cases still work despite that not making any sense.
            var result = transfer(sourceInventory, destination, shadowEntry.getStack().split(1), destinationIndex, direction);

            // Put the result back into the shadow item
            shadowEntry.setStack(result);
            // Return the shadow item itself, which should be unaffected
            cir.setReturnValue(toTransfer);
        }
        var destinationStack = destination.getStack(destinationIndex);
        if (destinationStack.getItem() == ShadowsGate.getShadowItem()) {
            // The destination is a shadow item, we will call this method again
            // using the fact that shadow entries can function as pseudo inventories.

            var world = (destination instanceof HopperBlockEntity be) ? be.getWorld() : ShadowsGate.tryGetWorldFromStack(toTransfer);
            var shadowEntry = ShadowItem.getOrCreateEntry(world, destinationStack);

            cir.setReturnValue(transfer(
                    sourceInventory,
                    shadowEntry,
                    toTransfer,
                    0, // The destination index is set to 0 because the destination is now the pseudo inventory, whose only slot is #0
                    direction));
        }
    }
}
