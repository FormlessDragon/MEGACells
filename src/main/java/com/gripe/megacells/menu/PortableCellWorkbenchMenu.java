package com.gripe.megacells.menu;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Iterators;

import it.unimi.dsi.fastutil.shorts.ShortSet;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import ae2.api.config.CopyMode;
import ae2.api.config.FuzzyMode;
import ae2.api.config.Settings;
import ae2.api.inventories.BaseInternalInventory;
import ae2.api.inventories.ISegmentedInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.stacks.GenericStack;
import ae2.api.storage.StorageCells;
import ae2.api.util.IConfigManager;
import ae2.helpers.externalstorage.GenericStackInv;
import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.implementations.ContainerCellWorkbench;
import ae2.container.implementations.UpgradeableContainer;
import ae2.container.slot.CellPartitionSlot;
import ae2.container.slot.IPartitionSlotHost;
import ae2.container.slot.OptionalRestrictedInputSlot;
import ae2.container.slot.RestrictedInputSlot;
import ae2.util.EnumCycler;
import ae2.util.ConfigGuiInventory;
import ae2.util.inv.SupplierInternalInventory;

import com.gripe.megacells.item.cell.BulkCellInventory;
import com.gripe.megacells.item.cell.PortableCellWorkbenchMenuHost;

/**
 * See {@link ae2.container.implementations.ContainerCellWorkbench}
 */
