package nl.theepicblock.shadowsgate.common;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public class ShadowEntry extends PersistentState {
    public static final ShadowEntry MISSING_ENTRY = new ShadowEntry(true);
    private ItemStack stack = ItemStack.EMPTY;
    private final Slot fakeSlot;
    public final ShadowEntryFakeInventory fakeInv;

    // We're employing a simple versioning-ish system here to keep track of dirtyness values per player
    // This is so we don't send useless packets
    public final Object2IntMap<PlayerEntity> dirtynessTracker;
    private int dirtynessValue = 0;

    public ShadowEntry() {
        this(false);
    }

    public ShadowEntry(boolean locked) {
        this.fakeInv = new ShadowEntryFakeInventory(this);
        if (locked) {
            this.fakeSlot = new CustomSlot(fakeInv, 0, 0, 0);
        } else {
            this.fakeSlot = new CustomSlot(fakeInv, 0, 0, 0);
        }
        this.dirtynessTracker = new Object2IntOpenHashMap<>();
        this.dirtynessTracker.defaultReturnValue(-1); // Ensures that new players added to the list are always marked out-of-date
    }

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

    @Override
    public void markDirty() {
        dirtynessValue++;
        super.markDirty();
    }

    /**
     * Checks if a player has a dirty value
     */
    public boolean checkDirt(PlayerEntity player) {
        return dirtynessTracker.getInt(player) < dirtynessValue;
    }

    /**
     * Marks a player as no longer being dirty
     */
    public void resetDirt(PlayerEntity player) {
        dirtynessTracker.put(player, dirtynessValue);
    }

    @ClientOnly
    public static ShadowEntry getEntryClient(int id) {
        var client = MinecraftClient.getInstance();
        if (client.player != null) {
            var entries = ((ClientShadowEntriesDuck)client.player.networkHandler).shadowsgate$getEntries();
            return entries.getOrDefault(id, ShadowEntry.MISSING_ENTRY);
        } else {
            return ShadowEntry.MISSING_ENTRY;
        }
    }

    public static boolean isValidStack(ItemStack stack) {
        var item = stack.getItem();
        return item != ShadowsGate.getShadowItem() && item != Items.BUNDLE;
    }

    public boolean canInsertStack(ItemStack stack) {
        return isValidStack(stack) && (ItemStack.canCombine(this.stack, stack) || this.stack.isEmpty());
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

    public Slot getFakeSlot() {
        return fakeSlot;
    }

    public <T> T executeActiveHand(PlayerEntity player, ItemStack originalShadowItem, Funct<T> r) {
        if (player.getStackInHand(player.getActiveHand()) == originalShadowItem) { // Sanity check
            return execute(player, player.getActiveHand(), r);
        } else {
            return r.run();
        }
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
                var newItem = inv.offHand.get(0);
                inv.offHand.set(0, original);
                if (newItem != this.stack) {
                    this.setStack(newItem);
                    this.markDirty();
                }

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
        var newItem = inv.main.get(inv.selectedSlot);
        inv.main.set(inv.selectedSlot, original);
        if (newItem != this.stack) {
            this.setStack(newItem);
            this.markDirty();
        }

        return v;
    }

    public <T> T execute(PlayerEntity player, int slot, Funct<T> r) {
        var original = player.getInventory().getStack(slot);
        if (!(original.getItem() instanceof ShadowItem)) {
            ShadowsGate.LOGGER.warn("Something fishy is going on "+player+" | "+slot+" | "+original.getTranslationKey());
        }

        player.getInventory().setStack(slot, this.stack);
        var v = r.run();
        var newItem = player.getInventory().getStack(slot);
        player.getInventory().setStack(slot, original);
        if (newItem != this.stack) {
            this.setStack(newItem);
            this.markDirty();
        }

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
