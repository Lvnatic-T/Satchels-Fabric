package net.lvnatic.satchels;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final SoundEvent SATCHEL_EQUIP = register("satchel_equip");
    public static final SoundEvent SATCHEL_OPEN = register("satchel_open");
    public static final SoundEvent SATCHEL_CLOSE = register("satchel_close");

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                Satchels.at(path),
                SoundEvent.createVariableRangeEvent(Satchels.at(path))
        );
    }

    public static void initialize() {
    }
}