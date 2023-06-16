package nl.theepicblock.shadowsgate.common.mixin;

import com.mojang.blaze3d.lighting.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiRenderMixin {
    @Shadow @Final private MatrixStack matrices;
    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract VertexConsumerProvider.Immediate getVertexConsumers();

    @Shadow public abstract void draw();

    @ModifyVariable(method = {
            "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
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

    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;isSideLit()Z"))
    private void doRenderin(LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        if (stack.getItem() == ShadowsGate.getShadowItem()) {
            ShadowsGate.AAAAAAA = true;
            DiffuseLighting.setupFlatGuiLighting();
            var bakedModel = this.client.getItemRenderer().getHeldItemModel(new ItemStack(ShadowsGate.getShadowItem()), null, null, 0);
            this.client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, this.matrices, this.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
            this.draw();
            ShadowsGate.AAAAAAA = false;
            DiffuseLighting.setup3DGuiLighting();
        }
    }
}
