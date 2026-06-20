package com.gripe.megacells.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import ae2.api.client.StorageCellModels;
import ae2.block.networking.EnergyCellBlockItem;
import ae2.core.AppEng;
import ae2.items.storage.BasicStorageCell;
import ae2.items.tools.powered.PortableCellItem;

import com.gripe.megacells.MEGACells;
import com.gripe.megacells.definition.MEGABlocks;
import com.gripe.megacells.definition.MEGAItems;

@SideOnly(Side.CLIENT)
public final class MEGACellsClient {
    private MEGACellsClient() {}

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerEnergyCellFillLevel();
        registerStorageCellModels();
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        List<Item> standardCells = new ArrayList<>();
        List<Item> portableCells = new ArrayList<>();

        for (var cell : MEGAItems.getTieredCells()) {
            (cell.portable() ? portableCells : standardCells).add(cell.item().asItem());
        }

        standardCells.add(MEGAItems.BULK_ITEM_CELL.asItem());

        IItemColor standardCellColor = BasicStorageCell::getColor;
        IItemColor portableCellColor = PortableCellItem::getColor;

        event.getItemColors().registerItemColorHandler(standardCellColor, standardCells.toArray(new Item[0]));
        event.getItemColors().registerItemColorHandler(portableCellColor, portableCells.toArray(new Item[0]));
    }

    private static void registerEnergyCellFillLevel() {
        MEGABlocks.MEGA_ENERGY_CELL.asItem()
                .addPropertyOverride(AppEng.makeId("fill_level"), (stack, world, entity) -> {
                    var energyCell = (EnergyCellBlockItem) MEGABlocks.MEGA_ENERGY_CELL.asItem();
                    double maxPower = energyCell.getAEMaxPower(stack);

                    if (maxPower <= 0) {
                        return 0;
                    }

                    return (float) (energyCell.getAECurrentPower(stack) / maxPower);
                });
    }

    private static void registerStorageCellModels() {
        String modelPrefix = "block/drive/cells/";

        for (var cell : MEGAItems.getTieredCells()) {
            StorageCellModels.registerModel(
                    cell.item().asItem(),
                    MEGACells.makeId(modelPrefix + cell.tier().namePrefix() + "_" + cell.keyType() + "_cell"));
        }

        StorageCellModels.registerModel(
                MEGAItems.BULK_ITEM_CELL.asItem(),
                MEGACells.makeId(modelPrefix + MEGAItems.BULK_ITEM_CELL.id().getPath()));
    }

    public static void initScreens() {
        // Registered from the 1.12 GUI handler once menu migration is complete.
    }

    public static Minecraft minecraft() {
        return Minecraft.getMinecraft();
    }
}
