package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class FixProjectiles extends LivingEntity {
    protected FixProjectiles(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "getArrowType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectile(Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/item/ItemStack;"))
    public Predicate<ItemStack> unwrapShadowItemPredicate(Predicate<ItemStack> predicate) {
        // Modifies the call for held projectiles
        return stack -> {
            if (stack.getItem() instanceof ShadowItem) {
                return predicate.test(ShadowItem.getOrCreateEntry(this.getWorld(), stack).getStack());
            } else {
                return predicate.test(stack);
            }
        };
    }

    @ModifyArg(method = "getArrowType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    public <T> T unwrapShadowItems(T stack) {
        // Modifies the call for other projectiles
        if (((ItemStack)stack).getItem() instanceof ShadowItem) {
            return (T)ShadowItem.getOrCreateEntry(this.getWorld(), ((ItemStack)stack)).getStack();
        }
        return stack;
    }

    @Inject(method = "getArrowType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    public void onReturn(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        var retStack = cir.getReturnValue();
        if (retStack.getItem() == ShadowsGate.getShadowItem()) {
            World world = null;
            if (itemStack.getHolder() != null) world = ((ItemStackAccessor)(Object)itemStack).shadowsgate$getHolder().getWorld();
            if (retStack.getHolder() != null) world = ((ItemStackAccessor)(Object)retStack).shadowsgate$getHolder().getWorld();
            if (world == null) world = ShadowsGate.getGlobalWorld(); // Try to fall back on the global world

            if (world != null) {
                cir.setReturnValue(ShadowItem.getOrCreateEntry(world, retStack).getStack());
            } else {
                cir.setReturnValue(null);
            }
        }
    }
}
