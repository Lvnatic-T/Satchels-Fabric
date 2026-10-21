package net.lvnatic.satchels.mixin.compat;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.compat.SophisticatedBackpacksCompat;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        targets = "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase",
        remap = false
)
public abstract class SophisticatedCoreMenuMixin extends AbstractContainerMenu {
    protected SophisticatedCoreMenuMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(
            method = "addPlayerInventorySlots",
            at = @At("HEAD"),
            remap = false
    )
    private void satchels$addInventorySlots(
            Inventory inventory,
            int storageItemSlotIndex,
            boolean shouldLockStorageItemSlot,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof BackpackContainer)) return;

        SatchelInventory satchel = SatchelData.get(inventory.player).getSatchelInventory();

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            addSlot(new SatchelInventorySlot(
                    satchel,
                    i,
                    i * 18,
                    0
            ));
        }
    }

    @ModifyConstant(
            method = {
                    "hasSomethingMessedWithStorage",
                    "isUpgradeSettingsSlot"
            },
            constant = @Constant(intValue = 36),
            remap = false
    )
    private int satchels$adjustPlayerSlotCount(int original) {
        return (Object) this instanceof BackpackContainer
                ? original + SatchelInventory.SIZE
                : original;
    }

    @Inject(
            method = {"clicked", "method_7593"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void satchels$protectBackpackClick(
            int slotId,
            int dragType,
            ClickType clickType,
            Player player,
            CallbackInfo ci
    ) {
        if (satchels$isOpenBackpackSlot(slotId)) {
            ci.cancel();
        }
    }

    @Inject(
            method = {"quickMoveStack", "method_7601"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void satchels$protectBackpackQuickMove(
            Player player,
            int slotId,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (satchels$isOpenBackpackSlot(slotId)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Unique
    private boolean satchels$isOpenBackpackSlot(int slotId) {
        if (!((Object) this instanceof BackpackContainer backpack)) return false;
        if (slotId < 0 || slotId >= slots.size()) return false;

        Slot slot = slots.get(slotId);

        if (!(slot instanceof SatchelInventorySlot satchelSlot)) return false;

        BackpackContext parentContext = backpack.getBackpackContext().getParentBackpackContext();

        if (!(parentContext instanceof BackpackContext.Item itemContext)) return false;

        String handler = ((SophisticatedBackpacksContextAccessor) itemContext)
                .satchels$getHandlerName();

        return SophisticatedBackpacksCompat.INVENTORY_HANDLER.equals(handler)
                && satchelSlot.getContainerSlot() == itemContext.getBackpackSlotIndex();
    }
}