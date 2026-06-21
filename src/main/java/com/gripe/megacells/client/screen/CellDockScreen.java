package com.gripe.megacells.client.screen;

import ae2.client.gui.AEBaseGui;
import ae2.client.gui.style.GuiStyle;
import com.gripe.megacells.menu.CellDockMenu;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;

public class CellDockScreen extends AEBaseGui<CellDockMenu> {
    private final ITextComponent title;

    public CellDockScreen(CellDockMenu menu, InventoryPlayer playerInventory, ITextComponent title, GuiStyle style) {
        super(menu, playerInventory, style);
        this.title = title;
        widgets.addOpenPriorityButton();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (title != null) {
            setTextContent(TEXT_ID_DIALOG_TITLE, title);
        }
    }
}
