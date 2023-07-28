package nl.theepicblock.shadowsgate.common.mixin;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LithiumStackList.class, remap = false)
public class FixComparatorLithium {
    @ModifyVariable(method = "calculateSignalStrength(I)I", at = @At(value = "STORE"))
    private ItemStack getItemStack(ItemStack instance) {
        if (instance.getItem() == ShadowsGate.getShadowItem()) {
            // Use the inner item instead of the shadow item for comparator calculations
            return ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(instance), instance).getStack();
        }
        return instance;
    }
}
