package net.lvnatic.satchels.mixin.client.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.client.SatchelsClient;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow @Final protected T menu;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private SatchelInventorySlot satchels$firstSlot() {
        for (Slot slot : menu.slots) {
            if (slot instanceof SatchelInventorySlot satchelSlot && slot.getContainerSlot() == 0) {
                return satchelSlot;
            }
        }
        return null;
    }

    @Unique
    private boolean satchels$shouldRender() {
        if ((Object) this instanceof InventoryScreen) return false;
        if ((Object) this instanceof CreativeModeInventoryScreen) return false;
        if (minecraft == null || minecraft.player == null) return false;
        return SatchelData.get(minecraft.player).canAccess();
    }

    @Inject(
            method = "renderBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void satchels$renderInventory(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!satchels$shouldRender()) return;

        SatchelInventorySlot firstSlot = satchels$firstSlot();
        if (firstSlot == null) return;

        SatchelsClient.applySatchelColor(graphics, minecraft.player);

        graphics.blitSprite(
                Satchels.at("satchel_inventory"),
                leftPos + firstSlot.x - 6,
                topPos + firstSlot.y - 5,
                118,
                27
        );

        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @ModifyExpressionValue(
            method = {"mouseClicked", "mouseReleased"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"
            )
    )
    private boolean satchels$handleOutsideClick(boolean original, double mouseX, double mouseY, int button) {
        if (!original || !satchels$shouldRender()) return original;

        SatchelInventorySlot firstSlot = satchels$firstSlot();
        if (firstSlot == null) return original;

        int x = leftPos + firstSlot.x - 6;
        int y = topPos + firstSlot.y - 5;

        if (mouseX >= x && mouseX < x + 118 && mouseY >= y && mouseY < y + 27) {
            return false;
        }

        return original;
    }
}