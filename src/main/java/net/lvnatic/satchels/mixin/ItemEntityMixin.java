package net.lvnatic.satchels.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @WrapOperation(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean satchels$pickup(Inventory inventory, ItemStack stack, Operation<Boolean> original) {
        SatchelData data = SatchelData.get(inventory.player);

        if (!data.canAccess() || stack.isEmpty()) {
            return original.call(inventory, stack);
        }

        SatchelInventory satchel = data.getSatchelInventory();
        int originalCount = stack.getCount();

        satchels$mergeExisting(satchel, stack);

        if (data.isActive()) {
            satchels$fillEmpty(satchel, stack);
        }

        boolean vanillaPickup = false;

        if (!stack.isEmpty()) {
            vanillaPickup = original.call(inventory, stack);
        }

        if (!data.isActive() && !stack.isEmpty()) {
            satchels$fillEmpty(satchel, stack);
        }

        return vanillaPickup || stack.getCount() < originalCount;
    }

    @Unique
    private void satchels$mergeExisting(SatchelInventory inventory, ItemStack incoming) {
        if (!incoming.isStackable()) return;

        for (int i = 0; i < inventory.getContainerSize() && !incoming.isEmpty(); i++) {
            ItemStack stored = inventory.getItem(i);

            if (stored.isEmpty() || !stored.isStackable()) continue;
            if (!ItemStack.isSameItemSameComponents(stored, incoming)) continue;

            int available = stored.getMaxStackSize() - stored.getCount();
            if (available <= 0) continue;

            int amount = Math.min(available, incoming.getCount());

            stored.grow(amount);
            incoming.shrink(amount);
            stored.setPopTime(5);
            inventory.setChanged();
        }
    }

    @Unique
    private void satchels$fillEmpty(SatchelInventory inventory, ItemStack incoming) {
        for (int i = 0; i < inventory.getContainerSize() && !incoming.isEmpty(); i++) {
            if (!inventory.getItem(i).isEmpty()) continue;

            int amount = Math.min(incoming.getCount(), incoming.getMaxStackSize());
            ItemStack inserted = incoming.split(amount);

            inserted.setPopTime(5);
            inventory.setItem(i, inserted);
            inventory.setChanged();
        }
    }
}