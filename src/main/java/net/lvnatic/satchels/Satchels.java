package net.lvnatic.satchels;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.lvnatic.satchels.api.SatchelAccess;
import net.lvnatic.satchels.compat.SophisticatedBackpacksCompat;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.network.ModPackets;

public final class Satchels implements ModInitializer {
    public static final String ID = "satchels";

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModSounds.initialize();
        ModPackets.initialize();

        CauldronInteraction.WATER.map().put(
                ModItems.SATCHEL,
                CauldronInteraction.DYED_ITEM
        );

        SatchelAccess.CAN_ACCESS_PREDICATES.add(player -> {
            AccessoriesCapability capability = AccessoriesCapability.get(player);
            return capability != null && capability.isEquipped(ModItems.SATCHEL);
        });

        SatchelAccess.SATCHEL_STACK_GETTERS.add(player -> {
            AccessoriesCapability capability = AccessoriesCapability.get(player);
            if (capability == null) return ItemStack.EMPTY;

            var equipped = capability.getFirstEquipped(ModItems.SATCHEL);
            return equipped != null ? equipped.stack() : ItemStack.EMPTY;
        });

        if (FabricLoader.getInstance().isModLoaded("sophisticatedbackpacks")) {
            SophisticatedBackpacksCompat.initialize();
        }

        ServerPlayerEvents.COPY_FROM.register((original, current, alive) -> {
            if (alive || original.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                SatchelData.get(current).copyFrom(SatchelData.get(original));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayer player)) return;
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

            SatchelData.get(player).getSatchelInventory().dropAll(true);
        });
    }

    public static ResourceLocation at(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}