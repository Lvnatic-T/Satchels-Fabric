package net.lvnatic.satchels.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.lvnatic.satchels.content.satchel.SatchelData;

@Mixin(
        targets = "net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem",
        remap = false
)
public abstract class SophisticatedBackpacksItemMixin {
    @ModifyArgs(
            method = {"use", "method_7836"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/p3pp3rf1y/sophisticatedbackpacks/common/gui/BackpackContext$Item;<init>(Ljava/lang/String;I)V"
            ),
            remap = false,
            require = 1
    )
    private void satchels$correctBackpackContext(
            Args args,
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (hand != InteractionHand.MAIN_HAND) return;

        SatchelData data = SatchelData.get(player);

        if (!data.canAccess() || !data.isActive()) return;

        int selected = player.getInventory().selected;

        if (!data.isSlotInSatchel(selected)) return;

        args.set(0, "satchels");
        args.set(1, data.convertToSatchelIndex(selected));
    }
}
