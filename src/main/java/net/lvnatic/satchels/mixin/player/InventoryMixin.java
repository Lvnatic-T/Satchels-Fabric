package net.lvnatic.satchels.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelStorageRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow @Final public Player player;
    @Shadow @Final public NonNullList<ItemStack> items;
    @Shadow public int selected;

    @ModifyReturnValue(method = "getSelected", at = @At("RETURN"))
    private ItemStack satchels$getSelected(ItemStack original) {
        SatchelData data = SatchelData.get(player);

        if (!data.isActive() || !data.canAccess() || !data.isSlotInSatchel(selected)) {
            return original;
        }

        return data.getSatchelInventory().getItem(data.convertToSatchelIndex(selected));
    }

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float satchels$getDestroySpeed(float original, BlockState state) {
        SatchelData data = SatchelData.get(player);

        if (!data.isActive() || !data.canAccess() || !data.isSlotInSatchel(selected)) {
            return original;
        }

        return data.getSatchelInventory().getItem(data.convertToSatchelIndex(selected)).getDestroySpeed(state);
    }

    @Inject(method = "removeFromSelected", at = @At("HEAD"), cancellable = true)
    private void satchels$removeFromSelected(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        SatchelData data = SatchelData.get(player);

        if (!data.isActive() || !data.canAccess() || !data.isSlotInSatchel(selected)) {
            return;
        }

        SatchelInventory inventory = data.getSatchelInventory();
        int slot = data.convertToSatchelIndex(selected);
        ItemStack stack = inventory.getItem(slot);

        cir.setReturnValue(inventory.removeItem(slot, entireStack ? stack.getCount() : 1));
    }

    @Inject(method = "pickSlot", at = @At("HEAD"), cancellable = true)
    private void satchels$pickIntoSatchel(int sourceSlot, CallbackInfo ci) {
        SatchelData data = SatchelData.get(player);

        if (!data.isActive() || !data.canAccess()) return;
        if (sourceSlot < 9 || sourceSlot >= items.size()) return;

        ItemStack sourceStack = items.get(sourceSlot);
        if (sourceStack.isEmpty() || !SatchelStorageRules.canStore(sourceStack)) return;

        SatchelInventory satchelInventory = data.getSatchelInventory();

        for (int i = 0; i < satchelInventory.getContainerSize(); i++) {
            if (!satchelInventory.getItem(i).isEmpty()) continue;

            items.set(sourceSlot, ItemStack.EMPTY);
            satchelInventory.setItem(i, sourceStack);
            selected = i + data.getHotbarOffset();

            ((Inventory) (Object) this).setChanged();
            ci.cancel();
            return;
        }
    }

    @ModifyReturnValue(method = "clearOrCountMatchingItems", at = @At("RETURN"))
    private int satchels$clearOrCountMatchingItems(
            int vanillaCount,
            Predicate<ItemStack> predicate,
            int maxCount,
            Container craftingInventory
    ) {
        if (maxCount > 0 && vanillaCount >= maxCount) {
            return vanillaCount;
        }

        SatchelInventory satchelInventory = SatchelData.get(player).getSatchelInventory();

        int remaining = maxCount > 0 ? maxCount - vanillaCount : maxCount;

        int satchelCount = ContainerHelper.clearOrCountMatchingItems(
                satchelInventory,
                predicate,
                remaining,
                maxCount == 0
        );

        if (satchelCount > 0 && maxCount != 0) {
            satchelInventory.setChanged();
        }

        return vanillaCount + satchelCount;
    }
}