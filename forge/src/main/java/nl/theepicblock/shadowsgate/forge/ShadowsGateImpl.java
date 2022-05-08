package nl.theepicblock.shadowsgate.forge;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nl.theepicblock.shadowsgate.ShadowsGate;

@Mod(ShadowsGate.MOD_ID)
public class ShadowsGateImpl {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShadowsGate.MOD_ID);
    public static final RegistryObject<Item> SHADOW_ITEM = ITEMS.register("shadow_item", ShadowsGate.SHADOW_ITEM_SUPPLIER);

    public ShadowsGateImpl() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ShadowsGate.init();
    }

    public static Item getShadowItem() {
        return SHADOW_ITEM.get();
    }
}
