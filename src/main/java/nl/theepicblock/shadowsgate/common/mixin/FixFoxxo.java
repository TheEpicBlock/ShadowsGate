package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoxEntity.class)
public abstract class FixFoxxo extends AnimalEntity {
    protected FixFoxxo(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "canEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private Item redirectItem(ItemStack instance) {
        var item = instance.getItem();
        if (item instanceof ShadowItem) {
            return ShadowItem.getOrCreateEntry(this.getWorld(), instance).getStack().getItem();
        }
        return item;
    }
}
