package com.gripe.megacells.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import ae2.api.parts.IPart;
import ae2.api.parts.IPartItem;
import ae2.api.parts.PartModels;
import ae2.api.stacks.AEKeyType;
import ae2.api.upgrades.Upgrades;
import ae2.container.GuiIds;
import ae2.core.definitions.AEItems;
import ae2.core.definitions.ItemDefinition;
import ae2.items.materials.EnergyCardItem;
import ae2.items.materials.MaterialItem;
import ae2.items.materials.StorageComponentItem;
import ae2.items.materials.UpgradeCardItem;
import ae2.items.parts.PartItem;
import ae2.items.parts.PartModelsHelper;
import ae2.items.storage.BasicStorageCell;
import ae2.items.storage.StorageTier;

import com.gripe.megacells.MEGACells;
import com.gripe.megacells.item.cell.BulkCellItem;
import com.gripe.megacells.item.cell.MEGAPortableCell;
import com.gripe.megacells.item.part.CellDockPart;
import com.gripe.megacells.item.part.DecompressionModulePart;

import static com.gripe.megacells.definition.MEGAStorageTiers.*;

@SuppressWarnings("unused")
public final class MEGAItems {
    private static final Logger LOGGER = LoggerFactory.getLogger(MEGAItems.class);

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();
    private static final List<CellDefinition> CELLS = new ArrayList<>();

