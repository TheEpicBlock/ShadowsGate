package nl.theepicblock.shadowsgate;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import nl.theepicblock.shadowsgate.mixin.DispenserBlockAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static @Nullable MinecraftServer SERVER_INSTANCE;
    private static boolean SERVER_SANITY_CHECK = false;

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
        // This'll be called from both the server and client side, I'm going to prefer to let the server be authoritative
        if (SERVER_INSTANCE != null) return SERVER_INSTANCE.getOverworld();
        if (SERVER_SANITY_CHECK == false) {
            // A server isn't currently active, nor has the sanity check tripped, so the client it is
            return getClientWorld();
        } else {
            return null;
        }
    }

    private static World getClientWorld() {
        return MinecraftClient.getInstance().world;
    }

    public static void serverStart(MinecraftServer server) {
        if (SERVER_INSTANCE == null && SERVER_SANITY_CHECK == false) {
            SERVER_INSTANCE = server;
        } else {
            ShadowsGate.LOGGER.warn("More than one server started, shadows gate sanity check has been tripped.");
            SERVER_INSTANCE = null;
            SERVER_SANITY_CHECK = true;
        }
    }


    public static void serverStop(MinecraftServer server) {
        SERVER_INSTANCE = null;
    }
}
