package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerInventory.class)
public class InventoryPickup {
    @ModifyVariable(method = "canStackAddMore", at = @At(value = "HEAD"), index = 1, argsOnly = true)
    private ItemStack makeShadowStackQualifyAsStackableWithOtherThings(ItemStack slotStack) {
        if (slotStack.getItem() == ShadowsGate.getShadowItem()) {
            return ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(slotStack), slotStack).getStack();
        }
        return slotStack;
    }

    @ModifyVariable(
            method = "addStack(ILnet/minecraft/item/ItemStack;)I",
            at = @At(value = "STORE"),
            ordinal = 1)
    private ItemStack shadowStackStacking(ItemStack slotStack) {
        if (slotStack.getItem() == ShadowsGate.getShadowItem()) {
            var entry = ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(slotStack), slotStack);
            entry.markDirty(); // If we got to this point we already know that the stack is going to get changed
            return entry.getStack();
        }
        return slotStack;
    }
}
