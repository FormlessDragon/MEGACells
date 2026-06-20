package com.gripe.megacells.misc;

import net.minecraft.item.ItemStack;

public final class CompressionOverride {
    private final ItemStack larger;
    private final ItemStack smaller;

    public CompressionOverride(ItemStack larger, ItemStack smaller) {
        this.larger = larger.copy();
        this.smaller = smaller.copy();
    }

    public ItemStack larger() {
        return larger.copy();
    }

    public ItemStack smaller() {
        return smaller.copy();
    }

    @Override
    public String toString() {
        return "CompressionOverride{larger=" + larger + ", smaller=" + smaller + '}';
    }
}
