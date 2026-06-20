package com.gripe.megacells.item.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import ae2.api.networking.GridFlags;
import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.container.ISubGui;
import ae2.container.GuiIds;
import ae2.core.gui.GuiOpener;
import ae2.helpers.IPriorityHost;
import ae2.items.parts.PartModels;
import ae2.parts.AEBasePart;
import ae2.parts.PartModel;

import com.gripe.megacells.MEGACells;
import com.gripe.megacells.definition.MEGAItems;
import com.gripe.megacells.misc.DecompressionService;

public class DecompressionModulePart extends AEBasePart implements IPriorityHost {
    @PartModels
    private static final IPartModel MODEL = new PartModel(MEGACells.makeId("part/decompression_module"));

    public DecompressionModulePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(10.0);
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!isClientSide()) {
            GuiOpener.openPartGui(player, GuiIds.GuiKey.PRIORITY, this);
        }

        return true;
    }

    @Override
    public int getPriority() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(DecompressionService.class).getPatternPriority() : 0;
    }

    @Override
    public void setPriority(int priority) {
        var node = getMainNode().getNode();
        var grid = getMainNode().getGrid();

        if (node != null && grid != null) {
            grid.getService(DecompressionService.class).setPatternPriority(priority, node);
            getHost().markForSave();
        }
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        player.closeScreen();
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return MEGAItems.DECOMPRESSION_MODULE.stack();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3, 3, 12, 13, 13, 16);
        bch.addBox(5, 5, 11, 11, 11, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }
}
