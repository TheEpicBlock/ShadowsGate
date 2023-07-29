package nl.theepicblock.shadowsgate.common;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;

public class LithiumCompat {
    public static void updateBE(BlockEntity be) {
        if (be instanceof LithiumInventory inventory) {
            var list = inventory.getInventoryLithium();
            if (list instanceof LithiumStackList lithiumList) {
                lithiumList.changed();
            }
        }
    }
}
