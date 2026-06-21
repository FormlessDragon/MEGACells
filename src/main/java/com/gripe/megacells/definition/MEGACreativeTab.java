package com.gripe.megacells.definition;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class MEGACreativeTab {
    public static final CreativeTabs TAB = new CreativeTabs("megacells") {
        @Override
        public ItemStack createIcon() {
            return MEGAItems.ITEM_CELL_256M.stack();
        }

        @Override
        public String getTranslationKey() {
            return MEGATranslations.ModName.getTranslationKey();
        }
    };

    private MEGACreativeTab() {
    }
}
