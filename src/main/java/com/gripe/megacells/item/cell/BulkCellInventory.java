package com.gripe.megacells.item.cell;

import ae2.api.config.Actionable;
import ae2.api.crafting.IPatternDetails;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import com.gripe.megacells.misc.CompressionChain;
import com.gripe.megacells.misc.CompressionService;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BulkCellInventory implements StorageCell {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkCellInventory.class);

    private static final String TAG_ROOT = "MEGABulkCell";
    private static final String TAG_ITEM = "BulkCellItem";
    private static final String TAG_UNIT_COUNT = "BulkCellUnitCount";
    private static final String TAG_UNIT_FACTOR = "BulkCellUnitFactor";
    private static final String TAG_COMPRESSION_CUTOFF = "BulkCellCompressionCutoff";

    private final ISaveProvider container;
    private final ItemStack stack;
    private final AEItemKey filterItem;
    private final boolean compressionEnabled;
    private AEItemKey storedItem;
    private CompressionChain compressionChain;
    private BigInteger unitCount;
    private BigInteger unitFactor;
    private int compressionCutoff;

    private Map<AEItemKey, Long> compressedStacks;
    private boolean needsStackUpdate;
    private List<IPatternDetails> decompressionPatterns;

    private boolean isPersisted = true;

    BulkCellInventory(ItemStack stack, ISaveProvider container) {
        this.stack = stack;
        this.container = container;

        BulkCellItem cell = (BulkCellItem) stack.getItem();
        filterItem = (AEItemKey) cell.getConfigInventory(stack).getKey(0);
        compressionEnabled = cell.hasCompressionCard(stack);

        NBTTagCompound data = getBulkTag(stack, false);
        storedItem = readItemKey(data);
        unitCount = readBigInteger(data, TAG_UNIT_COUNT, BigInteger.ZERO);

        AEItemKey determiningItem = storedItem != null ? storedItem : filterItem;
        compressionChain = CompressionService.getChain(determiningItem);

        unitFactor = compressionChain.unitFactor(determiningItem);
        BigInteger recordedFactor = readBigInteger(data, TAG_UNIT_FACTOR, unitFactor);

        if (!unitFactor.equals(recordedFactor) && recordedFactor.signum() > 0) {
            unitCount = unitCount.multiply(unitFactor).divide(recordedFactor);
            writeBigInteger(getBulkTag(stack, true), TAG_UNIT_COUNT, unitCount);
            writeBigInteger(getBulkTag(stack, true), TAG_UNIT_FACTOR, unitFactor);
        }

        if (determiningItem == null && data != null) {
            data.removeTag(TAG_COMPRESSION_CUTOFF);
        }

        int maxCutoff = Math.max(0, compressionChain.size() - 1);
        int recordedCutoff = data != null && data.hasKey(TAG_COMPRESSION_CUTOFF) ? data.getInteger(TAG_COMPRESSION_CUTOFF) : maxCutoff;
        compressionCutoff = recordedCutoff < 0 ? maxCutoff : Math.min(recordedCutoff, maxCutoff);

        compressedStacks = compressionChain.initStacks(unitCount, compressionCutoff, determiningItem);
    }

    @Nullable
    private static NBTTagCompound getBulkTag(ItemStack stack, boolean create) {
        if (stack.isEmpty()) {
            return null;
        }

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

    private static void removeBulkTag(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();

        if (root == null) {
            return;
        }

        root.removeTag(TAG_ROOT);

        if (root.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static AEItemKey readItemKey(@Nullable NBTTagCompound data) {
        if (data == null || !data.hasKey(TAG_ITEM, 10)) {
            return null;
        }

        AEKey key = AEKey.fromTagGeneric(data.getCompoundTag(TAG_ITEM));
        return key instanceof AEItemKey ? (AEItemKey) key : null;
    }

    private static BigInteger readBigInteger(@Nullable NBTTagCompound data, String key, BigInteger fallback) {
        if (data == null || !data.hasKey(key, 8)) {
            return fallback;
        }

        String value = data.getString(key);

        try {
            return new BigInteger(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Ignoring invalid bulk cell integer tag {}={}", key, value, e);
            return fallback;
        }
    }

    private static void writeBigInteger(NBTTagCompound data, String key, BigInteger value) {
        data.setString(key, value.toString());
    }

    @Override
    public CellState getStatus() {
        if (storedItem == null || unitCount.signum() < 1) {
            return CellState.EMPTY;
        }

        if (isFilterMismatched()) {
            return CellState.FULL;
        }

        return CellState.NOT_EMPTY;
    }

    AEItemKey getStoredItem() {
        return storedItem;
    }

    long getStoredQuantity() {
        return CompressionChain.clamp(unitCount.divide(unitFactor), Long.MAX_VALUE);
    }

    AEItemKey getFilterItem() {
        return filterItem;
    }

    private boolean isFilterMismatched() {
        if (storedItem == null) {
            return false;
        }

        if (storedItem.equals(filterItem)) {
            return false;
        }

        if (filterItem == null) {
            return true;
        }

        if (compressionChain.containsVariant(filterItem)) {
            storedItem = filterItem;
            unitFactor = compressionChain.unitFactor(storedItem);
            saveChanges();
            return false;
        }

        return true;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public boolean hasCompressionChain() {
        return !compressionChain.isEmpty();
    }

    long getTraceUnits() {
        return CompressionChain.clamp(unitCount.remainder(unitFactor), Long.MAX_VALUE);
    }

    public List<IPatternDetails> getDecompressionPatterns() {
        if (filterItem == null || !compressionEnabled || !hasCompressionChain() || isFilterMismatched()) {
            return Collections.emptyList();
        }

        if (decompressionPatterns == null) {
            decompressionPatterns = compressionChain.getDecompressionPatterns(compressionCutoff);
        }

        return decompressionPatterns;
    }

    @Override
    public double getIdleDrain() {
        return 5.0f;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !(what instanceof AEItemKey item)) {
            return 0;
        }

        if (isFilterMismatched()) {
            return 0;
        }

        if (!item.equals(filterItem) && (!compressionEnabled || !compressionChain.containsVariant(item))) {
            return 0;
        }

        BigInteger factor = compressionChain.unitFactor(item);
        BigInteger units = BigInteger.valueOf(amount).multiply(factor);

        if (mode == Actionable.MODULATE) {
            if (storedItem == null) {
                storedItem = filterItem;
            }

            unitCount = unitCount.add(units);
            saveChanges();
            needsStackUpdate = true;
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (storedItem == null || unitCount.signum() < 1 || !(what instanceof AEItemKey item)) {
            return 0;
        }

        if (!compressionChain.containsVariant(item) && !item.equals(storedItem)) {
            return 0;
        }

        if (isFilterMismatched()) {
            amount = Math.min(amount, getAvailableStacks().get(item));
        } else if (!compressionEnabled && !item.equals(storedItem)) {
            return 0;
        }

        BigInteger factor = compressionChain.unitFactor(item);
        BigInteger units = BigInteger.valueOf(amount).multiply(factor).min(unitCount);

        if (mode == Actionable.MODULATE) {
            unitCount = unitCount.subtract(units).max(BigInteger.ZERO);

            if (unitCount.signum() < 1) {
                storedItem = null;
                CompressionChain filterChain = CompressionService.getChain(filterItem);

                if (compressionChain != filterChain) {
                    compressionChain = filterChain;
                }
            }

            saveChanges();
            needsStackUpdate = true;
        }

        return CompressionChain.clamp(units.divide(factor), Long.MAX_VALUE);
    }

    private void saveChanges() {
        isPersisted = false;

        if (container != null) {
            container.saveChanges();
        } else {
            persist();
        }
    }

    @Override
    public void persist() {
        if (isPersisted) {
            return;
        }

        NBTTagCompound data = getBulkTag(stack, true);

        if (storedItem == null || unitCount.signum() < 1) {
            data.removeTag(TAG_ITEM);
            data.removeTag(TAG_UNIT_COUNT);
            data.removeTag(TAG_UNIT_FACTOR);
        } else {
            data.setTag(TAG_ITEM, storedItem.toTagGeneric());
            writeBigInteger(data, TAG_UNIT_COUNT, unitCount);
            writeBigInteger(data, TAG_UNIT_FACTOR, unitFactor);
        }

        if (data.isEmpty()) {
            removeBulkTag(stack);
        }

        isPersisted = true;
    }

    public void switchCompressionCutoff(boolean backwards) {
        if (!hasCompressionChain()) {
            return;
        }

        int newCutoff = compressionCutoff;
        newCutoff += (backwards ? 1 : -1);

        if (newCutoff < 0) {
            newCutoff = compressionChain.size() - 1;
        } else if (newCutoff >= compressionChain.size()) {
            newCutoff = 0;
        }

        compressionCutoff = newCutoff;
        getBulkTag(stack, true).setInteger(TAG_COMPRESSION_CUTOFF, compressionCutoff);
        decompressionPatterns = null;
        needsStackUpdate = true;
        saveChanges();
    }

    public ItemStack getCutoffItem() {
        return hasCompressionChain() ? compressionChain.getItem(compressionCutoff) : ItemStack.EMPTY;
    }

    ItemStack getHighestVariant() {
        return hasCompressionChain() ? compressionChain.getItem(compressionChain.size() - 1) : ItemStack.EMPTY;
    }

    ItemStack getLowestVariant() {
        return hasCompressionChain() ? compressionChain.getItem(0) : ItemStack.EMPTY;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (needsStackUpdate) {
            AEItemKey determiningItem = storedItem != null ? storedItem : filterItem;
            compressedStacks = compressionChain.initStacks(unitCount, compressionCutoff, determiningItem);
            needsStackUpdate = false;
        }

        if (storedItem != null) {
            if (compressionEnabled) {
                compressedStacks.forEach(out::add);
            } else {
                out.add(storedItem, CompressionChain.clamp(unitCount.divide(unitFactor), CompressionChain.STACK_LIMIT));

                if (isFilterMismatched()) {
                    for (AEItemKey item : compressedStacks.keySet()) {
                        if (storedItem.equals(item)) {
                            break;
                        }

                        out.add(item, compressedStacks.get(item));
                    }
                }
            }
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what instanceof AEItemKey
            && (what.equals(storedItem) || what.equals(filterItem) || compressionChain.containsVariant((AEItemKey) what));
    }

    @Override
    public boolean canFitInsideCell() {
        return filterItem == null && storedItem == null && unitCount.signum() < 1;
    }

    @Override
    public ITextComponent getDescription() {
        return stack.getTextComponent();
    }
}
