package nl.theepicblock.shadowsgate.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;

@Mixin(GuiGraphics.class)
public class GuiRenderMixin {
    @ModifyVariable(method = {
        "Lnet/minecraft/client/gui/GuiGraphics;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
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
