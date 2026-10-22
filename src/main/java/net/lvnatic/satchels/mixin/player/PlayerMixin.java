package net.lvnatic.satchels.mixin.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.lvnatic.satchels.content.satchel.IHaveSatchelData;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelStorageRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IHaveSatchelData {
    @Unique
    private SatchelData satchels$data;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public SatchelData satchels$getSatchelData() {
        if (satchels$data == null) {
            satchels$data = new SatchelData((Player) (Object) this);
        }
        return satchels$data;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void satchels$saveData(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        tag.put(SatchelData.KEY_SATCHEL, satchels$getSatchelData().serializeNBT(player.registryAccess()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void satchels$loadData(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains(SatchelData.KEY_SATCHEL)) return;

        Player player = (Player) (Object) this;
        satchels$getSatchelData().deserializeNBT(
                player.registryAccess(),
                tag.getCompound(SatchelData.KEY_SATCHEL)
        );
    }

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void satchels$setSelectedItem(EquipmentSlot equipmentSlot, ItemStack stack, CallbackInfo ci) {
        if (equipmentSlot != EquipmentSlot.MAINHAND) return;

        Player player = (Player) (Object) this;
        SatchelData data = satchels$getSatchelData();

        if (!data.isActive() || !data.canAccess()) return;

        int selected = player.getInventory().selected;
        if (!data.isSlotInSatchel(selected)) return;

        SatchelInventory inventory = data.getSatchelInventory();
        int slot = data.convertToSatchelIndex(selected);
        ItemStack previous = inventory.getItem(slot);

        if (!SatchelStorageRules.canStore(stack)) {
            boolean existingItem = !previous.isEmpty()
                    && previous.is(stack.getItem())
                    && stack.getCount() <= previous.getCount();

            if (!existingItem) {
                inventory.setItem(slot, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(stack.copy());
                this.onEquipItem(equipmentSlot, previous, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
        }

        inventory.setItem(slot, stack);
        this.onEquipItem(equipmentSlot, previous, stack);
        ci.cancel();
    }
}