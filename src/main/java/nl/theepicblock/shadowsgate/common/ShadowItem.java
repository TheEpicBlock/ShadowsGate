package nl.theepicblock.shadowsgate.common;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.mixin.ItemStackAccessor;
import nl.theepicblock.shadowsgate.common.mixin.ItemUsageContextAccessor;
import nl.theepicblock.shadowsgate.fabric.NetworkingImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ShadowItem extends NetworkSyncedItem {
    public ShadowItem(Settings settings) {
        super(settings);
    }

    public static int getIndex(@NotNull ItemStack stack) {
        var nbt = stack.getNbt();
        if (nbt == null) return -1;
        if (!nbt.contains("shadowindex", NbtElement.NUMBER_TYPE)) return -1;
        return nbt.getInt("shadowindex");
    }

    public static ShadowEntry getOrCreateEntry(World world, ItemStack stack) {
        // TODO maybe make sure this is a shadow item
        Objects.requireNonNull(world);
        var index = getIndex(stack);
        if (world instanceof ServerWorld serverWorld) {
            if (index == -1) {
                index = ShadowEntryCount.get(serverWorld.getServer().getOverworld().getPersistentStateManager()).getNextId();
                stack.getOrCreateNbt().putInt("shadowindex", index);
            }

            return ShadowEntry.get(serverWorld.getServer(), index);
        } else if (world.isClient) {
            return ShadowEntry.getEntryClient(index);
        }

        return ShadowEntry.MISSING_ENTRY;
    }

    @Nullable
    public static ShadowEntry getEntry(@NotNull World world, @NotNull ItemStack stack) {
        // TODO maybe make sure this is a shadow item
        Objects.requireNonNull(world);
        var index = getIndex(stack);
        if (world instanceof ServerWorld serverWorld) {
            if (index == -1) {
                return null;
            }

            return ShadowEntry.getExisting(serverWorld.getServer(), index);
        } else if (world.isClient) {
            var clientEntry = ShadowEntry.getEntryClient(index);
            if (clientEntry == ShadowEntry.MISSING_ENTRY) return null;
            return clientEntry;
        }

        return ShadowEntry.MISSING_ENTRY;
    }

    public static ShadowEntry getClientEntry(ItemStack stack) {
        var index = ShadowItem.getIndex(stack);
        if (index == -1) return ShadowEntry.MISSING_ENTRY;
        return ShadowEntry.getEntryClient(index);
    }

    @Nullable
    @Override
    public Packet<?> createSyncPacket(ItemStack stack, World world, PlayerEntity player) {
        var entry = getOrCreateEntry(world, stack);
        if (entry == ShadowEntry.MISSING_ENTRY) return null;
        if (entry.checkDirt(player)) {
            entry.resetDirt(player);
            return Networking.createPacket(getIndex(stack), entry);
        } else {
            // The entry isn't marked dirty for this player, so no need to send them a packet
            return null;
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        var entry = getOrCreateEntry(player.getWorld(), stack);
        if (entry == ShadowEntry.MISSING_ENTRY) return false;

        if (otherStack.isEmpty()) {
            if (slot.canTakePartial(player)) {
                if (clickType == ClickType.RIGHT) {
                    // Right click with an empty hand
                    cursorStackReference.set(entry.fakeInv.removeStack(0, entry.getStack().getCount()/2));
                    playerChangedContents(stack, entry, player);
                    return true;
                }
            }
        } else {
            if (entry.canInsertStack(otherStack) && slot.canTakePartial(player) && !otherStack.isEmpty()) {
                if (clickType == ClickType.RIGHT) {
                    // Right click with another item
                    cursorStackReference.set(entry.getFakeSlot().insertStack(otherStack, 1));
                } else {
                    // Left click with another item
                    cursorStackReference.set(entry.getFakeSlot().insertStack(otherStack));
                }
                playerChangedContents(stack, entry, player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onClickedOnOther(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.RIGHT) {
            // Right click with a shadow item on another slot
            var entry = getOrCreateEntry(player.getWorld(), stack);
            entry.setStack(slot.insertStack(entry.getStack(), 1));
            playerChangedContents(stack, entry, player);
            return true;
        }
        // FIXME add behaviour when left-clicking stacks with the same item
        return false;
    }

    /**
     * Called when an action of the player (eg, right-clicking the shadow stack with another item) causes the
     * contents of the shadow stack to change
     */
    private static void playerChangedContents(ItemStack stack, ShadowEntry entry, PlayerEntity player) {
        if (ShadowsGate.CLIENT) {
            playerChangedContentsInner(stack, entry, player);
        }
    }

    @ClientOnly
    private static void playerChangedContentsInner(ItemStack stack, ShadowEntry entry, PlayerEntity player) {
        if (player.isCreative() && player instanceof ClientPlayerEntity clientPlayer) {
            clientPlayer.networkHandler.sendPacket(NetworkingImpl.createUpdatePacket(getIndex(stack), entry));
        }
    }

        @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var world2 = world == null ? (((ItemStackAccessor)(Object)stack).shadowsgate$getHolder() == null ? null : ((ItemStackAccessor)(Object)stack).shadowsgate$getHolder().getWorld()) : world;
        if (world2 != null) {
            var entry = getOrCreateEntry(world2, stack);
            if (!entry.getStack().isEmpty() || context.shouldShowAdvancedDetails()) {
                if (context.shouldShowAdvancedDetails()) {
                    tooltip.add(Text.translatable("item.shadowsgate.shadow_item.lore_advanced", getIndex(stack)).formatted(Formatting.GRAY));
                } else {
                    tooltip.add(Text.translatable("item.shadowsgate.shadow_item.lore").formatted(Formatting.GRAY));
                }
            }
            entry.getStack().getItem().appendTooltip(entry.getStack(), world, tooltip, context);
        }
    }

    // Methods to copy behaviour of entry item:

    public Text getName(ItemStack srcStack) {
        World world = ShadowsGate.tryGetWorldFromStack(srcStack);
        if (world != null) {
            var entry = getOrCreateEntry(world, srcStack);
            if (entry == ShadowEntry.MISSING_ENTRY) {
                return Text.translatable("item.shadowsgate.shadow_item.not_synced");
            } else if (entry.getStack().isEmpty()) {
                // Will default to "empty shadowstack"
                return this.getName();
            }
            return entry.getStack().getName();
        }

        // We're unable to get the proper name because we can't get a world
        return Text.translatable("item.shadowsgate.shadow_item.unknown");
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack srcStack, int remainingUseTicks) {
        var entry = getOrCreateEntry(world, srcStack);
        var stack = entry.getStack();
        stack.usageTick(world, user, remainingUseTicks);
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
    @MarksAsDirty
    public ActionResult useOnBlock(ItemUsageContext context) {
        var entry = getOrCreateEntry(context.getWorld(), context.getStack());
        var newContext = ItemUsageContextAccessor.createItemUsageContext(context.getWorld(), context.getPlayer(), context.getHand(), entry.getStack(), ((ItemUsageContextAccessor)context).callGetHitResult());
        var result = entry.execute(context.getPlayer(), context.getHand(), () -> entry.getStack().useOnBlock(newContext));
        if (result.isAccepted()) {
            entry.markDirty();
        }
        return result;
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        var holder = ((ItemStackAccessor)(Object)stack).shadowsgate$getHolder();
        if (holder != null) {
            var entry = getOrCreateEntry(holder.getWorld(), stack);
            return entry.getStack().getMiningSpeedMultiplier(state);
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var entry = getOrCreateEntry(world, stack);
        var result = entry.execute(user, hand, () -> entry.getStack().use(world, user, hand));
        if (result.getValue() != entry.getStack() || result.getResult().isAccepted()) {
            entry.setStack(result.getValue());
            entry.markDirty();
        }
        return new TypedActionResult<>(result.getResult(), stack);
    }

    @Override
    @MarksAsDirty
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        var entry = getOrCreateEntry(world, stack);
        entry.setStack(entry.getStack().finishUsing(world, user));
        entry.markDirty();
        return stack;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        var entry = getOrCreateEntry(world, stack);
        if (user instanceof PlayerEntity player) {
            entry.executeActiveHand(player, stack, () -> {
                entry.getStack().onStoppedUsing(world, user, remainingUseTicks);
                return 0;
            });
        } else {
            entry.getStack().onStoppedUsing(world, user, remainingUseTicks);
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var world = target == null ? (attacker == null ? null : attacker.getWorld()) : target.getWorld();
        var entry = getOrCreateEntry(world, stack);
        if (attacker instanceof PlayerEntity player) {
            return entry.executeActiveHand(player, stack, () -> entry.getStack().getItem().postHit(stack, target, attacker));
        } else {
            return entry.getStack().getItem().postHit(stack, target, attacker);
        }
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        var entry = getOrCreateEntry(world, stack);
        if (miner instanceof PlayerEntity player) {
            return entry.executeActiveHand(player, stack, () -> entry.getStack().getItem().postMine(stack, world, state, pos, miner));
        } else {
            return entry.getStack().getItem().postMine(stack, world, state, pos, miner);
        }
    }

    @Override
    @MarksAsDirty
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        var entry = getOrCreateEntry(user.getWorld(), stack);
        var result = entry.executeActiveHand(user, stack, () -> entry.getStack().useOnEntity(user, entity, hand));
        if (result.isAccepted()) {
            entry.markDirty();
        }
        return result;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        World world = ShadowsGate.tryGetWorldFromStack(stack);
        if (world != null) {
            return getOrCreateEntry(world, stack).getStack().getUseAction();
        }
        return super.getUseAction(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        World world = ShadowsGate.tryGetWorldFromStack(stack);
        if (world != null) {
            return getOrCreateEntry(world, stack).getStack().getMaxUseTime();
        }
        return super.getMaxUseTime(stack);
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        World world = ShadowsGate.tryGetWorldFromStack(stack);
        if (world != null) {
            return getOrCreateEntry(world, stack).getStack().isUsedOnRelease();
        }
        return super.isUsedOnRelease(stack);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        World world = ShadowsGate.tryGetWorldFromStack(stack);
        if (world != null) {
            return getOrCreateEntry(world, stack).getStack().getTooltipData();
        }
        return super.getTooltipData(stack);
    }

}
