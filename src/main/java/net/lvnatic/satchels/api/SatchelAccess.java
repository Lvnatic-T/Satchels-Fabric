package net.lvnatic.satchels.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SatchelAccess {
    public static final Set<SatchelEquipCallback> SATCHEL_EQUIP_CALLBACKS = new HashSet<>();
    public static final Set<Predicate<Player>> CAN_ACCESS_PREDICATES = new HashSet<>();
    public static final Set<Predicate<Player>> IS_VISIBLE_PREDICATES = new HashSet<>();
    public static final Set<Function<Player, ItemStack>> SATCHEL_STACK_GETTERS = new HashSet<>();
    public static final Set<Function<Player, Integer>> SATCHEL_TINT_GETTERS = new HashSet<>();

    public static boolean equipSatchelTo(Player player, InteractionHand hand) {
        return SATCHEL_EQUIP_CALLBACKS.stream().anyMatch(callback -> callback.equip(player, hand));
    }

    public static boolean canAccessSatchel(Player player) {
        return CAN_ACCESS_PREDICATES.isEmpty() ||
                CAN_ACCESS_PREDICATES.stream().anyMatch(predicate -> predicate.test(player));
    }

    public static boolean satchelIsVisible(Player player) {
        return canAccessSatchel(player) && (
                IS_VISIBLE_PREDICATES.isEmpty() ||
                        IS_VISIBLE_PREDICATES.stream().allMatch(predicate -> predicate.test(player))
        );
    }

    public static ItemStack getSatchelStack(Player player) {
        for (Function<Player, ItemStack> getter : SATCHEL_STACK_GETTERS) {
            ItemStack stack = getter.apply(player);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static int getSatchelTint(Player player) {
        for (Function<Player, Integer> getter : SATCHEL_TINT_GETTERS) {
            int tint = getter.apply(player);
            if (tint != -1) return tint;
        }
        return -1;
    }

    @FunctionalInterface
    public interface SatchelEquipCallback {
        boolean equip(Player player, InteractionHand hand);
    }
}