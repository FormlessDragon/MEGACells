package com.gripe.megacells.item.cell;

import java.util.List;

import net.minecraft.item.ItemStack;

import ae2.api.stacks.GenericStack;

public final class PortableCellWorkbenchTooltipComponent {
    private final List<GenericStack> config;
    private final ItemStack cell;
    private final boolean hasMoreConfig;

    public PortableCellWorkbenchTooltipComponent(List<GenericStack> config, ItemStack cell, boolean hasMoreConfig) {
        this.config = config;
        this.cell = cell;
        this.hasMoreConfig = hasMoreConfig;
    }

    public List<GenericStack> config() {
        return config;
    }

    public ItemStack cell() {
        return cell;
    }

    public boolean hasMoreConfig() {
        return hasMoreConfig;
    }
}
