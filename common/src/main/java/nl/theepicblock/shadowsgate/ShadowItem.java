package nl.theepicblock.shadowsgate;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.mixin.ClientShadowEntriesDuck;
import nl.theepicblock.shadowsgate.mixin.ItemUsageContextAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShadowItem extends NetworkSyncedItem {
    public ShadowItem(Settings settings) {
        super(settings);
    }

    static int getIndex(ItemStack stack) {
        var nbt = stack.getNbt();
        if (nbt == null) return -1;
        if (!nbt.contains("shadowindex", NbtElement.NUMBER_TYPE)) return -1;
        return nbt.getInt("shadowindex");
    }

    static ShadowEntry getOrCreateEntry(World world, ItemStack stack) {
        Objects.requireNonNull(world);
        var index = getIndex(stack);
        if (world instanceof ServerWorld serverWorld) {
            if (index == -1) {
                index = ShadowEntryCount.get(serverWorld.getServer().getOverworld().getPersistentStateManager()).getNextId();
                stack.getOrCreateNbt().putInt("shadowindex", index);
            }

            return ShadowEntry.get(serverWorld.getServer().getOverworld().getPersistentStateManager(), index);
        } else if (world.isClient) {
            return ShadowEntry.getEntryClient(index);
        }

        return ShadowEntry.MISSING_ENTRY; // TODO client shit
    }

    @Nullable
    @Override
    public Packet<?> createSyncPacket(ItemStack stack, World world, PlayerEntity player) {
        var index = getIndex(stack);
        if (index == -1) {
            return null;
        }
        if (world instanceof ServerWorld serverWorld) {
            var entry = ShadowEntry.get(serverWorld.getServer().getOverworld().getPersistentStateManager(), index);
            return Networking.createPacket(index, entry);
        } else {
            return null;
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        var entry = getOrCreateEntry(player.getWorld(), stack);
        if (entry == ShadowEntry.MISSING_ENTRY) {
            return false;
        }
        if (entry.isUninitialized() && slot.canTakePartial(player) && !otherStack.isEmpty()) {
            if (clickType == ClickType.RIGHT) {
                entry.setStack(otherStack.split(1));
            } else {
                cursorStackReference.set(ItemStack.EMPTY);
                entry.setStack(otherStack);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        var entry = getOrCreateEntry(world, stack).getStack();
        entry.usageTick(world, user, remainingUseTicks);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        // Blocked due to duping risks
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        var entry = getOrCreateEntry(world, miner.getMainHandStack()).getStack();
        return entry.getItem().canMine(state, world, pos, miner);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var entry = getOrCreateEntry(context.getWorld(), context.getStack());
        var newContext = ItemUsageContextAccessor.createItemUsageContext(context.getWorld(), context.getPlayer(), context.getHand(), entry.getStack(), ((ItemUsageContextAccessor)context).callGetHitResult());
        return entry.execute(context.getPlayer(), context.getHand(), () -> entry.getStack().useOnBlock(newContext));
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        var holder = stack.getHolder();
        if (holder != null) {
            var entry = getOrCreateEntry(holder.getWorld(), stack);
            
        }

        return super.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        var entry = getOrCreateEntry(world, stack);
        if (entity instanceof PlayerEntity player) {
            entry.execute(player, slot, () -> {
                entry.getStack().inventoryTick(world, entity, slot, selected);
                return 0;
            });
        } else {
            entry.getStack().inventoryTick(world, entity, slot, selected);
        }
    }
}
