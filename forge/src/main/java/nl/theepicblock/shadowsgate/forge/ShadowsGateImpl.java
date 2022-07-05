package nl.theepicblock.shadowsgate.forge;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nl.theepicblock.shadowsgate.ShadowsGate;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

@Mod(ShadowsGate.MOD_ID)
public class ShadowsGateImpl {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShadowsGate.MOD_ID);
    public static final RegistryObject<Item> SHADOW_ITEM = ITEMS.register("shadow_item", () -> new FShadowItem(ShadowsGate.SHADOW_ITEM_SETTINGS));

    public ShadowsGateImpl() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ShadowsGate.init();
        MinecraftForge.EVENT_BUS.addListener(ShadowsGateImpl::serverStart);
        MinecraftForge.EVENT_BUS.addListener(ShadowsGateImpl::serverStop);
    }

    public static void serverStart(ServerStartingEvent event) {
        ShadowsGate.serverStart(event.getServer());
    }

    public static void serverStop(ServerStoppedEvent event) {
        ShadowsGate.serverStop(event.getServer());
    }

    public static Item getShadowItem() {
        return SHADOW_ITEM.get();
    }
}
