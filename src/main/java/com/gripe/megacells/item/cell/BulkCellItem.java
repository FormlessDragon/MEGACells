package com.gripe.megacells.item.cell;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import ae2.api.config.FuzzyMode;
import ae2.api.stacks.AEKeyType;
import ae2.api.storage.StorageCells;
import ae2.api.storage.cells.ICellHandler;
import ae2.api.storage.cells.ICellWorkbenchItem;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.core.localization.Tooltips;
import ae2.items.AEBaseItem;
import ae2.items.contents.CellConfig;
import ae2.util.ConfigInventory;

import com.gripe.megacells.definition.MEGAItems;
import com.gripe.megacells.definition.MEGATranslations;

public class BulkCellItem extends AEBaseItem implements ICellWorkbenchItem {
    private static final ICellHandler HANDLER = new Handler();

    public BulkCellItem() {
        setMaxStackSize(1);
    }

    public static void registerHandler() {
        StorageCells.addCellHandler(HANDLER);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(AEKeyType.items()), is, 1);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 1);
    }

    public boolean hasCompressionCard(ItemStack is) {
        Item compressionCard = MEGAItems.COMPRESSION_CARD.asItem();
        return getUpgrades(is).isInstalled(compressionCard);
    }

    @Override
    protected void addCheckedInformation(ItemStack is, World world, List<String> lines, ITooltipFlag flag) {
        BulkCellInventory inv = (BulkCellInventory) HANDLER.getCellInventory(is, null);

        if (inv == null) {
            return;
        }

        if (inv.getStoredItem() != null) {
            addLine(lines, MEGATranslations.Contains.text(inv.getStoredItem().getDisplayName()));

            long quantity = inv.getStoredQuantity();
            addLine(
                    lines,
                    MEGATranslations.Quantity.text(
                            quantity < Long.MAX_VALUE ? Tooltips.ofUnformattedNumber(quantity) : MEGATranslations.ALot.text()));
        } else {
            addLine(lines, MEGATranslations.Empty.text());
        }

        if (inv.getFilterItem() != null) {
            if (inv.getStoredItem() == null) {
                addLine(lines, MEGATranslations.PartitionedFor.text(inv.getFilterItem().getDisplayName()));
            } else if (!inv.getStoredItem().equals(inv.getFilterItem())) {
                addLine(lines, MEGATranslations.MismatchedFilter.text(inv.getFilterItem().getDisplayName()), TextFormatting.DARK_RED);
            }
        } else if (inv.getStoredItem() != null) {
            addLine(lines, MEGATranslations.MismatchedFilter.text(MEGATranslations.Empty.text()), TextFormatting.DARK_RED);
        } else {
            addLine(lines, MEGATranslations.NotPartitioned.text());
        }

        addLine(
                lines,
                MEGATranslations.Compression.text(
                        inv.isCompressionEnabled()
                                ? withStyle(MEGATranslations.Enabled.text(), TextFormatting.GREEN)
                                : withStyle(MEGATranslations.Disabled.text(), TextFormatting.RED)));

        long trace = inv.getTraceUnits();

        if (trace > 0) {
            ITextComponent text =
                    inv.isCompressionEnabled()
                            ? MEGATranslations.TraceUnits.text(Tooltips.ofUnformattedNumber(trace), inv.getLowestVariant().getTextComponent())
                            : MEGATranslations.ContainsTraceUnits.text();
            addLine(lines, text, TextFormatting.YELLOW);
        }

        if (inv.isCompressionEnabled()) {
            ItemStack cutoffItem = inv.getCutoffItem();

            if (!sameItemAndTag(cutoffItem, inv.getHighestVariant())) {
                addLine(lines, MEGATranslations.Cutoff.text(cutoffItem.getTextComponent()));
            }
        }
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack itemStack) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {
    }

    private static void addLine(List<String> lines, ITextComponent component) {
        lines.add(component.getFormattedText());
    }

    private static void addLine(List<String> lines, ITextComponent component, TextFormatting formatting) {
        lines.add(formatting + component.getFormattedText());
    }

    private static ITextComponent withStyle(ITextComponent component, TextFormatting formatting) {
        component.getStyle().setColor(formatting);
        return component;
    }

    public static boolean sameItemAndTag(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private static class Handler implements ICellHandler {
        private Handler() {
        }

        @Override
        public boolean isCell(ItemStack is) {
            return is != null && !is.isEmpty() && MEGAItems.BULK_ITEM_CELL.is(is);
        }

        @Nullable
        @Override
        public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            return isCell(is) ? new BulkCellInventory(is, host) : null;
        }
    }
}
