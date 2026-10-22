
package net.lvnatic.satchels.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.network.packets.SatchelOffsetUpdatePacketC2S;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class SatchelsClientConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("satchels-client.toml");
    private static final Path LEGACY_FILE = FabricLoader.getInstance().getConfigDir().resolve("satchels-client.properties");

    private static int satchelOffset = 0;
    private static boolean shiftSwap = true;
    private static boolean guiAnimation = true;

    public static void load() {
        satchelOffset = 0;
        shiftSwap = true;
        guiAnimation = true;

        if (!Files.exists(FILE)) {
            loadLegacyOffset();
            saveNew();
            return;
        }

        try {
            String section = "";

            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String value = stripComment(line).trim();

                if (value.isEmpty()) continue;

                if (value.startsWith("[") && value.endsWith("]")) {
                    section = value.substring(1, value.length() - 1).trim();
                    continue;
                }

                int separator = value.indexOf('=');
                if (separator < 0) continue;

                String key = value.substring(0, separator).trim();
                String setting = value.substring(separator + 1).trim();

                switch (section + "." + key) {
                    case "gameplay.satchel_offset" -> {
                        try {
                            satchelOffset = Math.clamp(Integer.parseInt(setting), 0, 3);
                        } catch (NumberFormatException ignored) {
                            satchelOffset = 0;
                        }
                    }
                    case "gameplay.shift_swap" -> {
                        if (setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("false")) {
                            shiftSwap = Boolean.parseBoolean(setting);
                        }
                    }
                    case "rendering.gui_animation" -> {
                        if (setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("false")) {
                            guiAnimation = Boolean.parseBoolean(setting);
                        }
                    }
                }
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

    private static void loadLegacyOffset() {
        if (!Files.exists(LEGACY_FILE)) return;

        Properties properties = new Properties();

        try (var input = Files.newInputStream(LEGACY_FILE)) {
            properties.load(input);
            satchelOffset = Math.clamp(
                    Integer.parseInt(properties.getProperty("satchel_offset", "0")),
                    0,
                    3
            );
        } catch (IOException | NumberFormatException ignored) {
            satchelOffset = 0;
        }
    }

    private static void saveNew() {
        String content = """
                [gameplay]
                satchel_offset = %d
                shift_swap = %s

                [rendering]
                gui_animation = %s
                """.formatted(satchelOffset, shiftSwap, guiAnimation);

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
            List<String> lines = new ArrayList<>(
                    Files.readAllLines(FILE, StandardCharsets.UTF_8)
            );

            int sectionStart = -1;
            int sectionEnd = lines.size();

            for (int i = 0; i < lines.size(); i++) {
                String line = stripComment(lines.get(i)).trim();

                if (!line.startsWith("[") || !line.endsWith("]")) continue;

                if (sectionStart >= 0) {
                    sectionEnd = i;
                    break;
                }

                if (line.equals("[" + section + "]")) {
                    sectionStart = i;
                }
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

                    String comment = commentStart >= 0
                            ? original.substring(commentStart).trim()
                            : "";

                    lines.set(
                            i,
                            key + " = " + value + (comment.isEmpty() ? "" : " " + comment)
                    );

                    Files.write(FILE, lines, StandardCharsets.UTF_8);
                    return;
                }

                int insertAt = sectionEnd;

                while (insertAt > sectionStart + 1 && lines.get(insertAt - 1).isBlank()) {
                    insertAt--;
                }

                lines.add(insertAt, key + " = " + value);
            } else {
                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
                    lines.add("");
                }

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