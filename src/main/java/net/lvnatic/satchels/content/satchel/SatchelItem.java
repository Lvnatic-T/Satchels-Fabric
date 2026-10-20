package net.lvnatic.satchels.content.satchel;

import io.wispforest.accessories.api.AccessoryItem;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.lvnatic.satchels.ModSounds;
import net.lvnatic.satchels.api.SatchelAccess;
import net.lvnatic.satchels.network.packets.SatchelStatusPacketS2C;

public final class SatchelItem extends AccessoryItem {
    public static final int DEFAULT_COLOR = 0xffaf5d2e;

    public SatchelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (SatchelAccess.equipSatchelTo(player, hand)) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference reference) {
        if (!super.canUnequip(stack, reference)) return false;

        if (reference.entity() instanceof Player player) {
            return SatchelData.get(player).getSatchelInventory().isEmpty();
        }

        return true;
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        SatchelData data = SatchelData.get(player);
        if (!data.isActive()) return;

        data.setActive(false, false);
        ServerPlayNetworking.send(player, new SatchelStatusPacketS2C(false));
    }

    @Override
    public void onEquipFromUse(ItemStack stack, SlotReference reference) {
        float pitch = 0.9f + reference.entity().getRandom().nextFloat() / 5;
        reference.entity().playSound(ModSounds.SATCHEL_EQUIP, 1.0f, pitch);
    }

    public static void playEquipSound(Player player) {
        float pitch = 0.9f + player.getRandom().nextFloat() / 5;

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.SATCHEL_EQUIP,
                SoundSource.PLAYERS,
                1.0f,
                pitch
        );
    }
}