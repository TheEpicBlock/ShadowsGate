package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.ShadowItem;

public class QShadowItem extends ShadowItem implements FabricItem {
    public QShadowItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        var a = getOrCreateEntry(player.getWorld(), oldStack).getStack();
        var b = getOrCreateEntry(player.getWorld(), newStack).getStack();
        if (a.getItem() == b.getItem()) {
            return ((FabricItem)(Object)a).allowContinuingBlockBreaking(player, a, b);
        }
        return false;
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        World world = tryGetWorldFromStack(stack);
        if (world != null) {
            return getOrCreateEntry(world, stack).getStack().isSuitableFor(state);
        }
        return super.isSuitableFor(stack, state);
    }
}
