package net.lvnatic.satchels.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelPickupMode;
import net.lvnatic.satchels.network.packets.SatchelOffsetUpdatePacketC2S;
import net.lvnatic.satchels.network.packets.SatchelPickupModePacketC2S;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class SatchelsClientConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("satchels-client.toml");
    private static final Path LEGACY_FILE = FabricLoader.getInstance().getConfigDir().resolve("satchels-client.properties");

    private static int satchelOffset = 0;
    private static boolean shiftSwap = true;
    private static boolean guiAnimation = true;
    private static int pickupMode = 1;
    private static int animationDuration = 300;
    private static boolean toggleMessages = true;

    public static void load() {
        satchelOffset = 0;
        shiftSwap = true;
        guiAnimation = true;
        pickupMode = 1;
        animationDuration = 300;
        toggleMessages = true;

        if (!Files.exists(FILE)) {
            loadLegacyOffset();
            saveNew();
            return;
        }

        try {
            String section = "";
            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
            List<String> retainedLines = new ArrayList<>(lines.size());

            for (String line : lines) {
                String value = stripComment(line).trim();
                if (value.isEmpty()) {
                    retainedLines.add(line);
                    continue;
                }

                if (value.startsWith("[") && value.endsWith("]")) {
                    section = value.substring(1, value.length() - 1).trim();
                    retainedLines.add(line);
                    continue;
                }

                int separator = value.indexOf('=');
                if (separator < 0) {
                    retainedLines.add(line);
                    continue;
                }

                String key = value.substring(0, separator).trim();
                if (section.equals("rendering") && key.equals("hotbar_opacity_percent")) continue;
                retainedLines.add(line);
                String setting = value.substring(separator + 1).trim();

                switch (section + "." + key) {
                    case "gameplay.satchel_offset" -> satchelOffset = readInt(setting, 0, 0, 3);
                    case "gameplay.shift_swap" -> shiftSwap = readBoolean(setting, shiftSwap);
                    case "gameplay.toggle_messages" -> toggleMessages = readBoolean(setting, toggleMessages);
                    case "gameplay.pickup_mode" -> {
                        String mode = setting.replace("\"", "").replace("'", "").toLowerCase(Locale.ROOT);
                        pickupMode = switch (mode) {
                            case "off" -> 0;
                            case "prioritize" -> 2;
                            default -> 1;
                        };
                    }
                    case "rendering.gui_animation" -> guiAnimation = readBoolean(setting, guiAnimation);
                    case "rendering.animation_duration_ms" -> animationDuration = 50 * Math.clamp(
                            Math.round(readInt(setting, 300, 100, 1000) / 50.0f), 2, 20);
                }
            }
            if (retainedLines.size() != lines.size()) {
                Files.write(FILE, retainedLines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Satchels client configuration", e);
        }
    }

    public static int getSatchelOffset() {
        return satchelOffset;
    }

    public static boolean shouldSwapWithShiftKey() {
        return shiftSwap;
    }

    public static boolean shouldAnimateGUI() {
        return guiAnimation;
    }

    public static int getPickupModeIndex() {
        return pickupMode;
    }

    public static int getAnimationDuration() {
        return animationDuration;
    }

    public static boolean shouldShowToggleMessages() {
        return toggleMessages;
    }

    public static void updateOffset(int value) {
        int offset = Math.clamp(value, 0, 3);
        if (satchelOffset == offset) return;

        satchelOffset = offset;
        saveSetting("gameplay", "satchel_offset", Integer.toString(offset));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        SatchelData.get(minecraft.player).setHotbarOffset(offset);
        if (minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new SatchelOffsetUpdatePacketC2S(offset));
        }
    }

    public static void updateShiftSwap(boolean enabled) {
        if (shiftSwap == enabled) return;
        shiftSwap = enabled;
        saveSetting("gameplay", "shift_swap", Boolean.toString(enabled));
    }

    public static void updateGuiAnimation(boolean enabled) {
        if (guiAnimation == enabled) return;
        guiAnimation = enabled;
        saveSetting("rendering", "gui_animation", Boolean.toString(enabled));
    }

    public static void updatePickupMode(int value) {
        int mode = Math.clamp(value, 0, 2);
        if (pickupMode == mode) return;
        pickupMode = mode;
        String name = SatchelPickupMode.fromId(mode).name().toLowerCase(Locale.ROOT);
        saveSetting("gameplay", "pickup_mode", "\"" + name + "\"");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new SatchelPickupModePacketC2S(mode));
        }
    }

    public static void updateAnimationDuration(int units) {
        int duration = Math.clamp(units, 2, 20) * 50;
        if (animationDuration == duration) return;
        animationDuration = duration;
        saveSetting("rendering", "animation_duration_ms", Integer.toString(duration));
    }

    public static void updateToggleMessages(boolean enabled) {
        if (toggleMessages == enabled) return;
        toggleMessages = enabled;
        saveSetting("gameplay", "toggle_messages", Boolean.toString(enabled));
    }

    private static int readInt(String setting, int fallback, int min, int max) {
        try {
            return Math.clamp(Integer.parseInt(setting), min, max);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean readBoolean(String setting, boolean fallback) {
        if (setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(setting);
        }
        return fallback;
    }

    private static void loadLegacyOffset() {
        if (!Files.exists(LEGACY_FILE)) return;

        Properties properties = new Properties();
        try (var input = Files.newInputStream(LEGACY_FILE)) {
            properties.load(input);
            satchelOffset = Math.clamp(
                    Integer.parseInt(properties.getProperty("satchel_offset", "0")), 0, 3);
        } catch (IOException | NumberFormatException ignored) {
            satchelOffset = 0;
        }
    }

    private static void saveNew() {
        String content = """
                [gameplay]
                satchel_offset = %d
                shift_swap = %s
                pickup_mode = "%s"
                toggle_messages = %s

                [rendering]
                gui_animation = %s
                animation_duration_ms = %d
                """.formatted(satchelOffset, shiftSwap,
                SatchelPickupMode.fromId(pickupMode).name().toLowerCase(Locale.ROOT),
                toggleMessages, guiAnimation, animationDuration);

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save Satchels client configuration", e);
        }
    }

    private static void saveSetting(String section, String key, String value) {
        if (!Files.exists(FILE)) {
            saveNew();
            return;
        }

        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(FILE, StandardCharsets.UTF_8));
            int sectionStart = -1;
            int sectionEnd = lines.size();

            for (int i = 0; i < lines.size(); i++) {
                String line = stripComment(lines.get(i)).trim();
                if (!line.startsWith("[") || !line.endsWith("]")) continue;

                if (sectionStart >= 0) {
                    sectionEnd = i;
                    break;
                }
                if (line.equals("[" + section + "]")) sectionStart = i;
            }

            if (sectionStart >= 0) {
                for (int i = sectionStart + 1; i < sectionEnd; i++) {
                    String line = stripComment(lines.get(i)).trim();
                    int separator = line.indexOf('=');
                    if (separator < 0) continue;

                    String existingKey = line.substring(0, separator).trim();
                    if (!existingKey.equals(key)) continue;

                    String original = lines.get(i);
                    int commentStart = original.indexOf('#');
                    String comment = commentStart >= 0 ? original.substring(commentStart).trim() : "";
                    lines.set(i, key + " = " + value + (comment.isEmpty() ? "" : " " + comment));
                    Files.write(FILE, lines, StandardCharsets.UTF_8);
                    return;
                }

                int insertAt = sectionEnd;
                while (insertAt > sectionStart + 1 && lines.get(insertAt - 1).isBlank()) insertAt--;
                lines.add(insertAt, key + " = " + value);
            } else {
                if (!lines.isEmpty() && !lines.getLast().isBlank()) lines.add("");
                lines.add("[" + section + "]");
                lines.add(key + " = " + value);
            }

            Files.write(FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save Satchels client configuration", e);
        }
    }

    private static String stripComment(String line) {
        int comment = line.indexOf('#');
        return comment >= 0 ? line.substring(0, comment) : line;
    }
}