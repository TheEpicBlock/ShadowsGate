package nl.theepicblock.shadowsgate.common.mixin;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.common.ShadowContainingBlockTracker;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LithiumStackList.class, remap = false)
public class FixComparatorLithium {
    @ModifyVariable(method = "calculateSignalStrength(I)I", at = @At(value = "STORE"))
    private ItemStack getItemStack(ItemStack instance) {
        if (instance.getItem() == ShadowsGate.getShadowItem()) {
            // Use the inner item instead of the shadow item for comparator calculations
            var entry = ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(instance), instance);
            ShadowContainingBlockTracker.ShadowEntriesUsedInComparatorCalculation.add(entry);
            return entry.getStack();
        }
        return instance;
    }

    @Inject(method = "getSignalStrength(Lnet/minecraft/inventory/Inventory;)I", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/lithium/common/hopper/LithiumStackList;calculateSignalStrength(I)I", shift = At.Shift.AFTER))
    private void trackEntriesInBlock(Inventory inventory, CallbackInfoReturnable<Integer> cir) {
        ShadowContainingBlockTracker.onComparatorCalcFinish(inventory);
    }
}
