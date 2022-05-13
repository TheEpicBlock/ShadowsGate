package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nl.theepicblock.shadowsgate.ShadowEntry;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @ModifyVariable(method = {
            "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"
            }, at = @At("HEAD"), argsOnly = true)
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
