package nl.theepicblock.shadowsgate.common.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import nl.theepicblock.shadowsgate.common.Networking;
import nl.theepicblock.shadowsgate.common.ShadowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PacketByteBuf.class)
public class OnItemPacket {
    @Inject(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"))
    public void writeItemStackHook(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        var player = PacketContext.get().getTarget();
        if (player == null) return;
        var entry = ShadowItem.getEntry(player.getWorld(), stack);
        if (entry == null) return;

        // Send a corresponding packet, just in case
        if (entry.checkDirt(player)) {
            entry.resetDirt(player);
            player.networkHandler.sendPacket(Networking.createPacket(ShadowItem.getIndex(stack), entry));
        }
    }
}
