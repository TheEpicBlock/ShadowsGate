package nl.theepicblock.shadowsgate;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ShadowsGate {
    public static final String MOD_ID = "shadowsgate";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Supplier<Item> SHADOW_ITEM_SUPPLIER = () -> new ShadowItem(new Item.Settings().maxCount(1));

    public static void init() {
    }

    @ExpectPlatform
    public static Item getShadowItem() {
        throw new AssertionError();
    }
}
