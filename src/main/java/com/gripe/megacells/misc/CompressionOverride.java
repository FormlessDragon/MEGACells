package com.gripe.megacells.misc;

import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public record CompressionOverride(ItemStack larger, ItemStack smaller) {
    public CompressionOverride(ItemStack larger, ItemStack smaller) {
        this.larger = larger.copy();
        this.smaller = smaller.copy();
    }

    @Override
    public ItemStack larger() {
        return larger.copy();
    }

    @Override
    public ItemStack smaller() {
        return smaller.copy();
    }

    @Override
    public @NonNull String toString() {
        return "CompressionOverride{larger=" + larger + ", smaller=" + smaller + '}';
    }
}
