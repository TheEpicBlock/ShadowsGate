package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerInventory.class)
public class PatchRemoveOne {
    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "removeOne", at = @At("HEAD"))
    public void removeFromInsideShadowItem(ItemStack itemStack, CallbackInfo ci) {
        for (var inv : this.combinedInventory) {
            for (var stack : inv) {
                if (stack.getItem() instanceof ShadowItem) {
                    var entry = ShadowItem.getOrCreateEntry(this.player.getWorld(), stack);
                    if (entry.getStack() == itemStack) {
                        entry.setStack(ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
