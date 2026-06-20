package com.gripe.megacells.item.cell;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ae2.api.stacks.AEKeyType;
import ae2.container.GuiIds.GuiKey;
import ae2.items.storage.StorageTier;
import ae2.items.tools.powered.PortableCellItem;

import com.gripe.megacells.MEGACells;

public class MEGAPortableCell extends PortableCellItem {
    public MEGAPortableCell(StorageTier tier, AEKeyType keyType, GuiKey gui, int defaultColour) {
        super(keyType, totalTypes(tier, keyType), gui, tier, 0.5, defaultColour);
        setMaxStackSize(1);
    }

    @Override
    public double getIdleDrain() {
        return 1.0;
    }

    @Override
    public ResourceLocation getRecipeId() {
        return MEGACells.makeId(
                "cells/portable/" + Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return super.getChargeRate(stack) * 2;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return super.getAEMaxPower(stack) * 8;
    }

    private static int totalTypes(StorageTier tier, AEKeyType keyType) {
        return 18 + (keyType.equals(AEKeyType.items()) ? (tier.index() - 5) * 9 : 0);
    }
}
