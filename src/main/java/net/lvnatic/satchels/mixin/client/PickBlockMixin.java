package net.lvnatic.satchels.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelStorageRules;
import net.lvnatic.satchels.network.packets.ToggleSatchelPacketC2S;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class PickBlockMixin {
    @Inject(
            method = "pickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"
            ),
            cancellable = true
    )
    private void satchels$pickBlock(
            CallbackInfo ci,
            @Local ItemStack stack,
            @Local Inventory inventory,
            @Local boolean creative
    ) {
        SatchelData data = SatchelData.get(inventory.player);

        if (creative) {
            satchels$setActive(data, false);
            return;
        }

        if (!data.canAccess()) return;

        if (!SatchelStorageRules.canStore(stack)) {
            satchels$setActive(data, false);
            return;
        }

        int normalSlot = inventory.findSlotMatchingItem(stack);
        SatchelInventory satchelInventory = data.getSatchelInventory();

        int satchelSlot = -1;
        int emptySatchelSlot = -1;

        for (int i = 0; i < satchelInventory.getContainerSize(); i++) {
            ItemStack stored = satchelInventory.getItem(i);

            if (stored.isEmpty()) {
                if (emptySatchelSlot == -1) emptySatchelSlot = i;
            } else if (satchelSlot == -1 && ItemStack.isSameItemSameComponents(stack, stored)) {
                satchelSlot = i;
            }
        }

        if (data.isActive()) {
            if (satchelSlot != -1) {
                inventory.selected = satchelSlot + data.getHotbarOffset();
                ci.cancel();
                return;
            }

            if (normalSlot >= 9 && emptySatchelSlot != -1) {
                return;
            }

            if (normalSlot >= 0) {
                satchels$setActive(data, false);
            }

            return;
        }

        if (normalSlot >= 0 && normalSlot < 9) return;

        if (satchelSlot != -1) {
            inventory.selected = satchelSlot + data.getHotbarOffset();
            satchels$setActive(data, true);
            ci.cancel();
        }
    }

    @Unique
    private void satchels$setActive(SatchelData data, boolean enabled) {
        if (data.isActive() == enabled) return;

        data.setActive(enabled, false);
        ClientPlayNetworking.send(new ToggleSatchelPacketC2S(enabled));
    }
}