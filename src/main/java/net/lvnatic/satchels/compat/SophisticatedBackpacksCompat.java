package net.lvnatic.satchels.compat;

import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.Set;

public final class SophisticatedBackpacksCompat {
    public static final String INVENTORY_HANDLER = "satchels";

    private SophisticatedBackpacksCompat() {
    }

    public static void initialize() {
        PlayerInventoryProvider.get().addPlayerInventoryHandler(
                INVENTORY_HANDLER,
                (player, gameTime) -> {
                    SatchelData data = SatchelData.get(player);

                    return data.canAccess()
                            ? PlayerInventoryHandler.SINGLE_IDENTIFIER
                            : Set.of();
                },
                (player, identifier) -> SatchelInventory.SIZE,
                (player, identifier, slot) -> {
                    if (slot < 0 || slot >= SatchelInventory.SIZE) {
                        return ItemStack.EMPTY;
                    }

                    return SatchelData.get(player)
                            .getSatchelInventory()
                            .getItem(slot);
                },
                false,
                false,
                false,
                false
        );
    }
}