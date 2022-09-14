package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;
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
            return ShadowItem.getOrCreateEntry(ShadowsGate.tryGetWorldFromStack(slotStack), slotStack).getStack();
        }
        return slotStack;
    }
}