public class PortableCellWorkbenchMenu extends UpgradeableContainer<PortableCellWorkbenchMenuHost>
        implements IPartitionSlotHost, CompressionCutoffHost {
    private static final int CONFIG_SLOTS_PER_PAGE = 63;

    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;

    @GuiSync(3)
    public int currentPage = 0;

    @GuiSync(4)
    public int pageCount = 1;

    @GuiSync(7)
    public int configSlotCount = CONFIG_SLOTS_PER_PAGE;

    private ConfigPageInventory configPageInventory;

    public PortableCellWorkbenchMenu(InventoryPlayer ip, PortableCellWorkbenchMenuHost host) {
        super(ip, host);
        registerClientAction(ContainerCellWorkbench.ACTION_NEXT_COPYMODE, this::nextWorkBenchCopyMode);
        registerClientAction(ContainerCellWorkbench.ACTION_PARTITION, this::partition);
        registerClientAction(ContainerCellWorkbench.ACTION_CLEAR, this::clear);
        registerClientAction(ContainerCellWorkbench.ACTION_SET_FUZZY_MODE, FuzzyMode.class, this::setCellFuzzyMode);
        registerClientAction(ACTION_SET_COMPRESSION_LIMIT, Boolean.class, this::mega$nextCompressionLimit);
        registerClientAction(ContainerCellWorkbench.ACTION_SET_PAGE, Integer.class, this::setPage);
    }

    public void setCellFuzzyMode(FuzzyMode fuzzyMode) {
        if (isClientSide()) {
            sendClientAction(ContainerCellWorkbench.ACTION_SET_FUZZY_MODE, fuzzyMode);
        } else {
            var cell = getHost().getCell();

            if (cell != null) {
                cell.setFuzzyMode(getWorkbenchItem(), fuzzyMode);
                getHost().saveChanges();
            }
        }
    }

    public void nextWorkBenchCopyMode() {
        if (isClientSide()) {
            sendClientAction(ContainerCellWorkbench.ACTION_NEXT_COPYMODE);
        } else {
            getHost().getConfigManager().putSetting(Settings.COPY_MODE, EnumCycler.next(getWorkBenchCopyMode()));
        }
    }

    @Override
    public void mega$nextCompressionLimit(boolean backwards) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_COMPRESSION_LIMIT, backwards);
        } else {
            if (StorageCells.getCellInventory(getHost().mega$getContainedStack(), null)
                    instanceof BulkCellInventory bulkCell) {
                bulkCell.switchCompressionCutoff(backwards);
                getHost().saveChanges();
            }
        }
    }

    private CopyMode getWorkBenchCopyMode() {
        return getHost().getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected void setupInventorySlots() {
        var cell = getHost().getSubInventory(ISegmentedInventory.CELLS);
        addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0),
                SlotSemantics.STORAGE_CELL);
    }

    @Override
    protected void setupConfig() {
        configPageInventory = new ConfigPageInventory(this::getConfigInventory, CONFIG_SLOTS_PER_PAGE);

        for (int slot = 0; slot < CONFIG_SLOTS_PER_PAGE; slot++) {
            addSlot(new CellPartitionSlot(configPageInventory, this, slot), SlotSemantics.CONFIG);
        }
    }

    @Override
    protected void setupUpgrades() {
        var upgradeInventory = new SupplierInternalInventory<>(getHost()::getCellUpgrades);

        for (int i = 0; i < 8; i++) {
            var slot = new OptionalRestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.UPGRADES, upgradeInventory, this, i, 0, 0, i);
            addSlot(slot, SlotSemantics.UPGRADE);
        }
    }

    public ItemStack getWorkbenchItem() {
        return Objects.requireNonNull(getHost().getSubInventory(ISegmentedInventory.CELLS))
                .getStackInSlot(0);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        copyMode = getWorkBenchCopyMode();

        var cell = getHost().getCell();
        setFuzzyMode(cell != null ? cell.getFuzzyMode(getWorkbenchItem()) : FuzzyMode.IGNORE_ALL);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return idx < getHost().getCellUpgrades().size();
    }

    @Override
    public boolean isPartitionSlotEnabled(int idx) {
        int slotIndex = firstSlotOnPage(currentPage) + idx;
        var cell = getHost().getCell();

        if (cell != null && getCopyMode() == CopyMode.CLEAR_ON_REMOVE) {
            return slotIndex < cell.getConfigInventory(getWorkbenchItem()).size();
        }

        return getCopyMode() == CopyMode.KEEP_ON_REMOVE && slotIndex < getConfigInventory().size();
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);
        getHost().getConfigManager().putSetting(Settings.COPY_MODE, getCopyMode());
        updateConfigPageInventory();
    }

    @Override
    public void onClientDataSync(ShortSet updatedFields) {
        super.onClientDataSync(updatedFields);
        updateConfigPageInventory();
    }

    public void clear() {
        if (isClientSide()) {
            sendClientAction(ContainerCellWorkbench.ACTION_CLEAR);
        } else {
            getConfigInventory().clear();
            broadcastChanges();
        }
    }

    public void partition() {
        if (isClientSide()) {
            sendClientAction(ContainerCellWorkbench.ACTION_PARTITION);
        } else {
            var inv = getConfigInventory();
            var is = getWorkbenchItem();

            var cellInv = StorageCells.getCellInventory(is, null);

            if (cellInv != null) {
                var it = Iterators.transform(cellInv.getAvailableStacks().iterator(), Map.Entry::getKey);

                for (var x = 0; x < inv.size(); x++) {
                    if (it.hasNext()) {
                        inv.setStack(x, new GenericStack(it.next(), 0));
                    } else {
                        inv.setStack(x, null);
                    }
                }
            }

            broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            configSlotCount = getConfigInventory().size();
            pageCount = pageCount(configSlotCount);
            currentPage = clampPage(currentPage, pageCount);
        }

        updateConfigPageInventory();
        super.broadcastChanges();
    }

    public void setPage(int page) {
        if (isClientSide()) {
            sendClientAction(ContainerCellWorkbench.ACTION_SET_PAGE, page);
        } else {
            configSlotCount = getConfigInventory().size();
            pageCount = pageCount(configSlotCount);
            currentPage = clampPage(page, pageCount);
            updateConfigPageInventory();
            detectAndSendChanges();
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    private GenericStackInv getConfigInventory() {
        return Objects.requireNonNull(getHost().getConfig());
    }

    public CopyMode getCopyMode() {
        return copyMode;
    }

    private void updateConfigPageInventory() {
        if (configPageInventory != null) {
            configPageInventory.setPage(currentPage);
        }
    }

    private static int pageCount(int slots) {
        return Math.max(1, (Math.max(0, slots) + CONFIG_SLOTS_PER_PAGE - 1) / CONFIG_SLOTS_PER_PAGE);
    }

    private static int clampPage(int page, int pageCount) {
        if (pageCount <= 0) {
            return 0;
        }

        return Math.max(0, Math.min(page, pageCount - 1));
    }

    private static int firstSlotOnPage(int page) {
        return Math.max(0, page) * CONFIG_SLOTS_PER_PAGE;
    }

    private static class ConfigPageInventory extends BaseInternalInventory {
        private final java.util.function.Supplier<GenericStackInv> configSupplier;
        private final int slotsPerPage;
        private int page;
        private GenericStackInv cachedConfig;
        private ConfigGuiInventory cachedGuiWrapper;

        ConfigPageInventory(java.util.function.Supplier<GenericStackInv> configSupplier, int slotsPerPage) {
            this.configSupplier = configSupplier;
            this.slotsPerPage = slotsPerPage;
        }

        void setPage(int page) {
            this.page = page;
        }

        private InternalInventory getDelegate() {
            GenericStackInv config = configSupplier.get();

            if (cachedConfig != config || cachedGuiWrapper == null) {
                cachedConfig = config;
                cachedGuiWrapper = config.createGuiWrapper();
            }

            return cachedGuiWrapper;
        }

        private int translateSlot(int slot) {
            return page * slotsPerPage + slot;
        }

        private boolean isValidTranslatedSlot(int slot) {
            int translatedSlot = translateSlot(slot);
            return slot >= 0 && slot < slotsPerPage && translatedSlot >= 0 && translatedSlot < getDelegate().size();
        }

        @Override
        public int size() {
            return slotsPerPage;
        }

        @Override
        public int getSlotLimit(int slot) {
            return isValidTranslatedSlot(slot) ? getDelegate().getSlotLimit(translateSlot(slot)) : 0;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return isValidTranslatedSlot(slotIndex)
                    ? getDelegate().getStackInSlot(translateSlot(slotIndex))
                    : ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if (isValidTranslatedSlot(slotIndex)) {
                getDelegate().setItemDirect(translateSlot(slotIndex), stack);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isValidTranslatedSlot(slot) && getDelegate().isItemValid(translateSlot(slot), stack);
        }

        @Override
        public InternalInventory getSlotInv(int slotIndex) {
            return isValidTranslatedSlot(slotIndex) ? getDelegate().getSlotInv(translateSlot(slotIndex)) : this;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isValidTranslatedSlot(slot)
                    ? getDelegate().insertItem(translateSlot(slot), stack, simulate)
                    : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isValidTranslatedSlot(slot)
                    ? getDelegate().extractItem(translateSlot(slot), amount, simulate)
                    : ItemStack.EMPTY;
        }
    }
}
