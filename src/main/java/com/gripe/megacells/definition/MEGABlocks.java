package com.gripe.megacells.definition;

import ae2.block.AEBaseBlockItem;
import ae2.block.crafting.CraftingBlockItem;
import ae2.block.crafting.CraftingUnitBlock;
import ae2.block.misc.AEDecorativeBlock;
import ae2.block.networking.EnergyCellBlock;
import ae2.block.networking.EnergyCellBlockItem;
import ae2.core.definitions.BlockDefinition;
import com.gripe.megacells.MEGACells;
import com.gripe.megacells.block.MEGACraftingUnitDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MEGABlocks {
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();

    public static final BlockDefinition<AEDecorativeBlock> SKY_STEEL_BLOCK = block(
        "Sky Steel Block",
        "sky_steel_block",
        () -> new AEDecorativeBlock(Material.IRON, 5.0F, 12.0F),
        AEBaseBlockItem::new);
    public static final BlockDefinition<AEDecorativeBlock> SKY_BRONZE_BLOCK = block(
        "Sky Bronze Block",
        "sky_bronze_block",
        () -> new AEDecorativeBlock(Material.IRON, 3.0F, 12.0F),
        AEBaseBlockItem::new);

    public static final BlockDefinition<EnergyCellBlock> MEGA_ENERGY_CELL = block(
        "Superdense Energy Cell",
        "mega_energy_cell",
        () -> new EnergyCellBlock(12800000, 3200, 12800),
        EnergyCellBlockItem::new);

    public static final BlockDefinition<CraftingUnitBlock> MEGA_CRAFTING_UNIT = craftingUnit(
        "MEGA Crafting Unit", "mega_crafting_unit", MEGACraftingUnitDefinition.UNIT);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_ACCELERATOR = craftingUnit(
        "16x Crafting Co-Processing Unit", "mega_crafting_accelerator", MEGACraftingUnitDefinition.ACCELERATOR);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_1M = craftingUnit(
        "1M MEGA Crafting Storage", "1m_crafting_storage", MEGACraftingUnitDefinition.STORAGE_1M);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_4M = craftingUnit(
        "4M MEGA Crafting Storage", "4m_crafting_storage", MEGACraftingUnitDefinition.STORAGE_4M);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_16M = craftingUnit(
        "16M MEGA Crafting Storage", "16m_crafting_storage", MEGACraftingUnitDefinition.STORAGE_16M);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_64M = craftingUnit(
        "64M MEGA Crafting Storage", "64m_crafting_storage", MEGACraftingUnitDefinition.STORAGE_64M);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_256M = craftingUnit(
        "256M MEGA Crafting Storage", "256m_crafting_storage", MEGACraftingUnitDefinition.STORAGE_256M);

    private MEGABlocks() {
    }

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (var definition : BLOCKS) {
            definition.block().setCreativeTab(MEGACreativeTab.TAB);
            registry.register(definition.block());
        }
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (var definition : BLOCKS) {
            if (definition.item() != null) {
                definition.item().setCreativeTab(MEGACreativeTab.TAB);
            }
            registry.register(definition.item());
        }
    }

    private static BlockDefinition<CraftingUnitBlock> craftingUnit(
        String englishName, String id, MEGACraftingUnitDefinition definition) {
        return block(englishName, id, () -> new CraftingUnitBlock(definition), CraftingBlockItem::new);
    }

    private static <T extends Block> BlockDefinition<T> block(
        String englishName,
        String id,
        Supplier<T> blockSupplier,
        Function<T, ItemBlock> itemFactory) {
        var definition = new BlockDefinition<>(englishName, MEGACells.makeId(id), blockSupplier.get(), itemFactory);
        BLOCKS.add(definition);
        return definition;
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (var definition : BLOCKS) {
            if (definition.item() != null) {
                ModelLoader.setCustomModelResourceLocation(definition.item(), 0, new ModelResourceLocation(definition.id(), "inventory"));
            }
        }
    }
}
