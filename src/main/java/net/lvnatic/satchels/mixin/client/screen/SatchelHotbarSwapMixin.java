package net.lvnatic.satchels.mixin.client.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.lvnatic.satchels.client.SatchelsClientConfig;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import net.lvnatic.satchels.network.packets.SwapSatchelHotbarPacketC2S;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public abstract class SatchelHotbarSwapMixin {
    @WrapOperation(
            method = "checkHotbarKeyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
                    ordinal = 1
            )
    )
    private void satchels$swapWithSatchel(
            AbstractContainerScreen<?> screen,
            Slot slot,
            int slotIndex,
            int hotbarIndex,
            ClickType clickType,
            Operation<Void> original
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null ||
                !Screen.hasShiftDown() ||
                !SatchelsClientConfig.shouldSwapWithShiftKey()) {
            original.call(screen, slot, slotIndex, hotbarIndex, clickType);
            return;
        }

        SatchelData data = SatchelData.get(minecraft.player);

        if (!data.canAccess() || !data.isSlotInSatchel(hotbarIndex)) {
            original.call(screen, slot, slotIndex, hotbarIndex, clickType);
            return;
        }

        int satchelIndex = data.convertToSatchelIndex(hotbarIndex);
        boolean found = false;

        for (Slot candidate : screen.getMenu().slots) {
            if (candidate instanceof SatchelInventorySlot &&
                    candidate.getContainerSlot() == satchelIndex) {
                found = true;
                break;
            }
        }

        if (!found) {
            original.call(screen, slot, slotIndex, hotbarIndex, clickType);
            return;
        }

        ClientPlayNetworking.send(new SwapSatchelHotbarPacketC2S(
                screen.getMenu().containerId,
                slotIndex,
                satchelIndex
        ));
    }
}