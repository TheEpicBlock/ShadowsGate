package nl.theepicblock.shadowsgate;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Item.Settings SHADOW_ITEM_SETTINGS = new Item.Settings().maxCount(1);

    public static void init() {
        Networking.init();
    }

    @ExpectPlatform
    @Nullable
    public static Item getShadowItem() {
        throw new AssertionError();
    }
}
