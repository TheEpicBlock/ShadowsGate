package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import nl.theepicblock.shadowsgate.ShadowsGate;

public class ShadowsGateImpl implements ModInitializer {
    public static final Item SHADOW_ITEM = ShadowsGate.SHADOW_ITEM_SUPPLIER.get();

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(ShadowsGate.MOD_ID, "shadow_item"), SHADOW_ITEM);
    }

    public static Item getShadowItem() {
        return SHADOW_ITEM;
    }
}
