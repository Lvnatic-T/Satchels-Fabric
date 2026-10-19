package net.lvnatic.satchels.mixin.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    protected abstract Slot addSlot(Slot slot);

    @Unique
    private boolean satchels$slotsAdded;

    @Inject(method = "addSlot", at = @At("RETURN"))
    private void satchels$addInventorySlots(Slot slot, CallbackInfoReturnable<Slot> cir) {
        if (satchels$slotsAdded) return;
        if ((Object) this instanceof InventoryMenu) return;
        if (!(slot.container instanceof Inventory inventory)) return;
        if (slot.getContainerSlot() != 8) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        try {
            if (menu.getType() == null) return;
        } catch (UnsupportedOperationException ignored) {
            return;
        }

        satchels$slotsAdded = true;

        SatchelData data = SatchelData.get(inventory.player);
        SatchelInventory satchelInventory = data.getSatchelInventory();

        int startX = slot.x - 8 * 18;
        int y = slot.y + 28;

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            addSlot(new SatchelInventorySlot(
                    satchelInventory,
                    i,
                    startX + i * 18,
                    y
            ));
        }
    }
}