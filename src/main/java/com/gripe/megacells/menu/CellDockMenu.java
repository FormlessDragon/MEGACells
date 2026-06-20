package com.gripe.megacells.menu;

import net.minecraft.entity.player.InventoryPlayer;

import ae2.container.AEBaseContainer;
import ae2.container.SlotSemantics;
import ae2.container.slot.RestrictedInputSlot;

import com.gripe.megacells.item.part.CellDockPart;

public class CellDockMenu extends AEBaseContainer {
    public CellDockMenu(InventoryPlayer playerInventory, CellDockPart dock) {
        super(playerInventory, dock);
        addSlotToContainer(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, dock.getCellInventory(), 0),
                SlotSemantics.STORAGE_CELL);
        addPlayerInventorySlots(8, 84);
    }
}
