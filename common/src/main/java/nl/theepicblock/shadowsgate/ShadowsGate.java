package nl.theepicblock.shadowsgate;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.mixin.DispenserBlockAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final ArrayList<MinecraftServer> ACTIVE_SERVERS = new ArrayList<>();

    public static final Item.Settings SHADOW_ITEM_SETTINGS = new Item.Settings().maxCount(1);

    public static void init() {
        Networking.init();
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
    }

    @ExpectPlatform
    @Nullable
    public static Item getShadowItem() {
        throw new AssertionError();
    }

    @Nullable
    public static World getGlobalWorld() {
        if (MinecraftClient.getInstance().isOnThread()) {
            return getClientWorld();
        }
        for (var server : ACTIVE_SERVERS) {
            if (server.isOnThread()) return server.getOverworld();
        }
        return null;
    }

    private static World getClientWorld() {
        return MinecraftClient.getInstance().world;
    }

    public static void serverStart(MinecraftServer server) {
        ACTIVE_SERVERS.add(server);
    }

    public static void serverStop(MinecraftServer server) {
        ACTIVE_SERVERS.remove(server);
    }

    public static World tryGetWorldFromStack(ItemStack stack) {
        if (stack.getHolder() != null) return stack.getHolder().getWorld();
        return getGlobalWorld();
    }
}
