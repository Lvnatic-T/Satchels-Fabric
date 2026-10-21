package net.lvnatic.satchels.mixin.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase",
        remap = false
)
public abstract class SophisticatedCoreScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    protected SophisticatedCoreScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(
            method = "updatePlayerSlotsPositions",
            at = @At("TAIL"),
            remap = false
    )
    private void satchels$positionInventorySlots(CallbackInfo ci) {
        if (!(menu instanceof BackpackContainer backpack)) return;

        int vanillaStart = backpack.getInventorySlotsSize() - 36;
        int satchelStart = vanillaStart - SatchelInventory.SIZE;

        if (satchelStart < 0) return;

        Slot firstHotbarSlot = backpack.getSlot(vanillaStart + 27);

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            Slot slot = backpack.getSlot(satchelStart + i);

            if (slot instanceof SatchelInventorySlot satchelSlot) {
                satchelSlot.setBasePosition(
                        firstHotbarSlot.x + i * 18,
                        firstHotbarSlot.y + 28
                );
            }
        }
    }

    @Inject(
            method = {"renderLabels", "method_2388"},
            at = @At("TAIL"),
            remap = false
    )
    private void satchels$renderInventorySlots(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (!(menu instanceof BackpackContainer backpack)) return;
        if (minecraft == null || minecraft.player == null) return;

        SatchelData data = SatchelData.get(minecraft.player);
        if (!data.canAccess()) return;

        int satchelStart = backpack.getInventorySlotsSize()
                - 36
                - SatchelInventory.SIZE;

        if (satchelStart < 0) return;

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            Slot slot = backpack.getSlot(satchelStart + i);

            if (!(slot instanceof SatchelInventorySlot)) continue;
            if (!slot.isActive()) continue;

            renderSlot(graphics, slot);

            if (isHovering(slot, mouseX, mouseY)) {
                hoveredSlot = slot;
            }
        }
    }
}