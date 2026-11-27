package net.lvnatic.satchels.content.satchel;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.lvnatic.satchels.ModSounds;
import net.lvnatic.satchels.api.SatchelAccess;

import java.util.ArrayList;
import java.util.List;

public final class SatchelData {
    public static final String KEY_SATCHEL = "satchels:satchel_data";

    private final Player player;
    private final SatchelInventory satchelInventory;
    private ItemStack boundStack = ItemStack.EMPTY;
    private List<ItemStack> savedSnapshot = List.of();
    private CompoundTag legacyInventory;
    private int hotbarOffset;
    private boolean active;
    private SatchelPickupMode pickupMode = SatchelPickupMode.OVERFLOW;

    public SatchelData(Player player) {
        this.player = player;
        this.satchelInventory = new SatchelInventory(this);
    }

    public static SatchelData get(Player player) {
        return ((IHaveSatchelData) player).satchels$getSatchelData();
    }

    public Player getPlayer() {
        return player;
    }

    public SatchelInventory getSatchelInventory() {
        return satchelInventory;
    }

    public boolean canAccess() {
        return SatchelAccess.canAccessSatchel(player);
    }

    public int getHotbarOffset() {
        return hotbarOffset;
    }

    public void setHotbarOffset(int offset) {
        hotbarOffset = Math.clamp(offset, 0, 3);

        if (player.inventoryMenu != null) {
            player.inventoryMenu.slots.forEach(slot -> {
                if (slot instanceof SatchelInventorySlot satchelSlot) satchelSlot.updateX();
            });
        }

        if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
            player.containerMenu.slots.forEach(slot -> {
                if (slot instanceof SatchelInventorySlot satchelSlot) satchelSlot.updateX();
            });
        }
    }

    public boolean isSlotInSatchel(int slot) {
        return slot >= hotbarOffset && slot < hotbarOffset + SatchelInventory.SIZE;
    }

    public int convertToSatchelIndex(int slot) {
        return slot - hotbarOffset;
    }

    public SatchelPickupMode getPickupMode() {
        return pickupMode;
    }

    public void setPickupMode(SatchelPickupMode mode) {
        pickupMode = mode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean value, boolean audible) {
        active = value;
        if (!audible) return;

        float pitch = 0.9f + player.getRandom().nextFloat() / 5;
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                value ? ModSounds.SATCHEL_OPEN : ModSounds.SATCHEL_CLOSE,
                SoundSource.PLAYERS,
                1.0f,
                pitch
        );
    }

    public void bind(ItemStack stack) {
        if (player.level().isClientSide() || stack.isEmpty()) return;

        if (boundStack == stack) {
            saveToBoundStack();
            return;
        }

        saveToBoundStack();
        boundStack = ItemStack.EMPTY;
        savedSnapshot = List.of();

        if (legacyInventory != null && !stack.has(DataComponents.CONTAINER)) {
            satchelInventory.deserializeNBT(player.registryAccess(), legacyInventory);
            legacyInventory = null;
            boundStack = stack;
            saveToBoundStack();
            return;
        }

        satchelInventory.loadFrom(stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        boundStack = stack;
        savedSnapshot = copyItems();
    }

    public void unbind() {
        if (player.level().isClientSide()) return;

        saveToBoundStack();
        boundStack = ItemStack.EMPTY;
        savedSnapshot = List.of();
        satchelInventory.loadFrom(ItemContainerContents.EMPTY);
    }

    public void onInventoryChanged() {
        player.getInventory().setChanged();
        saveToBoundStack();
    }

    private void saveToBoundStack() {
        if (player.level().isClientSide() || boundStack.isEmpty() || matchesSnapshot()) return;

        if (satchelInventory.isEmpty()) {
            boundStack.remove(DataComponents.CONTAINER);
        } else {
            boundStack.set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(satchelInventory.getItems())
            );
        }

        savedSnapshot = copyItems();
    }

    private boolean matchesSnapshot() {
        List<ItemStack> items = satchelInventory.getItems();
        if (savedSnapshot.size() != items.size()) return false;

        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.matches(savedSnapshot.get(i), items.get(i))) return false;
        }

        return true;
    }

    private List<ItemStack> copyItems() {
        List<ItemStack> snapshot = new ArrayList<>(SatchelInventory.SIZE);
        for (ItemStack stack : satchelInventory.getItems()) snapshot.add(stack.copy());
        return snapshot;
    }

    public void copyFrom(SatchelData original) {
        active = original.active;
        pickupMode = original.pickupMode;
        legacyInventory = original.legacyInventory == null ? null : original.legacyInventory.copy();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Active", active);
        if (legacyInventory != null) tag.put("LegacyInventory", legacyInventory.copy());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        active = tag.getBoolean("Active");
        legacyInventory = null;

        String key = tag.contains("LegacyInventory", Tag.TAG_COMPOUND)
                ? "LegacyInventory"
                : "Inventory";

        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag oldInventory = tag.getCompound(key);
            if (!oldInventory.getList("Items", Tag.TAG_COMPOUND).isEmpty()) {
                legacyInventory = oldInventory.copy();
            }
        }
    }

    public void dropLegacyContents() {
        if (legacyInventory == null || player.level().isClientSide()) return;

        ListTag list = legacyInventory.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(player.registryAccess(), list.getCompound(i))
                    .ifPresent(stack -> player.drop(stack, true, false));
        }
        legacyInventory = null;
    }
}