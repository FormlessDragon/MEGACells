package com.gripe.megacells.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import ae2.api.config.ActionItems;
import ae2.api.config.CopyMode;
import ae2.api.config.FuzzyMode;
import ae2.api.config.Settings;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.GenericStack;
import ae2.api.storage.StorageCells;
import ae2.client.gui.Icon;
import ae2.client.gui.implementations.GuiUpgradeable;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.ActionButton;
import ae2.client.gui.widgets.SettingToggleButton;
import ae2.client.gui.widgets.ToggleButton;
import ae2.core.definitions.AEItems;
import ae2.core.localization.GuiText;

import com.gripe.megacells.item.cell.BulkCellInventory;
import com.gripe.megacells.menu.PortableCellWorkbenchMenu;

public class PortableCellWorkbenchScreen extends GuiUpgradeable<PortableCellWorkbenchMenu> {
    private final ToggleButton copyMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final CompressionCutoffButton compressionCutoff;

    public PortableCellWorkbenchScreen(
            PortableCellWorkbenchMenu menu, InventoryPlayer playerInventory, ITextComponent title, GuiStyle style) {
        super(menu, playerInventory, title, style);

        fuzzyMode = addToLeftToolbar(new SettingToggleButton<>(
                Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL,
                (button, backwards) -> menu.setCellFuzzyMode(button.getNextValue(backwards))));
        addToLeftToolbar(new ActionButton(ActionItems.COG, menu::partition));
        addToLeftToolbar(new ActionButton(ActionItems.CLOSE, menu::clear));
        copyMode = addToLeftToolbar(new ToggleButton(
                Icon.COPY_MODE_ON,
                Icon.COPY_MODE_OFF,
                GuiText.CopyMode.text(),
                GuiText.CopyModeDesc.text(),
                backwards -> menu.nextWorkBenchCopyMode()));
        compressionCutoff = addToLeftToolbar(
                new CompressionCutoffButton(() -> menu.mega$nextCompressionLimit(isHandlingRightClick())));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        copyMode.setState(getContainer().getCopyMode() == CopyMode.CLEAR_ON_REMOVE);
        fuzzyMode.set(getContainer().getFuzzyMode());
        fuzzyMode.setVisibility(getContainer().getHost().getCellUpgrades().isInstalled(AEItems.FUZZY_CARD.asItem()));

        if (StorageCells.getCellInventory(getContainer().getHost().mega$getContainedStack(), null)
                        instanceof BulkCellInventory bulkCell
                && bulkCell.hasCompressionChain()) {
            compressionCutoff.setVisibility(bulkCell.isCompressionEnabled());
            compressionCutoff.setItem(bulkCell.getCutoffItem());
        } else {
            compressionCutoff.setVisibility(false);
        }
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        ItemStack cell = getContainer().getWorkbenchItem();

        if (cell.isEmpty() || cell == stack || getContainer().getHost().getCell() == null) {
            return super.getItemToolTip(stack);
        }

        GenericStack genericStack = GenericStack.unwrapItemStack(stack);
        var what = genericStack != null ? genericStack.what() : AEItemKey.of(stack);

        if (what == null) {
            return super.getItemToolTip(stack);
        }

        var configInventory = getContainer().getHost().getCell().getConfigInventory(cell);

        if (!configInventory.isSupportedType(what.getType())) {
            return incompatibleTooltip(stack);
        }

        for (int i = 0; i < configInventory.size(); i++) {
            if (configInventory.isAllowedIn(i, what)) {
                return super.getItemToolTip(stack);
            }
        }

        if (!configInventory.isEmpty()) {
            return incompatibleTooltip(stack);
        }

        return super.getItemToolTip(stack);
    }

    private List<String> incompatibleTooltip(ItemStack stack) {
        List<String> lines = new ArrayList<>(super.getItemToolTip(stack));
        lines.add(TextFormatting.RED + GuiText.IncompatibleWithCell.text().getUnformattedText());
        return lines;
    }
}
