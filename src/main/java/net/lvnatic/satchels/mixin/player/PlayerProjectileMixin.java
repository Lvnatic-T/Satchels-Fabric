package net.lvnatic.satchels.mixin.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerProjectileMixin {
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void satchels$getProjectile(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!(weapon.getItem() instanceof ProjectileWeaponItem projectileWeapon)) return;

        Player player = (Player) (Object) this;
        SatchelData data = SatchelData.get(player);

        if (!data.canAccess()) return;

        ItemStack held = ProjectileWeaponItem.getHeldProjectile(
                player,
                projectileWeapon.getSupportedHeldProjectiles()
        );

        if (!held.isEmpty()) return;

        Predicate<ItemStack> supported = projectileWeapon.getAllSupportedProjectiles();
        SatchelInventory satchel = data.getSatchelInventory();

        for (int i = 0; i < satchel.getContainerSize(); i++) {
            ItemStack ammunition = satchel.getItem(i);

            if (!ammunition.isEmpty() && supported.test(ammunition)) {
                cir.setReturnValue(ammunition);
                return;
            }
        }
    }
}