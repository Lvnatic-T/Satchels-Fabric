package net.lvnatic.satchels.content.satchel;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.mixin.accessor.SlotAccessor;

public class SatchelInventorySlot extends Slot {
    private final SatchelData satchelData;
    private int baseX;

    public SatchelInventorySlot(SatchelInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
        this.satchelData = inventory.getParent();
        this.baseX = x;
        updateX();
    }

    public void updateX() {
        ((SlotAccessor) this).satchels$setX(baseX + satchelData.getHotbarOffset() * 18);
    }

    public void setBasePosition(int x, int y) {
        this.baseX = x;
        this.y = y;
        updateX();
    }

    @Override
    public boolean isActive() {
        return satchelData.canAccess();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return satchelData.canAccess() && SatchelStorageRules.canStore(stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (!SatchelStorageRules.canStore(stack)) {
            ItemStack stored = getItem();

            return ItemStack.isSameItemSameComponents(stored, stack)
                    ? stored.getCount()
                    : 0;
        }

        return super.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return satchelData.canAccess() && super.mayPickup(player);
    }

    @Override
    public boolean allowModification(Player player) {
        return satchelData.canAccess() && super.allowModification(player);
    }

    @Override
    public boolean isHighlightable() {
        return satchelData.canAccess();
    }
}