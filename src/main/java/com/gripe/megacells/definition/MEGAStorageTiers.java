package com.gripe.megacells.definition;

import ae2.core.definitions.ItemDefinition;
import ae2.items.materials.StorageComponentItem;
import ae2.items.storage.StorageTier;

import static com.gripe.megacells.definition.MEGAItems.CELL_COMPONENT_16M;
import static com.gripe.megacells.definition.MEGAItems.CELL_COMPONENT_1M;
import static com.gripe.megacells.definition.MEGAItems.CELL_COMPONENT_256M;
import static com.gripe.megacells.definition.MEGAItems.CELL_COMPONENT_4M;
import static com.gripe.megacells.definition.MEGAItems.CELL_COMPONENT_64M;

public final class MEGAStorageTiers {

    public static final StorageTier TIER_1M = tier(6, "1m", CELL_COMPONENT_1M);
    public static final StorageTier TIER_4M = tier(7, "4m", CELL_COMPONENT_4M);
    public static final StorageTier TIER_16M = tier(8, "16m", CELL_COMPONENT_16M);
    public static final StorageTier TIER_64M = tier(9, "64m", CELL_COMPONENT_64M);
    public static final StorageTier TIER_256M = tier(10, "256m", CELL_COMPONENT_256M);

    private static StorageTier tier(int index, String namePrefix, ItemDefinition<StorageComponentItem> component) {
        var bytes = 1024 * (int) Math.pow(4, index - 1);
        return new StorageTier(index, namePrefix, bytes, 0.5 * index, component::asItem);
    }

}
