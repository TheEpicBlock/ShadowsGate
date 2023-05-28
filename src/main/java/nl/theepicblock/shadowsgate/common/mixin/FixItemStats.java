package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemStack.class)
public abstract class FixItemStats {
    @Shadow @Nullable public abstract Entity getHolder();

    @ModifyArg(method = {"postHit", "postMine"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/StatType;getOrCreateStat(Ljava/lang/Object;)Lnet/minecraft/stat/Stat;"))
    private Object modifyStack(Object original) {
        if (original instanceof ShadowItem && this.getHolder() != null) {
            return ShadowItem.getOrCreateEntry(this.getHolder().getWorld(), (ItemStack)(Object)this).getStack();
        }
        return original;
    }
}
