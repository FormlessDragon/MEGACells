package com.gripe.megacells.misc;

import net.minecraft.item.ItemStack;

import ae2.api.storage.cells.ICellWorkbenchItem;

public interface CellWorkbenchHost {
    ICellWorkbenchItem getCell();

    ItemStack mega$getContainedStack();

    void saveChanges();
}
