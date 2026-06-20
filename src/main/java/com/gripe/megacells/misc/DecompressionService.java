package com.gripe.megacells.misc;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.nbt.NBTTagCompound;

import ae2.api.config.Actionable;
import ae2.api.crafting.IPatternDetails;
import ae2.api.implementations.blockentities.IChestOrDrive;
import ae2.api.networking.IGrid;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridService;
import ae2.api.networking.IGridServiceProvider;
import ae2.api.networking.crafting.ICraftingProvider;
import ae2.api.networking.crafting.ICraftingService;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;

import com.gripe.megacells.item.cell.BulkCellInventory;
import com.gripe.megacells.item.part.DecompressionModulePart;

public class DecompressionService implements IGridService, IGridServiceProvider, ICraftingProvider {
    private static final String TAG_PATTERN_PRIORITY = "dcp";

    private final List<IChestOrDrive> cellHosts = new ObjectArrayList<>();
    private final List<IPatternDetails> patterns = new ObjectArrayList<>();

    private final IGrid grid;
    private int installedModules;

    private final Object2LongMap<AEKey> patternOutputs = new Object2LongOpenHashMap<>();
    private int patternPriority;
    private boolean priorityLocked;

    public DecompressionService(IGrid grid, ICraftingService craftingService) {
        this.grid = grid;
        craftingService.addGlobalCraftingProvider(this);
    }

    @Override
    public void addNode(IGridNode node, @Nullable NBTTagCompound savedData) {
        if (node.getOwner() instanceof IChestOrDrive) {
            cellHosts.add((IChestOrDrive) node.getOwner());
        }

        if (node.getOwner() instanceof DecompressionModulePart) {
            installedModules++;

            if (!priorityLocked && savedData != null && savedData.hasKey(TAG_PATTERN_PRIORITY, 3)) {
                patternPriority = savedData.getInteger(TAG_PATTERN_PRIORITY);
                priorityLocked = true;
            }
        }
    }

    @Override
    public void saveNodeData(IGridNode node, NBTTagCompound savedData) {
        if (priorityLocked && node.getOwner() instanceof DecompressionModulePart) {
            savedData.setInteger(TAG_PATTERN_PRIORITY, patternPriority);
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node.getOwner() instanceof IChestOrDrive) {
            cellHosts.remove(node.getOwner());
        }

        if (node.getOwner() instanceof DecompressionModulePart) {
            installedModules--;
        }
    }

    @Override
    public void onServerStartTick() {
        if (!patternOutputs.isEmpty()) {
            for (java.util.Iterator<Object2LongMap.Entry<AEKey>> it = Object2LongMaps.fastIterator(patternOutputs); it.hasNext(); ) {
                Object2LongMap.Entry<AEKey> output = it.next();
                AEKey what = output.getKey();
                long amount = output.getLongValue();
                long inserted = grid.getStorageService()
                        .getInventory()
                        .insert(what, amount, Actionable.MODULATE, IActionSource.empty());

                if (inserted >= amount) {
                    it.remove();
                } else if (inserted > 0) {
                    patternOutputs.put(what, amount - inserted);
                }
            }
        }
    }

    @Override
    public void onServerEndTick() {
        patterns.clear();

        if (installedModules > 0) {
            for (IChestOrDrive cellHost : cellHosts) {
                for (int i = 0; i < cellHost.getCellCount(); i++) {
                    if (cellHost.getOriginalCellInventory(i) instanceof BulkCellInventory) {
                        BulkCellInventory bulkCell = (BulkCellInventory) cellHost.getOriginalCellInventory(i);
                        patterns.addAll(bulkCell.getDecompressionPatterns());
                    }
                }
            }

            grid.getCraftingService().refreshGlobalCraftingProvider(this);
        }
    }

    @Override
    public List<? extends IPatternDetails> getAvailablePatterns() {
        return installedModules > 0 ? patterns : Collections.emptyList();
    }

    @Override
    public int getPatternPriority() {
        return patternPriority;
    }

    public void setPatternPriority(int priority, IGridNode node) {
        if (node.getOwner() instanceof DecompressionModulePart) {
            patternPriority = priority;
            priorityLocked = true;
            grid.getCraftingService().refreshGlobalCraftingProvider(this);
        }
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder, int multiplier) {
        if (details instanceof DecompressionPattern) {
            ae2.api.stacks.GenericStack output = details.getPrimaryOutput();
            patternOutputs.merge(output.what(), output.amount() * multiplier, Long::sum);
            return true;
        }

        return false;
    }

    @Override
    public boolean canMergePatternPush(IPatternDetails details) {
        return details instanceof DecompressionPattern;
    }

    @Override
    public int getMaxPatternPushMultiplier(IPatternDetails details, int maxMultiplier) {
        return details instanceof DecompressionPattern ? maxMultiplier : 0;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
