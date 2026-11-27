package net.lvnatic.satchels.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.lvnatic.satchels.ModItems;
import net.lvnatic.satchels.client.tooltip.SatchelTooltipData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void satchels$contentsPreview(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!stack.is(ModItems.SATCHEL) || !Screen.hasShiftDown()) return;

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(SatchelInventory.SIZE, ItemStack.EMPTY);
        contents.copyInto(items);
        if (items.stream().allMatch(ItemStack::isEmpty)) return;

        int color = DyedItemColor.getOrDefault(stack, SatchelItem.DEFAULT_COLOR);
        cir.setReturnValue(Optional.of(new SatchelTooltipData(contents, color)));
    }
}