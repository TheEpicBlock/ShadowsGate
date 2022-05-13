package nl.theepicblock.shadowsgate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class ShadowEntry extends PersistentState {
    public static final ShadowEntry MISSING_ENTRY = new ShadowEntry();
    private ItemStack stack = ItemStack.EMPTY;

    public static ShadowEntry fromNbt(NbtCompound nbt) {
        var entry = new ShadowEntry();

        if (nbt.contains("stack", NbtElement.COMPOUND_TYPE)) {
            entry.stack = ItemStack.fromNbt(nbt.getCompound("stack"));
        }

        return entry;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var innerNbt = new NbtCompound();
        stack.writeNbt(innerNbt);
        nbt.put("stack", innerNbt);
        return nbt;
    }

    public void write(PacketByteBuf buf) {
        buf.writeItemStack(this.stack);
    }

    public static ShadowEntry read(PacketByteBuf buf) {
        var entry = new ShadowEntry();
        entry.stack = buf.readItemStack();
        return entry;
    }

    @Environment(EnvType.CLIENT)
    public static ShadowEntry getEntryClient(int id) {
        var client = MinecraftClient.getInstance();
        if (client.player != null) {
            var entries = ((ClientShadowEntriesDuck)client.player.networkHandler).shadowsgate$getEntries();
            return entries.getOrDefault(id, ShadowEntry.MISSING_ENTRY);
        } else {
            return ShadowEntry.MISSING_ENTRY;
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        this.markDirty();
    }

    public boolean isUninitialized() {
        return this.stack.isEmpty();
    }

    public <T> T execute(PlayerEntity player, Hand hand, Funct<T> r) {
        return switch (hand) {
            case MAIN_HAND -> execute(player, r);
            case OFF_HAND -> {
                var inv = player.getInventory();

                var original = inv.offHand.get(0);
                if (!(original.getItem() instanceof ShadowItem)) {
                    ShadowsGate.LOGGER.warn("Something fishy is going on "+player+" | "+original.getTranslationKey());
                }

                inv.offHand.set(0, this.stack);
                var ret = r.run();
                inv.offHand.set(0, original);
                this.markDirty();

                yield ret;
            }
        };
    }

    public <T> T execute(PlayerEntity player, Funct<T> r) {
        var inv = player.getInventory();
        if (!PlayerInventory.isValidHotbarIndex(inv.selectedSlot)) {
            return r.run();
        }

        var original = inv.main.get(inv.selectedSlot);
        if (!(original.getItem() instanceof ShadowItem)) {
            ShadowsGate.LOGGER.warn("Something fishy is going on "+player+" | "+original.getTranslationKey());
        }

        inv.main.set(inv.selectedSlot, this.stack);
        var v = r.run();
        inv.main.set(inv.selectedSlot, original);
        this.markDirty();

        return v;
    }

    public <T> T execute(PlayerEntity player, int slot, Funct<T> r) {
        var original = player.getInventory().getStack(slot);
        if (!(original.getItem() instanceof ShadowItem)) {
            ShadowsGate.LOGGER.warn("Something fishy is going on "+player+" | "+slot+" | "+original.getTranslationKey());
        }

        player.getInventory().setStack(slot, this.stack);
        var v = r.run();
        player.getInventory().setStack(slot, original);
        this.markDirty();

        return v;
    }

    public static ShadowEntry get(PersistentStateManager manager, int id) {
        var name = "shadowsgate_entry_"+id;
        return manager.getOrCreate(ShadowEntry::fromNbt, ShadowEntry::new, name);
    }

    @FunctionalInterface
    public interface Funct<T> {
        T run();
    }
}
