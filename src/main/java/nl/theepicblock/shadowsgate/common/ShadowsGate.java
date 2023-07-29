package nl.theepicblock.shadowsgate.common;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.common.mixin.DispenserBlockAccessor;
import nl.theepicblock.shadowsgate.common.mixin.ItemStackAccessor;
import nl.theepicblock.shadowsgate.common.mixin.PersistentStateManagerAccessor;
import nl.theepicblock.shadowsgate.fabric.ShadowsGateClient;
import nl.theepicblock.shadowsgate.fabric.ShadowsGateImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;

import java.util.ArrayList;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final ArrayList<MinecraftServer> ACTIVE_SERVERS = new ArrayList<>();

    public static final Item.Settings SHADOW_ITEM_SETTINGS = new Item.Settings().maxCount(1);
    public static boolean CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    public static boolean LITHIUM = FabricLoader.getInstance().isModLoaded("lithium");
    /**
     * Prevents the rendering mixin from kicking in when we actually want to render the shadow item model instead of the model inside the shadow item
     */
    public static boolean RenderHack = false;

    public static void init() {
        DispenserBlock.registerBehavior(getShadowItem(), (pointer, stack) -> {
            var entry = ShadowItem.getOrCreateEntry(pointer.getWorld(), stack);
            if (entry == ShadowEntry.MISSING_ENTRY) return stack;
            var behaviour = ((DispenserBlockAccessor)Blocks.DISPENSER).callGetBehaviorForItem(entry.getStack());
            if (behaviour != DispenserBehavior.NOOP) {
                entry.setStack(behaviour.dispense(pointer, entry.getStack()));
                entry.markDirty();
            }
            return stack; // Always keep the stack the same to preserve the shadow item
        });
        ServerTickEvents.END.register(server -> {

            // Each of our persistent states tracks some values per player
            // This code is to garbage-collect any player who no longer has a shadow item in their inventory
            var loadedStates = ((PersistentStateManagerAccessor)server.getOverworld().getPersistentStateManager()).getLoadedStates();
            var playerInvCache = new Object2BooleanOpenHashMap<PlayerEntity>(); // Stores players that have shadowitems in their inventory

            for (var state : loadedStates.values()) {
                if (state instanceof ShadowEntry e) {
                    // Whilst we're here, update any comparators that need to be updated
                    e.updateWorld(server);

                    if ((server.getTicks()+198) % 300 != 0) continue;
                    var iterator = e.dirtynessTracker.keySet().iterator();
                    while (iterator.hasNext()) {
                        var player = iterator.next();
                        var hasShadowItem = playerInvCache.computeIfAbsent(player, player1 -> ((PlayerEntity)player1).getInventory().anyMatch(stack -> stack.getItem() == ShadowsGate.getShadowItem()));
                        if (player.isRemoved() || !hasShadowItem) {
                            iterator.remove();
                        }
                    }
                }
            }
        });
    }

    @NotNull
    public static Item getShadowItem() {
        return ShadowsGateImpl.getShadowItem();
    }

    @Nullable
    public static World getGlobalWorld() {
        if (CLIENT) {
            var clientWorld = ShadowsGateClient.getClientWorld();
            if (clientWorld != null) return clientWorld;
        }
        var server = getGlobalServer();
        if (server != null) return server.getOverworld();
        return null;
    }

    public static MinecraftServer getGlobalServer() {
        for (var server : ACTIVE_SERVERS) {
            if (server.isOnThread()) return server;
        }
        return null;
    }

    public static void serverStart(MinecraftServer server) {
        ACTIVE_SERVERS.add(server);
    }

    public static void serverStop(MinecraftServer server) {
        ACTIVE_SERVERS.remove(server);
    }

    public static World tryGetWorldFromStack(ItemStack stack) {
        var accessor = ((ItemStackAccessor)(Object)stack);
        if (accessor.shadowsgate$getHolder() != null) return accessor.shadowsgate$getHolder().getWorld();
        return getGlobalWorld();
    }
}
