package net.lvnatic.satchels.mixin.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    protected InventoryMenuMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void satchels$addInventorySlots(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
        SatchelData data = SatchelData.get(player);

        for (int i = 0; i < 6; i++) {
            this.addSlot(new SatchelInventorySlot(
                    data.getSatchelInventory(),
                    i,
                    8 + i * 18,
                    170
            ));
        }
    }
}