package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import nl.theepicblock.shadowsgate.ShadowItem;
import nl.theepicblock.shadowsgate.ShadowsGate;

public class ShadowsGateImpl implements ModInitializer {
    public static final Item SHADOW_ITEM = new QShadowItem(ShadowsGate.SHADOW_ITEM_SETTINGS);

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(ShadowsGate.MOD_ID, "shadow_item"), SHADOW_ITEM);
        ShadowsGate.init();

        ServerLifecycleEvents.SERVER_STARTING.register(ShadowsGate::serverStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(ShadowsGate::serverStop);
    }

    public static Item getShadowItem() {
        return SHADOW_ITEM;
    }
}
