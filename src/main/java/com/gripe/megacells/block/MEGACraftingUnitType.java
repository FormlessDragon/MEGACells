package com.gripe.megacells.block;

import net.minecraft.item.Item;

import ae2.block.crafting.ICraftingUnitType;
import ae2.core.definitions.BlockDefinition;

import com.gripe.megacells.definition.MEGABlocks;

public enum MEGACraftingUnitType implements ICraftingUnitType {
    UNIT(0, "unit"),
    ACCELERATOR(0, "accelerator"),
    STORAGE_1M(1, "1m_storage"),
    STORAGE_4M(4, "4m_storage"),
    STORAGE_16M(16, "16m_storage"),
    STORAGE_64M(64, "64m_storage"),
    STORAGE_256M(256, "256m_storage"),
    MONITOR(0, "monitor");

    private final int storageMb;
    private final String affix;

    MEGACraftingUnitType(int storageMb, String affix) {
        this.storageMb = storageMb;
        this.affix = affix;
    }

    @Override
    public long getStorageBytes() {
        return 1024L * 1024 * storageMb;
    }

    @Override
    public int getAcceleratorThreads() {
        return this == ACCELERATOR ? 4 : 0;
    }

    public String getAffix() {
        return affix;
    }

    public BlockDefinition<?> getDefinition() {
        switch (this) {
            case UNIT:
                return MEGABlocks.MEGA_CRAFTING_UNIT;
            case ACCELERATOR:
                return MEGABlocks.CRAFTING_ACCELERATOR;
            case STORAGE_1M:
                return MEGABlocks.CRAFTING_STORAGE_1M;
            case STORAGE_4M:
                return MEGABlocks.CRAFTING_STORAGE_4M;
            case STORAGE_16M:
                return MEGABlocks.CRAFTING_STORAGE_16M;
            case STORAGE_64M:
                return MEGABlocks.CRAFTING_STORAGE_64M;
            case STORAGE_256M:
                return MEGABlocks.CRAFTING_STORAGE_256M;
            case MONITOR:
                return MEGABlocks.CRAFTING_MONITOR;
            default:
                throw new IllegalStateException("Unknown crafting unit type " + this);
        }
    }

    @Override
    public Item getItemFromType() {
        return getDefinition().asItem();
    }
}
