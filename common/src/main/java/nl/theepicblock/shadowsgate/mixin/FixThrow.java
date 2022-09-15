package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class FixThrow {
    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onThrowSingleItem(boolean throwAll, CallbackInfoReturnable<ItemStack> cir) {
        if (!throwAll) { // throwing a single item
            var mainHandStack = this.getMainHandStack();
            if (mainHandStack.getItem() == ShadowsGate.getShadowItem()) {
                var entry = ShadowItem.getOrCreateEntry(this.player.world, mainHandStack);
                var toThrow = entry.removeStack(0, 1);
                entry.markDirty();
                cir.setReturnValue(toThrow);
            }
        }
    }
}
