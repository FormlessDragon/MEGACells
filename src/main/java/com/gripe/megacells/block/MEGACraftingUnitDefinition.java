package com.gripe.megacells.block;

import ae2.api.crafting.cpu.CraftingUnitVisualDefinition;
import ae2.api.crafting.cpu.CraftingUnitVisualKind;
import ae2.api.crafting.cpu.ICraftingUnitDefinition;
import ae2.core.AppEng;
import ae2.core.definitions.BlockDefinition;
import ae2.core.registries.CraftingUnitRegistry;
import com.gripe.megacells.MEGACells;
import com.gripe.megacells.definition.MEGABlocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Objects;

public enum MEGACraftingUnitDefinition implements ICraftingUnitDefinition {
    UNIT("mega_crafting_unit", 0, 0, "unit", CraftingUnitVisualKind.UNIT),
    ACCELERATOR("mega_crafting_accelerator", 0, 16, "accelerator", CraftingUnitVisualKind.LIGHT),
    STORAGE_1M("1m_crafting_storage", 1, 0, "1m_storage", CraftingUnitVisualKind.LIGHT),
    STORAGE_4M("4m_crafting_storage", 4, 0, "4m_storage", CraftingUnitVisualKind.LIGHT),
    STORAGE_16M("16m_crafting_storage", 16, 0, "16m_storage", CraftingUnitVisualKind.LIGHT),
    STORAGE_64M("64m_crafting_storage", 64, 0, "64m_storage", CraftingUnitVisualKind.LIGHT),
    STORAGE_256M("256m_crafting_storage", 256, 0, "256m_storage", CraftingUnitVisualKind.LIGHT);

    private static final ResourceLocation FAMILY_ID = AppEng.makeId("crafting_cpu");

    private final String blockId;
    private final int storageMb;
    private final int acceleratorThreads;
    private final String visualName;
    private final CraftingUnitVisualDefinition visualDefinition;

    MEGACraftingUnitDefinition(
        String blockId,
        int storageMb,
        int acceleratorThreads,
        String visualName,
        CraftingUnitVisualKind visualKind) {
        this.blockId = blockId;
        this.storageMb = storageMb;
        this.acceleratorThreads = acceleratorThreads;
        this.visualName = visualName;
        this.visualDefinition = createVisualDefinition(visualKind, visualName);
    }

    public static void registerAll() {
        for (var definition : values()) {
            CraftingUnitRegistry.getInstance().register(definition);
        }
    }

    private static CraftingUnitVisualDefinition createVisualDefinition(
        CraftingUnitVisualKind visualKind, String visualName) {
        var builder = CraftingUnitVisualDefinition.builder(
                                                      visualKind,
                                                      MEGACells.makeId("block/crafting/" + visualName),
                                                      MEGACells.makeId("block/crafting/" + visualName + "_formed"))
                                                  .ringTextures(
                                                      MEGACells.makeId("block/crafting/ring_corner"),
                                                      MEGACells.makeId("block/crafting/ring_side_hor"),
                                                      MEGACells.makeId("block/crafting/ring_side_ver"));

        if (visualKind == CraftingUnitVisualKind.UNIT) {
            return builder.baseTexture(MEGACells.makeId("block/crafting/unit_base")).build();
        }

        return builder
            .baseTexture(MEGACells.makeId("block/crafting/light_base"))
            .lightTexture(MEGACells.makeId("block/crafting/" + visualName + "_light"))
            .build();
    }

    public static MEGACraftingUnitDefinition[] storageDefinitions() {
        return Arrays.stream(values())
                     .filter(definition -> definition.storageMb > 0)
                     .toArray(MEGACraftingUnitDefinition[]::new);
    }

    public String blockId() {
        return blockId;
    }

    public String visualName() {
        return visualName;
    }

    @Override
    public ResourceLocation id() {
        return MEGACells.makeId(blockId);
    }

    @Override
    public long storageBytes() {
        return 1024L * 1024 * storageMb;
    }

    @Override
    public int acceleratorThreads() {
        return acceleratorThreads;
    }

    @Override
    public Item getItemRepresentation() {
        var item = blockDefinition().asItem();
        return Objects.requireNonNull(item, "Missing item representation for " + id());
    }

    @Override
    public CraftingUnitVisualDefinition getVisualDefinition() {
        return visualDefinition;
    }

    @Override
    public ResourceLocation getFamilyId() {
        return FAMILY_ID;
    }

    public BlockDefinition<?> blockDefinition() {
        return switch (this) {
            case UNIT -> MEGABlocks.MEGA_CRAFTING_UNIT;
            case ACCELERATOR -> MEGABlocks.CRAFTING_ACCELERATOR;
            case STORAGE_1M -> MEGABlocks.CRAFTING_STORAGE_1M;
            case STORAGE_4M -> MEGABlocks.CRAFTING_STORAGE_4M;
            case STORAGE_16M -> MEGABlocks.CRAFTING_STORAGE_16M;
            case STORAGE_64M -> MEGABlocks.CRAFTING_STORAGE_64M;
            case STORAGE_256M -> MEGABlocks.CRAFTING_STORAGE_256M;
        };
    }
}
