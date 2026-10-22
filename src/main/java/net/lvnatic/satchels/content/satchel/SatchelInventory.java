package net.lvnatic.satchels.content.satchel;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SatchelInventory implements Container, StackedContentsCompatible {
    public static final int SIZE = 6;

    private final SatchelData parent;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public SatchelInventory(SatchelData parent) {
        this.parent = parent;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return SatchelStorageRules.canStore(stack);
    }

    @Override
    public void setChanged() {
        parent.getPlayer().getInventory().setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player == parent.getPlayer();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SIZE; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack stack : items) {
            contents.accountSimpleStack(stack);
        }
    }

    public void dropAll(boolean died) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                parent.getPlayer().drop(stack, died, !died);
            }
        }
        setChanged();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            list.add(stack.save(provider, itemTag));
        }

        tag.put("Items", list);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        clearContent();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getInt("Slot");

            if (slot >= 0 && slot < SIZE) {
                ItemStack.parse(provider, itemTag).ifPresent(stack -> items.set(slot, stack));
            }
        }
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public SatchelData getParent() {
        return parent;
    }
}