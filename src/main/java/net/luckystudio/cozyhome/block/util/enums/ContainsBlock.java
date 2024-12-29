package net.luckystudio.cozyhome.block.util.enums;

import net.minecraft.util.StringIdentifiable;

public enum ContainsBlock implements StringIdentifiable {
    NONE("none"),
    LAVA("lava"),
    WATER("water"),
    ICE("ice");

    private final String name;

    private ContainsBlock(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
