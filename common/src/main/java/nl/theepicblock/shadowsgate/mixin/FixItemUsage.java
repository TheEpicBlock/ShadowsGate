package nl.theepicblock.shadowsgate.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.ShadowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FixItemUsage extends Entity {
    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow public abstract ItemStack getStackInHand(Hand hand);

    @Shadow public abstract Hand getActiveHand();

    public FixItemUsage(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "tickActiveItemStack()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack fixEquality(LivingEntity instance, Hand hand) {
        var handItem = instance.getStackInHand(hand);
        if (handItem.getItem() instanceof ShadowItem) {
            return ShadowItem.getOrCreateEntry(this.getWorld(), handItem).getStack();
        }
        return handItem;
    }

    @Redirect(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack fixEquality2(LivingEntity instance, Hand hand) {
        var handItem = instance.getStackInHand(hand);
        if (handItem.getItem() instanceof ShadowItem) {
            return ShadowItem.getOrCreateEntry(this.getWorld(), handItem).getStack();
        }
        return handItem;
    }

    @Redirect(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"))
    public void fixConsume(LivingEntity instance, Hand hand, ItemStack itemStack) {
        var handItem = instance.getStackInHand(hand);
        if (handItem.getItem() instanceof ShadowItem) {
            ShadowItem.getOrCreateEntry(this.getWorld(), handItem).setStack(itemStack);
        }
        instance.setStackInHand(hand, itemStack);
    }

    @Inject(method = "clearActiveItem", at = @At("HEAD"))
    public void ensureDirty(CallbackInfo ci) {
        var handItem = this.getStackInHand(this.getActiveHand());
        if (handItem.getItem() instanceof ShadowItem) {
            ShadowItem.getOrCreateEntry(this.getWorld(), handItem).markDirty();
        }
    }
}
