package com.gripe.megacells.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import ae2.client.gui.Icon;
import ae2.client.gui.widgets.IconButton;

import com.gripe.megacells.definition.MEGATranslations;

public class CompressionCutoffButton extends IconButton {
    private ItemStack item = ItemStack.EMPTY;

    public CompressionCutoffButton(Runnable onPress) {
        super(onPress);
    }

    public void setItem(ItemStack item) {
        this.item = item == null ? ItemStack.EMPTY : item;
    }

    @Override
    protected Icon getIcon() {
        return Icon.BACKGROUND_STORAGE_CELL;
    }

    @Override
    protected ItemStack getItemStackOverlay() {
        return item;
    }

    @Override
    public List<ITextComponent> getTooltipMessage() {
        List<ITextComponent> message = new ArrayList<>();
        message.add(MEGATranslations.CompressionCutoff.text());

        if (!item.isEmpty()) {
            message.add(item.getTextComponent());
        }

        return message;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(minecraft, mouseX, mouseY, partialTicks);
    }
}
