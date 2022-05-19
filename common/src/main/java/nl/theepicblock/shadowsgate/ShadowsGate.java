package nl.theepicblock.shadowsgate;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import nl.theepicblock.shadowsgate.mixin.DispenserBlockAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

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
}
