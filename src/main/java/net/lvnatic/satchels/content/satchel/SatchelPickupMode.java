package net.lvnatic.satchels.content.satchel;

public enum SatchelPickupMode {
    OFF,
    OVERFLOW,
    PRIORITIZE;

    public static SatchelPickupMode fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : OVERFLOW;
    }
}