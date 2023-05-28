package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @ModifyVariable(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"), argsOnly = true)
    private ItemStack modifyStack(ItemStack stack) {
        if (stack.getItem() == ShadowsGate.getShadowItem()) {
            var entry = ShadowItem.getClientEntry(stack);
            if (entry.isUninitialized()) {
                return new ItemStack(Items.BARRIER);
            }
            return entry.getStack();
        }
        return stack;
    }
}
