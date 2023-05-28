package nl.theepicblock.shadowsgate.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor("holder")
    public Entity shadowsgate$getHolder();
}
