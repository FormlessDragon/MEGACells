package com.gripe.megacells.item.cell;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import ae2.api.implementations.guiobjects.IGuiItem;
import ae2.api.implementations.guiobjects.ItemGuiHost;
import ae2.api.stacks.GenericStack;
import ae2.core.AEConfig;
import ae2.core.gui.locator.GuiHostLocators;
import ae2.core.gui.locator.ItemGuiHostLocator;
import ae2.items.AEBaseItem;

import com.gripe.megacells.MEGACells;
import com.gripe.megacells.misc.MEGAGuiHandler;

public class PortableCellWorkbenchItem extends AEBaseItem implements IGuiItem {
    public PortableCellWorkbenchItem() {
        setMaxStackSize(1);
    }

    @Override
    public ItemGuiHost<?> getGuiHost(EntityPlayer player, ItemGuiHostLocator locator, RayTraceResult hitResult) {
        return new PortableCellWorkbenchMenuHost(this, player, locator);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        int slot = findHeldSlot(player, stack);

        if (!world.isRemote && slot >= 0) {
            player.openGui(
                    MEGACells.instance,
                    MEGAGuiHandler.PORTABLE_CELL_WORKBENCH,
                    world,
                    slot,
                    0,
                    0);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag) {
        PortableCellWorkbenchMenuHost host =
                new PortableCellWorkbenchMenuHost(this, null, GuiHostLocators.forStack(stack));
        List<GenericStack> config = host.getConfig().toList();

        List<GenericStack> shownConfig = new ArrayList<>();
        boolean hasMore = false;

        for (GenericStack c : config) {
            if (c != null) {
                shownConfig.add(c);

                if (shownConfig.size() == AEConfig.instance().getTooltipMaxCellContentShown()) {
                    hasMore = true;
                    break;
                }
            }
        }

        ItemStack contained = host.mega$getContainedStack();

        if (!contained.isEmpty()) {
            lines.add(contained.getDisplayName());
        }

        for (GenericStack genericStack : shownConfig) {
            lines.add(genericStack.what().getDisplayName().getFormattedText());
        }

        if (hasMore) {
            lines.add("...");
        }
    }

    private static int findHeldSlot(EntityPlayer player, ItemStack held) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) == held) {
                return i;
            }
        }

        return -1;
    }
}
