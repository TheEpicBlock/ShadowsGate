package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import nl.theepicblock.shadowsgate.common.ShadowContainingBlockTracker;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class FixComparator {
    @ModifyVariable(method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I", at = @At(value = "STORE"))
    private static ItemStack getItemStack(ItemStack instance) {
        if (instance.getItem() == ShadowsGate.getShadowItem()) {
            // Use the inner item instead of the shadow item for comparator calculations
            var world = ShadowsGate.tryGetWorldFromStack(instance);
            if (world != null) {
                var entry = ShadowItem.getEntry(world, instance);
                if (entry != null) {
                    ShadowContainingBlockTracker.ShadowEntriesUsedInComparatorCalculation.add(entry);
                    return entry.getStack();
                }
            }
        }
        return instance;
    }

    @Inject(method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I", at = @At("RETURN"))
    private static void trackEntriesInBlock(Inventory inventory, CallbackInfoReturnable<Integer> cir) {
        ShadowContainingBlockTracker.onComparatorCalcFinish(inventory);
    }
}
