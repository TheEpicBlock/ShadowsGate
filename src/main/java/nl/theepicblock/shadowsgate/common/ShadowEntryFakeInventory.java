package nl.theepicblock.shadowsgate.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import static nl.theepicblock.shadowsgate.common.ShadowEntry.MISSING_ENTRY;

public class ShadowEntryFakeInventory implements Inventory {
    private final ShadowEntry entry;

    public ShadowEntryFakeInventory(ShadowEntry entry) {
        this.entry = entry;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return entry.getStack().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0) {
            return entry.getStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @MarksAsDirty()
    public ItemStack removeStack(int slot, int amount) {
        var stack = entry.getStack();
        if (slot == 0 && !stack.isEmpty() && amount > 0) {
            entry.markDirty();
            return stack.split(amount);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    @MarksAsDirty(becauseOf = "ShadowEntry#setStack")
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            var stack = entry.getStack();
            entry.setStack(ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    @MarksAsDirty(becauseOf = "ShadowEntry#setStack")
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            entry.setStack(stack);
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return ShadowEntry.isValidStack(stack);
    }

    @Override
    public void markDirty() {
        entry.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return entry != MISSING_ENTRY;
    }

    @Override
    public void clear() {

    }
}
