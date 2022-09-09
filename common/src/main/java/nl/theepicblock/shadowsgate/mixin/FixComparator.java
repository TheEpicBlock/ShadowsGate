package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ScreenHandler.class)
public class FixComparator {
    @ModifyVariable(method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I", at = @At(value = "STORE"))
    private static ItemStack getItemStack(ItemStack instance) {
        if (instance.getItem() == ShadowsGate.getShadowItem()) {
            // Use the inner item instead of the shadow item for comparator calculations
            return ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(instance), instance).getStack();
        }
        return instance;
    }
}
