package com.gripe.megacells.item.cell;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ae2.api.implementations.guiobjects.ItemGuiHost;
import ae2.api.inventories.ISegmentedInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.storage.cells.ICellWorkbenchItem;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.util.IConfigManager;
import ae2.api.util.IConfigurableObject;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.helpers.IConfigInvHost;
import ae2.helpers.externalstorage.GenericStackInv;
import ae2.items.contents.StackDependentSupplier;

public class PortableCellWorkbenchMenuHost extends ItemGuiHost<PortableCellWorkbenchItem>
        implements ISegmentedInventory, IConfigurableObject, IConfigInvHost {
    private final Supplier<PortableCellWorkbenchInventory> cellInv =
            new StackDependentSupplier<>(this::getItemStack, PortableCellWorkbenchInventory::new);

    public PortableCellWorkbenchMenuHost(
            PortableCellWorkbenchItem item, EntityPlayer player, ItemGuiHostLocator locator) {
        super(item, player, locator);
    }

    public ICellWorkbenchItem getCell() {
        return cellInv.get().getCell();
    }

    public ItemStack mega$getContainedStack() {
        return cellInv.get().getStackInSlot(0);
    }

    @Override
    public IConfigManager getConfigManager() {
        return cellInv.get().getConfigManager();
    }

    @Override
    public GenericStackInv getConfig() {
        return cellInv.get().getConfig();
    }

    public IUpgradeInventory getCellUpgrades() {
        return cellInv.get().getCellUpgrades();
    }

    public void saveChanges() {
        cellInv.get().saveChanges();
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        return id.equals(ISegmentedInventory.CELLS) ? cellInv.get() : null;
    }
}
