package net.lvnatic.satchels.mixin.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.client.SatchelsClient;
import net.lvnatic.satchels.content.satchel.SatchelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    protected InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void satchels$renderInventory(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (minecraft == null || minecraft.player == null) return;

        SatchelData data = SatchelData.get(minecraft.player);
        if (!data.canAccess()) return;

        SatchelsClient.applySatchelColor(graphics, minecraft.player);

        graphics.blitSprite(
                Satchels.at("satchel_inventory"),
                leftPos + 2 + data.getHotbarOffset() * 18,
                topPos + imageHeight - 1,
                118,
                27
        );

        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void satchels$handleOutsideClick(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft == null || minecraft.player == null) return;

        SatchelData data = SatchelData.get(minecraft.player);
        if (!data.canAccess()) return;

        int left = leftPos + 2 + data.getHotbarOffset() * 18;
        int top = topPos + imageHeight - 1;

        if (mouseX >= left && mouseX < left + 118 && mouseY >= top && mouseY < top + 27) {
            cir.setReturnValue(false);
        }
    }
}