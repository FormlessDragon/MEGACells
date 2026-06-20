package com.gripe.megacells.item.cell;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ae2.api.config.CopyMode;
import ae2.api.config.Settings;
import ae2.api.inventories.InternalInventory;
import ae2.api.storage.cells.ICellWorkbenchItem;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.api.util.IConfigManager;
import ae2.helpers.externalstorage.GenericStackInv;
import ae2.tile.misc.TileCellWorkbench;
import ae2.util.ConfigInventory;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import ae2.util.inv.filter.IAEItemFilter;

public class PortableCellWorkbenchInventory extends AppEngInternalInventory implements InternalInventoryHost {
    private static final String TAG_ROOT = "MEGAPortableCellWorkbench";
    private static final String TAG_CELL = "Cell";
    private static final String TAG_CONFIG = "Config";
    private static final String TAG_SETTINGS = "Settings";

    private final ItemStack stack;

    private final GenericStackInv config =
            new GenericStackInv(this::onConfigChanged, GenericStackInv.Mode.CONFIG_TYPES, 63);
    private final IConfigManager manager = IConfigManager.builder(this::saveChanges)
            .registerSetting(Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE)
            .build();

    public PortableCellWorkbenchInventory(ItemStack stack) {
        super(null, 1, 1, Filter.FILTER);
        this.stack = stack;

        setHost(this);
        setEnableClientEvents(true);

        NBTTagCompound data = getRoot(stack, false);

        if (data != null) {
            if (data.hasKey(TAG_CELL, 10)) {
                setItemDirect(0, new ItemStack(data.getCompoundTag(TAG_CELL)));
            }

            config.readFromChildTag(data, TAG_CONFIG);

            if (data.hasKey(TAG_SETTINGS, 10)) {
                manager.readFromNBT(data.getCompoundTag(TAG_SETTINGS));
            }
        }
    }

    ICellWorkbenchItem getCell() {
        if (getStackInSlot(0).isEmpty()) {
            return null;
        }

        return getStackInSlot(0).getItem() instanceof ICellWorkbenchItem
                ? (ICellWorkbenchItem) getStackInSlot(0).getItem()
                : null;
    }

    GenericStackInv getConfig() {
        return config;
    }

    IConfigManager getConfigManager() {
        return manager;
    }

    private ConfigInventory getCellConfigInventory() {
        ICellWorkbenchItem cell = getCell();
        return cell != null ? cell.getConfigInventory(getStackInSlot(0)) : null;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        ConfigInventory configInventory = getCellConfigInventory();

        if (configInventory != null) {
            if (!configInventory.isEmpty()) {
                TileCellWorkbench.copy(configInventory, config);
            } else {
                TileCellWorkbench.copy(config, configInventory);
                TileCellWorkbench.copy(configInventory, config);
            }
        } else if (getConfigManager().getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
            config.clear();
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    private void onConfigChanged() {
        ConfigInventory c = getCellConfigInventory();

        if (c != null) {
            TileCellWorkbench.copy(config, c);
            TileCellWorkbench.copy(c, config);
        }

        saveChanges();
    }

    void saveChanges() {
        NBTTagCompound data = getRoot(stack, true);

        if (getStackInSlot(0).isEmpty()) {
            data.removeTag(TAG_CELL);
        } else {
            data.setTag(TAG_CELL, getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }

        config.writeToChildTag(data, TAG_CONFIG);

        NBTTagCompound settings = new NBTTagCompound();
        manager.writeToNBT(settings);
        data.setTag(TAG_SETTINGS, settings);

        if (data.isEmpty()) {
            NBTTagCompound root = stack.getTagCompound();

            if (root != null) {
                root.removeTag(TAG_ROOT);

                if (root.isEmpty()) {
                    stack.setTagCompound(null);
                }
            }
        }
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    IUpgradeInventory getCellUpgrades() {
        ICellWorkbenchItem cell = getCell();
        return cell != null
                ? new ProxiedUpgradeInventory(cell.getUpgrades(getStackInSlot(0)), this)
                : UpgradeInventories.empty();
    }

    private static NBTTagCompound getRoot(ItemStack stack, boolean create) {
        NBTTagCompound root = stack.getTagCompound();

        if (root == null) {
            if (!create) {
                return null;
            }

            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }

        if (!root.hasKey(TAG_ROOT, 10)) {
            if (!create) {
                return null;
            }

            root.setTag(TAG_ROOT, new NBTTagCompound());
        }

        return root.getCompoundTag(TAG_ROOT);
    }

    private static class ProxiedUpgradeInventory extends AppEngInternalInventory implements IUpgradeInventory {
        private final IUpgradeInventory delegate;

        ProxiedUpgradeInventory(IUpgradeInventory delegate, InternalInventoryHost host) {
            super(host, delegate.size(), 1);
            this.delegate = delegate;
        }

        @Override
        public Item getUpgradableItem() {
            return delegate.getUpgradableItem();
        }

        @Override
        public int getInstalledUpgrades(Item u) {
            return delegate.getInstalledUpgrades(u);
        }

        @Override
        public int getMaxInstalled(Item u) {
            return delegate.getMaxInstalled(u);
        }

        @Override
        public void readFromNBT(NBTTagCompound data, String subtag) {
            delegate.readFromNBT(data, subtag);
        }

        @Override
        public void writeToNBT(NBTTagCompound data, String subtag) {
            delegate.writeToNBT(data, subtag);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return delegate.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            delegate.setItemDirect(slotIndex, stack);
            onContentsChanged(slotIndex);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = delegate.extractItem(slot, amount, simulate);

            if (!simulate && !extracted.isEmpty()) {
                onContentsChanged(slot);
            }

            return extracted;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return delegate.isItemValid(slot, stack);
        }

        @Override
        protected boolean eventsEnabled() {
            return true;
        }
    }

    private static class Filter implements IAEItemFilter {
        private static final IAEItemFilter FILTER = new Filter();

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return stack.getItem() instanceof ICellWorkbenchItem;
        }
    }
}
