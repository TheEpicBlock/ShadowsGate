package nl.theepicblock.shadowsgate.forge;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeItem;
import nl.theepicblock.shadowsgate.ShadowItem;
import org.jetbrains.annotations.Nullable;

public class FShadowItem extends ShadowItem implements IForgeItem {
    public FShadowItem(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public FoodComponent getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        if (entity != null) {
            return getOrCreateEntry(entity.getWorld(), stack).getStack().getFoodProperties(entity);
        }
        return super.getFoodProperties(stack, entity);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        return getOrCreateEntry(player.getWorld(), itemstack).getStack().onBlockStartBreak(pos, player);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        getOrCreateEntry(player.getWorld(), stack).getStack().onUsingTick(player, count);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return getOrCreateEntry(entity.getWorld(), stack).getStack().canDisableShield(shield, entity, attacker);
    }
}
