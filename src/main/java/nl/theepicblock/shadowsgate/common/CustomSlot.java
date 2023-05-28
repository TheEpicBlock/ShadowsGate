package nl.theepicblock.shadowsgate.common;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CustomSlot extends Slot {
    public CustomSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return ShadowEntry.isValidStack(stack);
    }
}
