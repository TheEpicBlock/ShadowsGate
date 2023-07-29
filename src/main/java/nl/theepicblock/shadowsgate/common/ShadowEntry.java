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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.HashMap;
import java.util.Map;

public class ShadowEntry extends PersistentState {
    public static final ShadowEntry MISSING_ENTRY = new ShadowEntry(true);
    private ItemStack stack = ItemStack.EMPTY;
    /**
     * Tracks blocks that contain shadow items, so they can receive an update every time the block updates.
     * This means that, for example, comparators are properly updated when the shadow stack fills up
     */
    private final Map<RegistryKey<World>, ShadowContainingBlockTracker> usedPositions;

    // These are cached so we don't have to instantiate them each time
    private final Slot fakeSlot;
    public final ShadowEntryFakeInventory fakeInv;

    // We're employing a simple versioning-ish system here to keep track of dirtyness values per player
    // This is so we don't send useless packets
    public final Object2IntMap<PlayerEntity> dirtynessTracker;
    private int dirtynessValue = 0;
    /**
     * Tracks if any blocks containing shadow items still need to be updated.
     */
    private boolean worldDirty;

    public ShadowEntry() {
        this(false);
    }

    public ShadowEntry(boolean locked) {
        this.usedPositions = new HashMap<>();
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

        if (nbt.contains("usedPositions", NbtElement.COMPOUND_TYPE)) {
            var server = ShadowsGate.getGlobalServer();
            var usedPositionsMap = nbt.getCompound("usedPositions");
            for (String dimensionKey : usedPositionsMap.getKeys()) {
                var dimensionId = Identifier.tryParse(dimensionKey);
                if (dimensionId == null) {
                    ShadowsGate.LOGGER.error("Found invalid identifier for dimension: '" + dimensionKey + "'");
                    entry.markForSaving();
                    continue;
                }
                var registryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);

                if (server != null && server.getWorld(registryKey) == null) {
                    ShadowsGate.LOGGER.warn("Dimension "+dimensionKey+" doesn't appear to exist, skipping these entries");
                    entry.markForSaving();
                    continue;
                }

                var tracker = entry.usedPositions.computeIfAbsent(registryKey, k -> new ShadowContainingBlockTracker(entry));
                tracker.fromNbt(usedPositionsMap.get(dimensionKey));
            }
        }

        return entry;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var innerNbt = new NbtCompound();
        stack.writeNbt(innerNbt);
        nbt.put("stack", innerNbt);

        var usedPositionsMap = new NbtCompound();
        for (var entry : this.usedPositions.entrySet()) {
            var dimension = entry.getKey();
            var positions = entry.getValue();

            usedPositionsMap.put(dimension.getValue().toString(), positions.toNbt());
        }
        nbt.put("usedPositions", usedPositionsMap);
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
        this.markDirty(true);
    }

    public void markDirty(boolean countChanged) {
        this.dirtynessValue++;
        if (countChanged) {
            this.worldDirty = true;
        }
        super.markDirty();
    }

    public void markForSaving() {
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

    /**
     * Marks a blockpos as containing a shadow item
     */
    public void startTrackingBlockPos(World world, BlockPos pos) {
        if (!world.isClient)
            this.usedPositions.computeIfAbsent(world.getRegistryKey(), k -> new ShadowContainingBlockTracker(this)).add(pos);
    }

    public void updateWorld(MinecraftServer server) {
        if (!this.worldDirty) return;

        this.usedPositions.forEach((dimension, positions) -> {
            var world = server.getWorld(dimension);
            if (world == null) {
                ShadowsGate.LOGGER.error("Tried updating invalid dimension "+dimension);
                return;
            }

            positions.updateBlocks(world);
        });
        this.worldDirty = false;
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
        if (!ItemStack.areEqual(this.stack, stack)) {
            this.markDirty(this.stack.getCount() != stack.getCount());
        }
        this.stack = stack;
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
        }

        return v;
    }

    public static ShadowEntry get(MinecraftServer server, int id) {
        return get(server.getOverworld().getPersistentStateManager(), id);
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
