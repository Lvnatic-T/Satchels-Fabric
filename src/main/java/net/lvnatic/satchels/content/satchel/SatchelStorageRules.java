package net.lvnatic.satchels.content.satchel;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.Satchels;

public final class SatchelStorageRules {
    public static final TagKey<Item> STORAGE_BLACKLIST = TagKey.create(
            Registries.ITEM,
            Satchels.at("storage_blacklist")
    );

    private SatchelStorageRules() {
    }

    public static boolean canStore(ItemStack stack) {
        return stack.isEmpty() || !stack.is(STORAGE_BLACKLIST);
    }
}