    public static final ItemDefinition<MaterialItem> SKY_STEEL_INGOT =
            item("Sky Steel Ingot", "sky_steel_ingot", MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SKY_BRONZE_INGOT =
            item("Sky Bronze Ingot", "sky_bronze_ingot", MaterialItem::new);

    public static final ItemDefinition<MaterialItem> ACCUMULATION_PROCESSOR_PRESS =
            item("Inscriber Accumulation Press", "accumulation_processor_press", MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ACCUMULATION_PROCESSOR_PRINT =
            item("Printed Accumulation Circuit", "printed_accumulation_processor", MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ACCUMULATION_PROCESSOR =
            item("Accumulation Processor", "accumulation_processor", MaterialItem::new);

    public static final ItemDefinition<MaterialItem> MEGA_ITEM_CELL_HOUSING =
            item("MEGA Item Cell Housing", "mega_item_cell_housing", MaterialItem::new);
    public static final ItemDefinition<MaterialItem> MEGA_FLUID_CELL_HOUSING =
            item("MEGA Fluid Cell Housing", "mega_fluid_cell_housing", MaterialItem::new);

    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_1M = component(1);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_4M = component(4);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_16M = component(16);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_64M = component(64);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_256M = component(256);

    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_1M = itemCell(TIER_1M);
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_4M = itemCell(TIER_4M);
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_16M = itemCell(TIER_16M);
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_64M = itemCell(TIER_64M);
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_256M = itemCell(TIER_256M);

    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_1M = fluidCell(TIER_1M);
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_4M = fluidCell(TIER_4M);
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_16M = fluidCell(TIER_16M);
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_64M = fluidCell(TIER_64M);
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_256M = fluidCell(TIER_256M);

    public static final ItemDefinition<MEGAPortableCell> PORTABLE_ITEM_CELL_1M = itemPortable(TIER_1M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_ITEM_CELL_4M = itemPortable(TIER_4M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_ITEM_CELL_16M = itemPortable(TIER_16M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_ITEM_CELL_64M = itemPortable(TIER_64M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_ITEM_CELL_256M = itemPortable(TIER_256M);

    public static final ItemDefinition<MEGAPortableCell> PORTABLE_FLUID_CELL_1M = fluidPortable(TIER_1M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_FLUID_CELL_4M = fluidPortable(TIER_4M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_FLUID_CELL_16M = fluidPortable(TIER_16M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_FLUID_CELL_64M = fluidPortable(TIER_64M);
    public static final ItemDefinition<MEGAPortableCell> PORTABLE_FLUID_CELL_256M = fluidPortable(TIER_256M);

    public static final ItemDefinition<EnergyCardItem> GREATER_ENERGY_CARD =
            item("Greater Energy Card", "greater_energy_card", () -> new EnergyCardItem(8));

    public static final ItemDefinition<MaterialItem> BULK_CELL_COMPONENT =
            item("MEGA Bulk Storage Component", "bulk_cell_component", MaterialItem::new);
    public static final ItemDefinition<BulkCellItem> BULK_ITEM_CELL =
            item("MEGA Bulk Item Storage Cell", "bulk_item_cell", BulkCellItem::new);
    public static final ItemDefinition<UpgradeCardItem> COMPRESSION_CARD =
            item("Compression Card", "compression_card", UpgradeCardItem::new);
    public static final ItemDefinition<PartItem<DecompressionModulePart>> DECOMPRESSION_MODULE =
            part("MEGA Decompression Module", "decompression_module", DecompressionModulePart.class, DecompressionModulePart::new);

    public static final ItemDefinition<PartItem<CellDockPart>> CELL_DOCK =
            part("ME Cell Dock", "cell_dock", CellDockPart.class, CellDockPart::new);

    private MEGAItems() {}

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static List<CellDefinition> getTieredCells() {
        return Collections.unmodifiableList(CELLS);
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (var definition : ITEMS) {
            if (definition.asItem() != null) {
                definition.asItem().setCreativeTab(MEGACreativeTab.TAB);
            }
            registry.register(definition.asItem());
        }

        registerPartModels();
    }

    public static void initUpgrades() {
        for (var cell : CELLS) {
            Upgrades.add(AEItems.INVERTER_CARD.asItem(), cell.item().asItem(), 1, "gui.tooltips.appliedenergistics2.StorageCells");
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), cell.item().asItem(), 1, "gui.tooltips.appliedenergistics2.StorageCells");
            Upgrades.add(AEItems.VOID_CARD.asItem(), cell.item().asItem(), 1, "gui.tooltips.appliedenergistics2.StorageCells");

            if (cell.keyType() == AEKeyType.items()) {
                Upgrades.add(AEItems.FUZZY_CARD.asItem(), cell.item().asItem(), 1, "gui.tooltips.appliedenergistics2.StorageCells");
            }

            if (cell.portable()) {
                Upgrades.add(GREATER_ENERGY_CARD.asItem(), cell.item().asItem(), 2, "gui.tooltips.appliedenergistics2.PortableCells");
            }
        }

        Upgrades.add(COMPRESSION_CARD.asItem(), BULK_ITEM_CELL.asItem(), 1);
    }

    private static StorageTier tier(int index, String namePrefix, ItemDefinition<StorageComponentItem> component) {
        var bytes = 1024 * (int) Math.pow(4, index - 1);
        return new StorageTier(index, namePrefix, bytes, 0.5 * index, component::asItem);
    }

    private static ItemDefinition<StorageComponentItem> component(int mb) {
        return item(mb + "M MEGA Storage Component", "cell_component_" + mb + "m", () -> new StorageComponentItem(mb * 1024));
    }

    private static ItemDefinition<BasicStorageCell> itemCell(StorageTier tier) {
        var cell = item(
                tier.namePrefix().toUpperCase() + " MEGA Item Storage Cell",
                "item_storage_cell_" + tier.namePrefix(),
                () -> new BasicStorageCell(tier.idleDrain(), tier.bytes() / 1024, tier.bytes() / 128, 63, AEKeyType.items()));
        CELLS.add(new CellDefinition(cell, tier, CellModelType.fromKeyType(AEKeyType.items()), false));
        return cell;
    }

    private static ItemDefinition<BasicStorageCell> fluidCell(StorageTier tier) {
        var cell = item(
                tier.namePrefix().toUpperCase() + " MEGA Fluid Storage Cell",
                "fluid_storage_cell_" + tier.namePrefix(),
                () -> new BasicStorageCell(tier.idleDrain(), tier.bytes() / 1024, tier.bytes() / 128, 18, AEKeyType.fluids()));
        CELLS.add(new CellDefinition(cell, tier, CellModelType.fromKeyType(AEKeyType.fluids()), false));
        return cell;
    }

    private static ItemDefinition<MEGAPortableCell> itemPortable(StorageTier tier) {
        var cell = item(
                tier.namePrefix().toUpperCase() + " Portable Item Cell",
                "portable_item_cell_" + tier.namePrefix(),
                () -> new MEGAPortableCell(tier, AEKeyType.items(), GuiIds.GuiKey.PORTABLE_ITEM_CELL, 0x80caff));
        CELLS.add(new CellDefinition(cell, tier, CellModelType.fromKeyType(AEKeyType.items()), true));
        return cell;
    }

    private static ItemDefinition<MEGAPortableCell> fluidPortable(StorageTier tier) {
        var cell = item(
                tier.namePrefix().toUpperCase() + " Portable Fluid Cell",
                "portable_fluid_cell_" + tier.namePrefix(),
                () -> new MEGAPortableCell(tier, AEKeyType.fluids(), GuiIds.GuiKey.PORTABLE_FLUID_CELL, 0x80caff));
        CELLS.add(new CellDefinition(cell, tier, CellModelType.fromKeyType(AEKeyType.fluids()), true));
        return cell;
    }

    private static <T extends IPart> ItemDefinition<PartItem<T>> part(
            String englishName,
            String id,
            Class<T> partClass,
            java.util.function.Function<IPartItem<T>, T> factory) {
        return item(englishName, id, () -> new PartItem<>(partClass, factory));
    }

    private static <T extends Item> ItemDefinition<T> item(String englishName, String id, Supplier<T> supplier) {
        var definition = new ItemDefinition<>(englishName, MEGACells.makeId(id), supplier.get());
        ITEMS.add(definition);
        return definition;
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (var definition : ITEMS) {
            ModelLoader.setCustomModelResourceLocation(definition.asItem(), 0, new ModelResourceLocation(definition.id(), "inventory"));
        }
    }

    private static void registerPartModels() {
        for (var definition : ITEMS) {
            if (definition.asItem() instanceof PartItem<?> partItem) {
                PartModels.registerModels(PartModelsHelper.createModels(partItem.getPartClass()));
            }
        }
    }

    public enum CellModelType {
        ITEM(AEKeyType.items(), "item"),
        FLUID(AEKeyType.fluids(), "fluid");

        private final AEKeyType keyType;
        private final String driveModelSuffix;

        CellModelType(AEKeyType keyType, String driveModelSuffix) {
            this.keyType = keyType;
            this.driveModelSuffix = driveModelSuffix;
        }

        public static CellModelType fromKeyType(AEKeyType keyType) {
            for (var modelType : values()) {
                if (modelType.keyType == keyType) {
                    return modelType;
                }
            }

            var keyTypeId = keyType == null ? "<null>" : keyType.getId().toString();
            LOGGER.error("Unsupported storage cell AE key type for MEGA drive cell model: {}", keyTypeId);
            throw new IllegalArgumentException("Unsupported storage cell AE key type for MEGA drive cell model: " + keyTypeId);
        }

        public AEKeyType keyType() {
            return keyType;
        }

        public String driveModelSuffix() {
            return driveModelSuffix;
        }
    }

    public record CellDefinition(ItemDefinition<?> item, StorageTier tier, CellModelType modelType, boolean portable) {
        public AEKeyType keyType() {
            return modelType.keyType();
        }
    }
}
