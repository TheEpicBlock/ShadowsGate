package nl.theepicblock.shadowsgate.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nl.theepicblock.shadowsgate.common.Commands;
import nl.theepicblock.shadowsgate.common.Networking;
import nl.theepicblock.shadowsgate.common.ShadowEntry;
import nl.theepicblock.shadowsgate.common.ShadowsGate;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class ShadowsGateImpl implements ModInitializer {
    public static final Item SHADOW_ITEM = new QShadowItem(ShadowsGate.SHADOW_ITEM_SETTINGS);

    public static Item getShadowItem() {
        return SHADOW_ITEM;
    }

	@Override
	public void onInitialize(ModContainer mod) {
        Registry.register(Registries.ITEM, new Identifier(ShadowsGate.MOD_ID, "shadow_item"), SHADOW_ITEM);
        ShadowsGate.init();

        ServerLifecycleEvents.SERVER_STARTING.register(ShadowsGate::serverStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(ShadowsGate::serverStop);

        ServerPlayNetworking.registerGlobalReceiver(Networking.UPDATE_ENTRY_C2S, (server, player, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var stack = buf.readItemStack();
            server.execute(() -> {
                if (!player.isCreative()) {
                    ShadowsGate.LOGGER.warn(player+" tried to directly modify shadow stack contents, despite not being in creative");
                    return;
                }
                var entry = ShadowEntry.getExisting(server, id);
                if (entry != null) {
                    entry.setStack(stack);
                }
            });
        });

        CommandRegistrationCallback.EVENT.register(Commands::register);
	}
}
