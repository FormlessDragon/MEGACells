package com.gripe.megacells.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import ae2.api.parts.IPart;
import ae2.api.parts.IPartHost;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.style.GuiStyleManager;
import ae2.core.gui.locator.GuiHostLocators;

import com.gripe.megacells.client.screen.CellDockScreen;
import com.gripe.megacells.client.screen.PortableCellWorkbenchScreen;
import com.gripe.megacells.definition.MEGAItems;
import com.gripe.megacells.item.cell.PortableCellWorkbenchItem;
import com.gripe.megacells.item.cell.PortableCellWorkbenchMenuHost;
import com.gripe.megacells.item.part.CellDockPart;
import com.gripe.megacells.menu.CellDockMenu;
import com.gripe.megacells.menu.PortableCellWorkbenchMenu;

public final class MEGAGuiHandler implements IGuiHandler {
    public static final MEGAGuiHandler INSTANCE = new MEGAGuiHandler();

    public static final int CELL_DOCK = 0;
    public static final int PORTABLE_CELL_WORKBENCH = 1;

    private static final int SIDE_SHIFT = 28;
    private static final int X_MASK = 0x0fffffff;
    private static final int X_SIGN_BIT = 0x08000000;

    private MEGAGuiHandler() {
    }

    public static int encodeX(int x, EnumFacing side) {
        return (side.ordinal() << SIDE_SHIFT) | (x & X_MASK);
    }

    private static int decodeX(int encodedX) {
        int x = encodedX & X_MASK;
        return (x & X_SIGN_BIT) != 0 ? x | ~X_MASK : x;
    }

    private static EnumFacing decodeSide(int encodedX) {
        int ordinal = encodedX >>> SIDE_SHIFT;
        EnumFacing[] values = EnumFacing.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : EnumFacing.NORTH;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == CELL_DOCK) {
            CellDockPart dock = locateCellDock(world, x, y, z);
            return dock == null ? null : new CellDockMenu(player.inventory, dock);
        }

        if (id == PORTABLE_CELL_WORKBENCH) {
            PortableCellWorkbenchMenuHost host = locatePortableWorkbench(player, x);
            return host == null ? null : new PortableCellWorkbenchMenu(player.inventory, host);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == CELL_DOCK) {
            CellDockPart dock = locateCellDock(world, x, y, z);

            if (dock != null) {
                GuiStyle style = GuiStyleManager.loadStyleDoc("/screens/cell_dock.json");
                return new CellDockScreen(
                        new CellDockMenu(player.inventory, dock),
                        player.inventory,
                        new TextComponentTranslation("gui.megacells.cell_dock.name"),
                        style);
            }
        }

        if (id == PORTABLE_CELL_WORKBENCH) {
            PortableCellWorkbenchMenuHost host = locatePortableWorkbench(player, x);

            if (host != null) {
                GuiStyle style = GuiStyleManager.loadStyleDoc("/screens/portable_cell_workbench.json");
                return new PortableCellWorkbenchScreen(
                        new PortableCellWorkbenchMenu(player.inventory, host),
                        player.inventory,
                        new TextComponentTranslation("item.megacells.portable_cell_workbench.name"),
                        style);
            }
        }

        return null;
    }

    private static CellDockPart locateCellDock(World world, int encodedX, int y, int z) {
        BlockPos pos = new BlockPos(decodeX(encodedX), y, z);
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IPartHost host) {
            IPart part = host.getPart(decodeSide(encodedX));
            return part instanceof CellDockPart ? (CellDockPart) part : null;
        }

        return null;
    }

    private static PortableCellWorkbenchMenuHost locatePortableWorkbench(EntityPlayer player, int slot) {
        if (slot < 0 || slot >= player.inventory.getSizeInventory()) {
            return null;
        }

        ItemStack stack = player.inventory.getStackInSlot(slot);

        if (!MEGAItems.PORTABLE_CELL_WORKBENCH.is(stack)) {
            return null;
        }

        PortableCellWorkbenchItem item = MEGAItems.PORTABLE_CELL_WORKBENCH.asItem();
        return new PortableCellWorkbenchMenuHost(item, player, GuiHostLocators.forInventorySlot(slot));
    }
}
