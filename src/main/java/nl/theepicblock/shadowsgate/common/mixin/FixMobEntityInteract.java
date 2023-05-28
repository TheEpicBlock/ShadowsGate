package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobEntity.class, priority = 995)
public abstract class FixMobEntityInteract {
    @Shadow public abstract ActionResult interact(PlayerEntity player, Hand hand);

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onStartInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        var stack = player.getStackInHand(hand);
        if (stack.getItem() == ShadowsGate.getShadowItem()) {
            var entry = ShadowItem.getOrCreateEntry(player.getWorld(), stack);
            cir.setReturnValue(entry.execute(player, hand, () -> this.interact(player, hand)));
            cir.cancel();
        }
    }
}
