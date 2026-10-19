package net.lvnatic.satchels.content.satchel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.ModSounds;
import net.lvnatic.satchels.api.SatchelAccess;

import java.util.List;

public final class SatchelData {
    public static final String KEY_SATCHEL = "satchels:satchel_data";

    private final Player player;
    private final SatchelInventory satchelInventory;
    private int hotbarOffset;
    private boolean active;

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

    public void copyFrom(SatchelData original) {
        List<ItemStack> items = original.satchelInventory.getItems();

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            satchelInventory.setItem(i, items.get(i).copy());
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Active", active);
        tag.put("Inventory", satchelInventory.serializeNBT(provider));
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        active = tag.getBoolean("Active");
        satchelInventory.deserializeNBT(provider, tag.getCompound("Inventory"));
    }
}